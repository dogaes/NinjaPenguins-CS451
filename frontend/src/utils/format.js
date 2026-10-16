// Shared formatting so every page shows money as $1,234.56 and dates as MM/DD/YYYY (NFR-UI-004).
const usd = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })

export const money = (n) => usd.format(n)

export const date = (iso) => {
  const [y, m, d] = iso.split('-')
  return `${m}/${d}/${y}`
}

export const monthLabel = (yyyyMm) => {
  const [y, m] = yyyyMm.split('-').map(Number)
  return new Date(y, m - 1, 1).toLocaleString('en-US', { month: 'long', year: 'numeric' })
}
