// TypeScript mirrors of the backend DTOs. Keep these in sync with the Java records.

export type Role = 'CUSTOMER' | 'ADMIN'
export type AccountType = 'CHECKING' | 'SAVINGS'
export type AccountStatus = 'ACTIVE' | 'FROZEN' | 'CLOSED'

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  role: Role
}

export interface CustomerProfile {
  id: string
  fullName: string
  phone: string | null
  createdAt: string
}

/** GET /api/me - customer is null for admins. */
export interface Me {
  userId: string
  email: string
  role: Role
  customer: CustomerProfile | null
}

export interface Account {
  id: string
  accountNumber: string
  type: AccountType
  currency: string
  /** A decimal string like "1250.50". Never convert to Number for arithmetic. */
  balance: string
  status: AccountStatus
  createdAt: string
}

/** Admin list row: customer joined with login email. */
export interface CustomerView {
  id: string
  fullName: string
  email: string
  phone: string | null
  createdAt: string
}

export interface RegisterRequest {
  fullName: string
  email: string
  password: string
  phone?: string
}

export interface OpenAccountRequest {
  type: AccountType
  currency: string
}
