export const formatAmount = (amount: number | string | null | undefined, decimals = 2): string => {
  if (amount === null || amount === undefined || amount === '') return '-'
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  if (isNaN(num)) return '-'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(num)
}

export const formatNumber = (num: number | string | null | undefined, decimals = 2): string => {
  if (num === null || num === undefined || num === '') return '-'
  const n = typeof num === 'string' ? parseFloat(num) : num
  if (isNaN(n)) return '-'
  return n.toLocaleString('zh-CN', { minimumFractionDigits: decimals, maximumFractionDigits: decimals })
}

export const formatDate = (date: string | Date | null | undefined): string => {
  if (!date) return '-'
  const d = typeof date === 'string' ? new Date(date) : date
  if (isNaN(d.getTime())) return '-'
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export const formatDateTime = (date: string | Date | null | undefined): string => {
  if (!date) return '-'
  const d = typeof date === 'string' ? new Date(date) : date
  if (isNaN(d.getTime())) return '-'
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

export const formatPercent = (value: number | string | null | undefined, decimals = 2): string => {
  if (value === null || value === undefined || value === '') return '-'
  const v = typeof value === 'string' ? parseFloat(value) : value
  if (isNaN(v)) return '-'
  return `${(v * 100).toFixed(decimals)}%`
}

export const formatPhone = (phone: string | null | undefined): string => {
  if (!phone) return '-'
  if (phone.length === 11) {
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
  }
  return phone
}

export const formatIdCard = (idCard: string | null | undefined): string => {
  if (!idCard) return '-'
  if (idCard.length === 18) {
    return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2')
  }
  return idCard
}