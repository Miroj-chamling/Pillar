-- V1: core banking schema
-- Flyway runs files in version order (V1, V2, ...) and records them in flyway_schema_history.
-- Never edit a migration after it has run - add a new V2__... file instead.

CREATE TABLE customers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name   VARCHAR(200) NOT NULL,
    email       VARCHAR(320) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID          NOT NULL REFERENCES customers (id),
    account_number  VARCHAR(20)   NOT NULL UNIQUE,
    type            VARCHAR(20)   NOT NULL CHECK (type IN ('CHECKING', 'SAVINGS')),
    currency        CHAR(3)       NOT NULL,                         -- ISO 4217, e.g. NPR, USD
    -- Money is NUMERIC, never FLOAT/DOUBLE: 0.1 + 0.2 must equal 0.3 exactly.
    balance         NUMERIC(19, 4) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    version         BIGINT        NOT NULL DEFAULT 0,               -- optimistic locking (@Version)
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Postgres does NOT auto-index foreign keys; we will often query "accounts of customer X".
CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);

-- One row per business event (a deposit, a withdrawal, a transfer).
CREATE TABLE transactions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type             VARCHAR(20)    NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    amount           NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency         CHAR(3)        NOT NULL,
    description      VARCHAR(500),
    -- Client-supplied key so a retried request doesn't move money twice.
    idempotency_key  VARCHAR(100)   UNIQUE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- Double-entry ledger: every transaction produces entries that sum to zero.
-- A transfer of 100 = one DEBIT of 100 on the source + one CREDIT of 100 on the target.
-- The accounts.balance column is a cached total of these entries.
CREATE TABLE ledger_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id  UUID           NOT NULL REFERENCES transactions (id),
    account_id      UUID           NOT NULL REFERENCES accounts (id),
    direction       VARCHAR(6)     NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
    amount          NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    balance_after   NUMERIC(19, 4) NOT NULL,                        -- running balance, handy for statements
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- Composite index for the most common query: "statement for account X, newest first".
CREATE INDEX idx_ledger_account_created ON ledger_entries (account_id, created_at DESC);
CREATE INDEX idx_ledger_transaction_id ON ledger_entries (transaction_id);
