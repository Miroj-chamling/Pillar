/** One field-level validation error from the backend's Problem Details "errors" array. */
export interface FieldError {
  field: string
  message: string
}

/** Thrown for any non-2xx response. Built from the backend's RFC 9457 Problem Details body. */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: FieldError[]

  constructor(status: number, message: string, fieldErrors: FieldError[] = []) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

/** First error message for a form field, if the backend rejected it. */
export function fieldError(error: unknown, field: string): string | undefined {
  return error instanceof ApiError ? error.fieldErrors.find((e) => e.field === field)?.message : undefined
}

// --- token storage -------------------------------------------------------------------------
// localStorage survives page refreshes. Trade-off: any XSS bug on the page could read it.
// A later lesson can move to an httpOnly refresh cookie.

const TOKEN_KEY = 'finance.accessToken'

export const tokenStore = {
  get(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY)
    } catch {
      return null // storage blocked (e.g. some private modes)
    }
  },
  set(token: string) {
    try {
      localStorage.setItem(TOKEN_KEY, token)
    } catch {
      /* ignore - user just won't stay logged in across refreshes */
    }
  },
  clear() {
    try {
      localStorage.removeItem(TOKEN_KEY)
    } catch {
      /* ignore */
    }
  },
}

// Called when an authenticated request gets 401 (token expired or invalid). AuthProvider sets it to logout.
let onUnauthorized: () => void = () => {}

export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler
}

// --- fetch wrapper -------------------------------------------------------------------------

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
}

export async function api<T>(path: string, { method = 'GET', body }: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  const token = tokenStore.get()
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (!res.ok) {
    // Our API errors are Problem Details JSON; Spring Security's 401/403 have an empty body.
    const problem = await res.json().catch(() => null)
    if (res.status === 401 && token) onUnauthorized()
    throw new ApiError(
      res.status,
      problem?.detail ?? (res.status === 401 ? 'Please log in again' : `Request failed (${res.status})`),
      problem?.errors ?? [],
    )
  }

  if (res.status === 204) return undefined as T
  return (await res.json()) as T
}
