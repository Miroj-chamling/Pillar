import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { homeFor, useAuth } from '../auth/AuthContext.ts'
import { ErrorBanner } from '../components/ErrorBanner.tsx'
import { FormField } from '../components/FormField.tsx'

export function LoginPage() {
  const { me, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [error, setError] = useState<unknown>(null)
  const [pending, setPending] = useState(false)

  if (me) return <Navigate to={homeFor(me.role)} replace />

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = new FormData(e.currentTarget)
    setPending(true)
    setError(null)
    try {
      const user = await login(String(form.get('email')), String(form.get('password')))
      // Go back to the page that sent us here, or to this role's home.
      const from = (location.state as { from?: string } | null)?.from
      navigate(from && from !== '/' ? from : homeFor(user.role), { replace: true })
    } catch (err) {
      setError(err)
      setPending(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={handleSubmit}>
        <h1>Sign in</h1>
        <ErrorBanner error={error} />
        <FormField label="Email" name="email" type="email" autoComplete="email" required autoFocus />
        <FormField label="Password" name="password" type="password" autoComplete="current-password" required />
        <button type="submit" className="btn btn-primary btn-block" disabled={pending}>
          {pending ? 'Signing in…' : 'Sign in'}
        </button>
        <p className="muted center">
          New here? <Link to="/register">Create an account</Link>
        </p>
        {import.meta.env.DEV && (
          <p className="dev-hint">
            Dev admin: <span className="mono">admin@finance.local</span> / <span className="mono">Admin@12345</span>
          </p>
        )}
      </form>
    </div>
  )
}
