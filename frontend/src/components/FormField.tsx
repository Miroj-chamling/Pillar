import type { InputHTMLAttributes } from 'react'

interface Props extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  name: string
  /** Error message from the backend for this field, if any. */
  error?: string
}

export function FormField({ label, error, id, name, ...input }: Props) {
  const inputId = id ?? name
  return (
    <div className="field">
      <label htmlFor={inputId}>{label}</label>
      <input
        id={inputId}
        name={name}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? `${inputId}-error` : undefined}
        {...input}
      />
      {error && (
        <span id={`${inputId}-error`} className="field-error">
          {error}
        </span>
      )}
    </div>
  )
}
