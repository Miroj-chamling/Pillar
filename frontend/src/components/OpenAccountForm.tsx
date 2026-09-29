import type { FormEvent } from 'react'
import { fieldError } from '../api/client.ts'
import type { AccountType, OpenAccountRequest } from '../api/types.ts'
import { ErrorBanner } from './ErrorBanner.tsx'

const CURRENCIES = ['NPR', 'USD', 'EUR', 'INR', 'GBP']

interface Props {
  onSubmit: (req: OpenAccountRequest) => void
  isPending: boolean
  error: unknown
}

export function OpenAccountForm({ onSubmit, isPending, error }: Props) {
  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    // Uncontrolled form: read the values from the DOM on submit instead of keeping React state.
    const form = new FormData(e.currentTarget)
    onSubmit({
      type: form.get('type') as AccountType,
      currency: String(form.get('currency')),
    })
  }

  const fieldMessage = fieldError(error, 'type') ?? fieldError(error, 'currency')

  return (
    <form className="card inline-form" onSubmit={handleSubmit}>
      <h3>Open a new account</h3>
      <div className="row">
        <div className="field">
          <label htmlFor="type">Type</label>
          <select id="type" name="type" defaultValue="SAVINGS">
            <option value="SAVINGS">Savings</option>
            <option value="CHECKING">Checking</option>
          </select>
        </div>
        <div className="field">
          <label htmlFor="currency">Currency</label>
          <select id="currency" name="currency" defaultValue="NPR">
            {CURRENCIES.map((c) => (
              <option key={c}>{c}</option>
            ))}
          </select>
        </div>
        <button type="submit" className="btn btn-primary" disabled={isPending}>
          {isPending ? 'Opening…' : 'Open account'}
        </button>
      </div>
      <ErrorBanner error={fieldMessage ? new Error(fieldMessage) : error} />
    </form>
  )
}
