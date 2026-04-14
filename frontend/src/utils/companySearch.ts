import { ref } from 'vue'
import { get } from './request'

export interface CompanyInfo {
  id: number
  companyName: string
  creditCode: string
  address: string
  contactName: string
  contactPhone: string
  bankName: string
  bankAccount: string
  taxNo: string
  email: string
}

export interface UseCompanySearchOptions {
  minLength?: number
  debounce?: number
  onSelect?: (company: CompanyInfo) => void
}

export function useCompanySearch(options: UseCompanySearchOptions = {}) {
  const minLength = options.minLength || 2
  const debounceTime = options.debounce || 300
  
  const keyword = ref('')
  const loading = ref(false)
  const companyList = ref<CompanyInfo[]>([])
  const selectedCompany = ref<CompanyInfo | null>(null)
  const showDropdown = ref(false)
  const error = ref('')

  let debounceTimer: ReturnType<typeof setTimeout> | null = null

  const search = async (kw: string) => {
    if (kw.length < minLength) {
      companyList.value = []
      return
    }
    
    loading.value = true
    error.value = ''
    
    try {
      const res = await get<{ code: number; data: CompanyInfo[] }>('/api/v1/company/search', { keyword: kw })
      if (res.code === 200) {
        companyList.value = res.data || []
        showDropdown.value = companyList.value.length > 0
      } else {
        error.value = res.message || '搜索失败'
      }
    } catch (e: any) {
      error.value = e.message || '搜索失败'
      companyList.value = []
    } finally {
      loading.value = false
    }
  }

  const debouncedSearch = (kw: string) => {
    if (debounceTimer) {
      clearTimeout(debounceTimer)
    }
    debounceTimer = setTimeout(() => {
      search(kw)
    }, debounceTime)
  }

  const handleInput = (value: string) => {
    keyword.value = value
    if (value.length >= minLength) {
      debouncedSearch(value)
    } else {
      companyList.value = []
      showDropdown.value = false
    }
  }

  const handleSelect = (company: CompanyInfo) => {
    selectedCompany.value = company
    showDropdown.value = false
    keyword.value = company.companyName
    options.onSelect?.(company)
  }

  const handleBlur = () => {
    setTimeout(() => {
      showDropdown.value = false
    }, 200)
  }

  const handleFocus = () => {
    if (companyList.value.length > 0) {
      showDropdown.value = true
    }
  }

  const reset = () => {
    keyword.value = ''
    companyList.value = []
    selectedCompany.value = null
    showDropdown.value = false
    error.value = ''
  }

  const getCompanyDetail = async (id: number): Promise<CompanyInfo | null> => {
    try {
      const res = await get<{ code: number; data: CompanyInfo }>(`/api/v1/company/${id}`)
      if (res.code === 200) {
        return res.data
      }
      return null
    } catch {
      return null
    }
  }

  return {
    keyword,
    loading,
    companyList,
    selectedCompany,
    showDropdown,
    error,
    search,
    handleInput,
    handleSelect,
    handleBlur,
    handleFocus,
    reset,
    getCompanyDetail
  }
}