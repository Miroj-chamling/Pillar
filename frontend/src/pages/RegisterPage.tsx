import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import { ApiError, fieldError } from '../api/client.ts'
import { homeFor, useAuth } from '../auth/AuthContext.ts'
import { ErrorBanner } from '../components/ErrorBanner.tsx'
import { FormField } from '../components/FormField.tsx'

export function RegisterPage() {
  const { me, register } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<unknown>(null)
  const [pending, setPending] = useState(false)

  if (me) return <Navigate to={homeFor(me.role)} replace />

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = new FormData(e.currentTarget)
    const phone = String(form.get('phone')).trim()
    setPending(true)
    setError(null)
    try {
      const user = await register({
        fullName: String(form.get('fullName')),
        email: String(form.get('email')),
        password: String(form.get('password')),
        phone: phone || undefined, // omit rather than send "" (backend validates the format)
      })
      navigate(homeFor(user.role), { replace: true })
    } catch (err) {
      setError(err)
      setPending(false)
    }
  }

  // Field problems show under each input; only show the banner for everything else (e.g. 409 email taken).
  const hasFieldErrors = error instanceof ApiError && error.fieldErrors.length > 0

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={handleSubmit}>
        <h1>Create your account</h1>
        {!hasFieldErrors && <ErrorBanner error={error} />}
        <FormField label="Full name" name="fullName" autoComplete="name" required maxLength={200}
          error={fieldError(error, 'fullName')} />
        <FormField label="Email" name="email" type="email" autoComplete="email" required
          error={fieldError(error, 'email')} />
        <FormField label="Password" name="password" type="password" autoComplete="new-password" required
          minLength={8} maxLength={72} placeholder="At least 8 characters"
          error={fieldError(error, 'password')} />
        <FormField label="Phone (optional)" name="phone" type="tel" autoComplete="tel" placeholder="+9779800000000"
          error={fieldError(error, 'phone')} />
        <button type="submit" className="btn btn-primary btn-block" disabled={pending}>
          {pending ? 'Creating account…' : 'Create account'}
        </button>
        <p className="muted center">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  )
}
