import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { setUnauthorizedHandler, tokenStore } from '../api/client.ts'
import { authApi } from '../api/endpoints.ts'
import type { RegisterRequest } from '../api/types.ts'
import { AuthContext, type AuthState } from './AuthContext.ts'

/**
 * Holds the JWT and the current user (/api/me) for the whole app.
 * The token lives in localStorage so a refresh keeps you logged in; /api/me is re-fetched on load.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [token, setToken] = useState<string | null>(() => tokenStore.get())

  // The token is part of the query key, so one user's cached data can never show for another.
  const meQuery = useQuery({
    queryKey: ['me', token],
    queryFn: authApi.me,
    enabled: token !== null,
    staleTime: Infinity,
  })

  const logout = useCallback(() => {
    tokenStore.clear()
    setToken(null)
    queryClient.clear() // drop every cached response from the previous session
  }, [queryClient])

  // Any 401 on an authenticated call (expired token) logs out; RequireAuth then redirects to /login.
  useEffect(() => setUnauthorizedHandler(logout), [logout])

  const login = useCallback(
    async (email: string, password: string) => {
      const { accessToken } = await authApi.login(email, password)
      tokenStore.set(accessToken)
      // Load /api/me into the cache before switching token, so the UI never flashes a logged-out state.
      const me = await queryClient.fetchQuery({ queryKey: ['me', accessToken], queryFn: authApi.me })
      setToken(accessToken)
      return me
    },
    [queryClient],
  )

  const register = useCallback(
    async (req: RegisterRequest) => {
      await authApi.register(req)
      return login(req.email, req.password) // sign straight in after registering
    },
    [login],
  )

  const value = useMemo<AuthState>(
    () => ({
      // If /api/me fails for a non-401 reason, me stays null and RequireAuth sends the user to /login.
      me: token ? (meQuery.data ?? null) : null,
      isLoading: token !== null && meQuery.isPending,
      login,
      register,
      logout,
    }),
    [token, meQuery.data, meQuery.isPending, login, register, logout],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
