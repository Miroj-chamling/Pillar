/**
 * Formats a decimal string like "1250.5" as "NPR 1,250.50".
 * Intl.NumberFormat accepts the string directly, so we never round-trip money through a JS float.
 */
export function formatMoney(amount: string, currency: string): string {
  try {
    return new Intl.NumberFormat(undefined, { style: 'currency', currency }).format(
      amount as `${number}`,
    )
  } catch {
    return `${currency} ${amount}` // unknown currency code
  }
}
