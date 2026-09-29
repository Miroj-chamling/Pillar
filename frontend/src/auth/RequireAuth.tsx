import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import type { Role } from '../api/types.ts'
import { homeFor, useAuth } from './AuthContext.ts'

/**
 * Route guard. Logged out -> /login (remembering where you were going).
 * Wrong role -> your own home page.
 * UX only: the backend enforces the same rules, so hiding a page is never the real security.
 */
export function RequireAuth({ role, children }: { role?: Role; children: ReactNode }) {
  const { me, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) return <p className="center muted">Loading…</p>
  if (!me) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (role && me.role !== role) return <Navigate to={homeFor(me.role)} replace />
  return children
}
