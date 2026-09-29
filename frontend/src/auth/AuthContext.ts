import { createContext, useContext } from 'react'
import type { Me, RegisterRequest, Role } from '../api/types.ts'

export interface AuthState {
  /** The logged-in user, or null when logged out. */
  me: Me | null
  /** True while we have a token but haven't loaded /api/me yet (e.g. right after a page refresh). */
  isLoading: boolean
  login: (email: string, password: string) => Promise<Me>
  register: (req: RegisterRequest) => Promise<Me>
  logout: () => void
}

export const AuthContext = createContext<AuthState | null>(null)

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}

/** Where each role lands after logging in. */
export function homeFor(role: Role): string {
  return role === 'ADMIN' ? '/admin/customers' : '/accounts'
}
