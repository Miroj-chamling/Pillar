-- V2: login credentials, roles, and account-number generation.
-- We don't edit V1 (it already ran); we evolve the schema with ALTERs instead.

-- Login identity, kept separate from banking data (customers).
-- An ADMIN user has no customer profile; a CUSTOMER user has exactly one.
CREATE TABLE users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email          VARCHAR(320) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,          -- BCrypt hash, never the plain password
    role           VARCHAR(20)  NOT NULL CHECK (role IN ('CUSTOMER', 'ADMIN')),
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Unique index on lower(email): "Ram@X.com" and "ram@x.com" count as the same login.
CREATE UNIQUE INDEX uq_users_email_lower ON users (lower(email));

-- Email now lives on users (it's the login), so drop the duplicate from customers
-- and link each customer to its user. UNIQUE makes the relationship 1:1.
ALTER TABLE customers DROP COLUMN email;
ALTER TABLE customers ADD COLUMN user_id UUID NOT NULL UNIQUE REFERENCES users (id);
ALTER TABLE customers ADD COLUMN phone VARCHAR(20);

-- Account numbers come from a sequence: unique, never reused, no race conditions.
-- The app formats nextval() as a 12-digit number, e.g. 100000000001.
CREATE SEQUENCE account_number_seq START WITH 100000000001;
