import { NavLink, Outlet } from 'react-router'
import { useAuth } from '../auth/AuthContext.ts'

/** Shell for every logged-in page: header + nav + the current page (<Outlet />). */
export function Layout() {
  const { me, logout } = useAuth()
  if (!me) return null // RequireAuth guarantees me; this just narrows the type

  return (
    <div className="app">
      <header className="topbar">
        <div className="topbar-inner">
          <span className="brand">Finance</span>
          <nav>
            {me.role === 'CUSTOMER' && <NavLink to="/accounts">My accounts</NavLink>}
            {me.role === 'ADMIN' && <NavLink to="/admin/customers">Customers</NavLink>}
          </nav>
          <div className="user">
            <span className="muted">{me.email}</span>
            <span className="badge">{me.role}</span>
            <button type="button" className="btn btn-ghost" onClick={logout}>
              Log out
            </button>
          </div>
        </div>
      </header>
      <main className="container">
        <Outlet />
      </main>
    </div>
  )
}
