export interface ValidationRule {
  required?: boolean
  message?: string
  validator?: (value: any) => boolean | string
  trigger?: 'blur' | 'change' | 'input'
}

export interface ValidationRules {
  [key: string]: ValidationRule[]
}

export const validateRequired = (value: any): boolean => {
  if (value === null || value === undefined) return false
  if (typeof value === 'string') return value.trim().length > 0
  if (Array.isArray(value)) return value.length > 0
  return true
}

export const validateEmail = (email: string): boolean => {
  if (!email) return true
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return reg.test(email)
}

export const validatePhone = (phone: string): boolean => {
  if (!phone) return true
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

export const validateIdCard = (idCard: string): boolean => {
  if (!idCard) return true
  const reg = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
  return reg.test(idCard)
}

export const validatePositiveNumber = (value: any): boolean => {
  if (value === null || value === undefined || value === '') return true
  const num = typeof value === 'string' ? parseFloat(value) : value
  return !isNaN(num) && num > 0
}

export const validateNonNegativeNumber = (value: any): boolean => {
  if (value === null || value === undefined || value === '') return true
  const num = typeof value === 'string' ? parseFloat(value) : value
  return !isNaN(num) && num >= 0
}

export const validateAmount = (value: any): boolean => {
  if (value === null || value === undefined || value === '') return true
  const num = typeof value === 'string' ? parseFloat(value) : value
  return !isNaN(num) && num >= 0
}

export const validateDateRange = (startDate: string | Date, endDate: string | Date): boolean => {
  if (!startDate || !endDate) return true
  const start = new Date(startDate)
  const end = new Date(endDate)
  return start <= end
}

export const validateFutureDate = (date: string | Date | null | undefined): boolean => {
  if (!date) return true
  const d = new Date(date)
  return d > new Date()
}

export const validatePastDate = (date: string | Date | null | undefined): boolean => {
  if (!date) return true
  const d = new Date(date)
  return d < new Date()
}

export interface FormValidateRule {
  field: string
  label: string
  value: any
  rules: ValidationRule[]
}

export const validateField = (rule: FormValidateRule): { valid: boolean; message: string } => {
  for (const r of rule.rules) {
    if (r.required && !validateRequired(rule.value)) {
      return { valid: false, message: r.message || `${rule.label}不能为空` }
    }
    if (r.validator) {
      const result = r.validator(rule.value)
      if (result !== true) {
        return { valid: false, message: typeof result === 'string' ? result : r.message || `${rule.label}格式不正确` }
      }
    }
  }
  return { valid: true, message: '' }
}

export const validateForm = (rules: FormValidateRule[]): { valid: boolean; errors: Record<string, string> } => {
  const errors: Record<string, string> = {}
  for (const rule of rules) {
    const result = validateField(rule)
    if (!result.valid) {
      errors[rule.field] = result.message
    }
  }
  return { valid: Object.keys(errors).length === 0, errors }
}