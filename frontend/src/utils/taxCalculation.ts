import { ref, computed, watch } from 'vue'

export interface TaxAmountLinkageOptions {
  initialAmountWithTax?: number
  initialAmountWithoutTax?: number
  initialTaxRate?: number
  onAmountWithTaxChange?: (value: number) => void
  onAmountWithoutTaxChange?: (value: number) => void
  onTaxRateChange?: (value: number) => void
}

export interface TaxAmountLinkageReturn {
  amountWithTax: ReturnType<typeof ref>
  amountWithoutTax: ReturnType<typeof ref>
  taxRate: ReturnType<typeof ref>
  taxAmount: ReturnType<typeof computed>
  lastEditedField: ReturnType<typeof ref>
  setAmountWithTax: (value: number) => void
  setAmountWithoutTax: (value) => void
  setTaxRate: (value: number) => void
  reset: () => void
}

export const TAX_RATE_OPTIONS = [
  { label: '0%', value: 0 },
  { label: '1%', value: 0.01 },
  { label: '3%', value: 0.03 },
  { label: '6%', value: 0.06 },
  { label: '9%', value: 0.09 },
  { label: '13%', value: 0.13 }
]

export function useTaxAmountLinkage(options: TaxAmountLinkageOptions = {}): TaxAmountLinkageReturn {
  const amountWithTax = ref(options.initialAmountWithTax || 0)
  const amountWithoutTax = ref(options.initialAmountWithoutTax || 0)
  const taxRate = ref(options.initialTaxRate || 0)
  const lastEditedField = ref<'withTax' | 'withoutTax' | null>(null)

  const calculateWithoutTax = (withTax: number, rate: number): number => {
    if (rate === 0) return withTax
    return withTax / (1 + rate)
  }

  const calculateWithTax = (withoutTax: number, rate: number): number => {
    return withoutTax * (1 + rate)
  }

  const calculateTaxAmount = (withTax: number, withoutTax: number): number => {
    return withTax - withoutTax
  }

  const taxAmount = computed(() => calculateTaxAmount(amountWithTax.value, amountWithoutTax.value))

  const setAmountWithTax = (value: number) => {
    lastEditedField.value = 'withTax'
    amountWithTax.value = value
    if (taxRate.value > 0) {
      amountWithoutTax.value = Number(calculateWithoutTax(value, taxRate.value).toFixed(2))
    }
    options.onAmountWithTaxChange?.(value)
  }

  const setAmountWithoutTax = (value: number) => {
    lastEditedField.value = 'withoutTax'
    amountWithoutTax.value = value
    if (taxRate.value > 0) {
      amountWithTax.value = Number(calculateWithTax(value, taxRate.value).toFixed(2))
    }
    options.onAmountWithoutTaxChange?.(value)
  }

  const setTaxRate = (value: number) => {
    taxRate.value = value
    if (lastEditedField.value === 'withTax' && value > 0) {
      amountWithoutTax.value = Number(calculateWithoutTax(amountWithTax.value, value).toFixed(2))
    } else if (lastEditedField.value === 'withoutTax' && value > 0) {
      amountWithTax.value = Number(calculateWithTax(amountWithoutTax.value, value).toFixed(2))
    } else if (value > 0) {
      if (amountWithoutTax.value > 0) {
        amountWithTax.value = Number(calculateWithTax(amountWithoutTax.value, value).toFixed(2))
      } else if (amountWithTax.value > 0) {
        amountWithoutTax.value = Number(calculateWithoutTax(amountWithTax.value, value).toFixed(2))
      }
    }
    options.onTaxRateChange?.(value)
  }

  const reset = () => {
    amountWithTax.value = 0
    amountWithoutTax.value = 0
    taxRate.value = 0
    lastEditedField.value = null
  }

  return {
    amountWithTax,
    amountWithoutTax,
    taxRate,
    taxAmount,
    lastEditedField,
    setAmountWithTax,
    setAmountWithoutTax,
    setTaxRate,
    reset
  }
}