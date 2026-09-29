import { BrowserRouter, Navigate, Route, Routes } from 'react-router'
import { homeFor, useAuth } from './auth/AuthContext.ts'
import { RequireAuth } from './auth/RequireAuth.tsx'
import { Layout } from './components/Layout.tsx'
import { AccountsPage } from './pages/AccountsPage.tsx'
import { AdminCustomerDetailPage } from './pages/AdminCustomerDetailPage.tsx'
import { AdminCustomersPage } from './pages/AdminCustomersPage.tsx'
import { LoginPage } from './pages/LoginPage.tsx'
import { NotFoundPage } from './pages/NotFoundPage.tsx'
import { RegisterPage } from './pages/RegisterPage.tsx'

/** "/" sends each role to its own home page. */
function HomeRedirect() {
  const { me } = useAuth()
  return me ? <Navigate to={homeFor(me.role)} replace /> : null
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Everything inside requires login and shares the Layout (header + nav) */}
        <Route element={<RequireAuth><Layout /></RequireAuth>}>
          <Route index element={<HomeRedirect />} />
          <Route path="accounts" element={<RequireAuth role="CUSTOMER"><AccountsPage /></RequireAuth>} />
          <Route path="admin/customers" element={<RequireAuth role="ADMIN"><AdminCustomersPage /></RequireAuth>} />
          <Route
            path="admin/customers/:customerId"
            element={<RequireAuth role="ADMIN"><AdminCustomerDetailPage /></RequireAuth>}
          />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  )
}
