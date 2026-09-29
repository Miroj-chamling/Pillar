import { Link } from 'react-router'

export function NotFoundPage() {
  return (
    <div className="auth-page">
      <div className="card auth-card center">
        <h1>Page not found</h1>
        <Link to="/">Go home</Link>
      </div>
    </div>
  )
}
