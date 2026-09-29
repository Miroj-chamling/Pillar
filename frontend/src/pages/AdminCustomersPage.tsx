import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { adminApi } from '../api/endpoints.ts'
import { ErrorBanner } from '../components/ErrorBanner.tsx'

export function AdminCustomersPage() {
  const customers = useQuery({ queryKey: ['admin', 'customers'], queryFn: adminApi.customers })

  return (
    <>
      <h1>Customers</h1>
      {customers.isPending && <p className="muted">Loading customers…</p>}
      <ErrorBanner error={customers.error} />
      {customers.data?.length === 0 && <p className="muted">No customers have registered yet.</p>}
      {customers.data && customers.data.length > 0 && (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Joined</th>
              </tr>
            </thead>
            <tbody>
              {customers.data.map((c) => (
                <tr key={c.id}>
                  <td>
                    <Link to={`/admin/customers/${c.id}`} state={{ customer: c }}>
                      {c.fullName}
                    </Link>
                  </td>
                  <td>{c.email}</td>
                  <td>{c.phone ?? '—'}</td>
                  <td>{new Date(c.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
