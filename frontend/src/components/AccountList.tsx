import type { Account } from '../api/types.ts'
import { formatMoney } from '../lib/money.ts'

export function AccountList({ accounts }: { accounts: Account[] }) {
  if (accounts.length === 0) {
    return <p className="muted">No accounts yet.</p>
  }
  return (
    <ul className="account-grid">
      {accounts.map((a) => (
        <li key={a.id} className="card account-card">
          <div className="account-head">
            <span className="account-type">{a.type === 'CHECKING' ? 'Checking' : 'Savings'}</span>
            <span className={`badge badge-${a.status.toLowerCase()}`}>{a.status}</span>
          </div>
          <div className="account-balance">{formatMoney(a.balance, a.currency)}</div>
          <div className="account-number mono">{a.accountNumber}</div>
        </li>
      ))}
    </ul>
  )
}
