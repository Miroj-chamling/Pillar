import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useLocation, useParams } from 'react-router'
import { adminApi } from '../api/endpoints.ts'
import type { CustomerView } from '../api/types.ts'
import { AccountList } from '../components/AccountList.tsx'
import { ErrorBanner } from '../components/ErrorBanner.tsx'
import { OpenAccountForm } from '../components/OpenAccountForm.tsx'

/** Admin view of one customer: their accounts + open an account on their behalf. */
export function AdminCustomerDetailPage() {
  const { customerId = '' } = useParams()
  const queryClient = useQueryClient()
  // Name/email passed from the list page via router state (absent if you open the URL directly).
  const customer = (useLocation().state as { customer?: CustomerView } | null)?.customer

  const accountsKey = ['admin', 'customers', customerId, 'accounts']
  const accounts = useQuery({ queryKey: accountsKey, queryFn: () => adminApi.customerAccounts(customerId) })

  const openAccount = useMutation({
    mutationFn: (req: Parameters<typeof adminApi.openAccountFor>[1]) => adminApi.openAccountFor(customerId, req),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: accountsKey }),
  })

  return (
    <>
      <p>
        <Link to="/admin/customers">← All customers</Link>
      </p>
      <h1>{customer?.fullName ?? 'Customer'}</h1>
      {customer && <p className="muted">{customer.email}</p>}

      {accounts.isPending && <p className="muted">Loading accounts…</p>}
      <ErrorBanner error={accounts.error} />
      {accounts.data && <AccountList accounts={accounts.data} />}

      <OpenAccountForm
        onSubmit={(req) => openAccount.mutate(req)}
        isPending={openAccount.isPending}
        error={openAccount.error}
      />
    </>
  )
}
