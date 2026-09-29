import { api } from './client.ts'
import type {
  Account,
  CustomerView,
  Me,
  OpenAccountRequest,
  RegisterRequest,
  TokenResponse,
} from './types.ts'

export const authApi = {
  login: (email: string, password: string) =>
    api<TokenResponse>('/api/auth/login', { method: 'POST', body: { email, password } }),
  register: (req: RegisterRequest) => api<Me>('/api/auth/register', { method: 'POST', body: req }),
  me: () => api<Me>('/api/me'),
}

export const accountsApi = {
  list: () => api<Account[]>('/api/accounts'),
  open: (req: OpenAccountRequest) => api<Account>('/api/accounts', { method: 'POST', body: req }),
}

export const adminApi = {
  customers: () => api<CustomerView[]>('/api/admin/customers'),
  customerAccounts: (customerId: string) => api<Account[]>(`/api/admin/customers/${customerId}/accounts`),
  openAccountFor: (customerId: string, req: OpenAccountRequest) =>
    api<Account>(`/api/admin/customers/${customerId}/accounts`, { method: 'POST', body: req }),
}
