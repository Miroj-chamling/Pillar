import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { accountsApi } from '../api/endpoints.ts'
import { useAuth } from '../auth/AuthContext.ts'
import { AccountList } from '../components/AccountList.tsx'
import { ErrorBanner } from '../components/ErrorBanner.tsx'
import { OpenAccountForm } from '../components/OpenAccountForm.tsx'

/** Customer home: my accounts + open a new one. */
export function AccountsPage() {
  const { me } = useAuth()
  const queryClient = useQueryClient()

  // useQuery: fetch + cache + loading/error state. The key identifies this data in the cache.
  const accounts = useQuery({ queryKey: ['accounts'], queryFn: accountsApi.list })

  // useMutation for writes; on success, mark the list stale so it refetches.
  const openAccount = useMutation({
    mutationFn: accountsApi.open,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['accounts'] }),
  })

  return (
    <>
      <h1>Hello, {me?.customer?.fullName ?? me?.email}</h1>
      <p className="muted">Your accounts</p>

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
