import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      window.location.href = '/login'
    } else if (error.response?.status === 403) {
      ElMessage.error('没有权限或登录已失效，请重新登录')
    }
    return Promise.reject(error)
  }
)

export default service

export function request<T = any>(config: AxiosRequestConfig): Promise<T> {
  return service(config) as Promise<T>
}

export const api = {
  auth: {
    login: (data: any) => request({ url: '/auth/login', method: 'POST', data }),
    sendSms: (data: any) => request({ url: '/auth/send-sms', method: 'POST', data }),
    resetPassword: (data: any) => request({ url: '/auth/reset-password', method: 'POST', data }),
    getUserInfo: () => request({ url: '/auth/info', method: 'GET' }),
    logout: () => request({ url: '/auth/logout', method: 'POST' }),
  },
  home: {
    getStats: () => request({ url: '/home/stats', method: 'GET' }),
  },
  
  project: {
    list: () => request({ url: '/project/list', method: 'GET' }),
    page: (params: any) => request({ url: '/project/page', method: 'GET', params }),
    get: (id: number) => request({ url: `/project/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/project', method: 'POST', data }),
    update: (data: any) => request({ url: '/project', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/project/${id}`, method: 'DELETE' }),
    submit: (id: number) => request({ url: `/project/${id}/submit`, method: 'POST' }),
  },
  
  contract: {
    income: {
      list: () => request({ url: '/contract/income/list', method: 'GET' }),
      page: (params: any) => request({ url: '/contract/income/page', method: 'GET', params }),
      get: (id: number) => request({ url: `/contract/income/${id}`, method: 'GET' }),
      create: (data: any) => request({ url: '/contract/income', method: 'POST', data }),
      update: (data: any) => request({ url: '/contract/income', method: 'PUT', data }),
      delete: (id: number) => request({ url: `/contract/income/${id}`, method: 'DELETE' }),
      submit: (id: number) => request({ url: `/contract/income/${id}/submit`, method: 'POST' }),
    },
    expense: {
      list: () => request({ url: '/contract/expense/list', method: 'GET' }),
      page: (params: any) => request({ url: '/contract/expense/page', method: 'GET', params }),
      get: (id: number) => request({ url: `/contract/expense/${id}`, method: 'GET' }),
      create: (data: any) => request({ url: '/contract/expense', method: 'POST', data }),
      update: (data: any) => request({ url: '/contract/expense', method: 'PUT', data }),
      delete: (id: number) => request({ url: `/contract/expense/${id}`, method: 'DELETE' }),
    },
  },
  
  statement: {
    list: () => request({ url: '/statement/list', method: 'GET' }),
    summary: () => request({ url: '/statement/summary', method: 'GET' }),
    page: (params: any) => request({ url: '/statement/page', method: 'GET', params }),
    get: (id: number) => request({ url: `/statement/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/statement', method: 'POST', data }),
    update: (data: any) => request({ url: '/statement', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/statement/${id}`, method: 'DELETE' }),
    submit: (id: number) => request({ url: `/statement/${id}/submit`, method: 'POST' }),
    getConfig: (projectId: number) => request({ url: `/statement/config/${projectId}`, method: 'GET' }),
    updateConfig: (projectId: number, data: any) => request({ url: `/statement/config/${projectId}`, method: 'PUT' }),
    generateManual: (data: any) => request({ url: '/statement/generate-manual', method: 'POST', data }),
    getDifferencePeriods: (projectId: number) => request({ url: `/statement/difference/periods/${projectId}`, method: 'GET' }),
    getDifferenceAnalysis: (params: any) => request({ url: '/statement/difference/analysis', method: 'GET', params }),
    saveDifferenceRemark: (data: any) => request({ url: '/statement/difference/remark', method: 'POST', data }),
    exportDifference: (data: any) => request({ url: '/statement/difference/export', method: 'POST', data }),
    createSplit: (data: any) => request({ url: '/statement/split', method: 'POST', data }),
    getSplitList: (contractId: number) => request({ url: '/statement/split/list', method: 'GET', params: { contractId } }),
  },
  
  paymentSupervision: {
    list: () => request({ url: '/payment-supervision/list', method: 'GET' }),
    summary: () => request({ url: '/payment-supervision/summary', method: 'GET' }),
    page: (params: any) => request({ url: '/payment-supervision/page', method: 'GET', params }),
    get: (id: number) => request({ url: `/payment-supervision/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/payment-supervision', method: 'POST', data }),
    update: (data: any) => request({ url: '/payment-supervision', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/payment-supervision/${id}`, method: 'DELETE' }),
    submit: (id: number) => request({ url: `/payment-supervision/${id}/submit`, method: 'POST' }),
    approve: (id: number, params: any) => request({ url: `/payment-supervision/${id}/approve`, method: 'POST', params }),
    precheck: (projectId: number) => request({ url: `/payment-supervision/precheck/${projectId}`, method: 'GET' }),
    generate: (data: any) => request({ url: '/payment-supervision/generate', method: 'POST', data }),
    assign: (id: number, data: any) => request({ url: `/payment-supervision/${id}/assign`, method: 'POST', data }),
    accept: (id: number) => request({ url: `/payment-supervision/${id}/accept`, method: 'POST' }),
    feedback: (id: number, data: any) => request({ url: `/payment-supervision/${id}/feedback`, method: 'POST', data }),
    updateFeedback: (feedbackId: number, data: any) => request({ url: `/payment-supervision/feedback/${feedbackId}`, method: 'PUT', data }),
    export: (data: any) => request({ url: '/payment-supervision/export', method: 'POST', data }),
  },
  
  expense: {
    report: {
      list: () => request({ url: '/expense/report/list', method: 'GET' }),
      page: (params: any) => request({ url: '/expense/report/page', method: 'GET', params }),
      get: (id: number) => request({ url: `/expense/report/${id}`, method: 'GET' }),
      create: (data: any) => request({ url: '/expense/report', method: 'POST', data }),
      update: (data: any) => request({ url: '/expense/report', method: 'PUT', data }),
      delete: (id: number) => request({ url: `/expense/report/${id}`, method: 'DELETE' }),
      submit: (id: number) => request({ url: `/expense/report/${id}/submit`, method: 'POST' }),
      approve: (id: number, params: any) => request({ url: `/expense/report/${id}/approve`, method: 'POST', params }),
      uploadAttachment: (id: number, data: any) => request({ url: `/expense/report/${id}/attachments`, method: 'POST', data }),
      getAttachments: (id: number) => request({ url: `/expense/report/${id}/attachments`, method: 'GET' }),
      deleteAttachment: (attachmentId: number) => request({ url: `/expense/attachment/${attachmentId}`, method: 'DELETE' }),
      budgetCheck: (params: any) => request({ url: '/expense/report/budget-check', method: 'GET', params }),
      verifyInvoice: (data: any) => request({ url: '/expense/report/invoice-verify', method: 'POST', data }),
      export: (data: any) => request({ url: '/expense/report/export', method: 'POST', data }),
      getApprovalTrace: (id: number) => request({ url: `/expense/report/${id}/approval-trace`, method: 'GET' }),
      summary: (params?: any) => request({ url: '/expense/report/summary', method: 'GET', params }),
    },
  },
  
  purchase: {
    list: () => request({ url: '/purchase/list', method: 'GET' }),
    page: (params: any) => request({ url: '/purchase/page', method: 'GET', params }),
    get: (id: number) => request({ url: `/purchase/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/purchase', method: 'POST', data }),
    update: (data: any) => request({ url: '/purchase', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/purchase/${id}`, method: 'DELETE' }),
    submit: (id: number) => request({ url: `/purchase/${id}/submit`, method: 'POST' }),
    price: {
      baseList: (params: any) => request({ url: '/purchase/price/base/list', method: 'GET', params }),
      standardList: (params: any) => request({ url: '/purchase/price/standard/list', method: 'GET', params }),
      search: (data: any) => request({ url: '/purchase/price/search', method: 'GET', params: data }),
      history: (id: number) => request({ url: `/purchase/price/${id}/history`, method: 'GET' }),
    },
    inquiry: {
      list: (params: any) => request({ url: '/purchase/inquiry/list', method: 'GET', params }),
      page: (params: any) => request({ url: '/purchase/inquiry/page', method: 'GET', params }),
      get: (id: number) => request({ url: `/purchase/inquiry/${id}`, method: 'GET' }),
      create: (data: any) => request({ url: '/purchase/inquiry', method: 'POST', data }),
      submit: (id: number) => request({ url: `/purchase/inquiry/${id}/submit`, method: 'POST' }),
    },
    compare: (data: any) => request({ url: '/purchase/compare', method: 'POST', data }),
    checkLimit: (params: any) => request({ url: '/purchase/limit-check', method: 'GET', params }),
    overrideLimit: (data: any) => request({ url: '/purchase/limit-override', method: 'POST', data }),
    getMatchResult: (purchaseId: number) => request({ url: `/purchase/match-result/${purchaseId}`, method: 'GET' }),
    confirmPrice: (data: any) => request({ url: '/purchase/confirm-price', method: 'POST', data }),
  },
  
supplier: {
    list: () => request({ url: '/supplier/list', method: 'GET' }),
    page: (params: any) => request({ url: '/supplier/page', method: 'GET', params }),
    get: (id: number) => request({ url: `/supplier/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/supplier', method: 'POST', data }),
    update: (data: any) => request({ url: '/supplier', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/supplier/${id}`, method: 'DELETE' }),
    rating: {
      list: (params: any) => request({ url: '/supplier/rating/list', method: 'GET', params }),
      get: (id: number) => request({ url: `/supplier/rating/${id}`, method: 'GET' }),
      recalc: (id: number) => request({ url: `/supplier/rating/${id}/recalc`, method: 'POST' }),
      export: (params: any) => request({ url: '/supplier/rating/export', method: 'GET', params }),
    }
  },
  
  material: {
    list: () => request({ url: '/material/list', method: 'GET' }),
    page: (params: any) => request({ url: '/material/page', method: 'GET', params }),
    get: (id: number) => request({ url: `/material/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/material', method: 'POST', data }),
    update: (data: any) => request({ url: '/material', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/material/${id}`, method: 'DELETE' }),
    outbound: (data: any) => request({ url: '/material/outbound', method: 'POST', data }),
    transfer: (data: any) => request({ url: '/material/transfer', method: 'POST', data }),
    validateTransfer: (data: any) => request({ url: '/material/outbound/validate-transfer', method: 'POST', data }),
    getStock: (warehouseId: number) => request({ url: `/material/stock/${warehouseId}`, method: 'GET' }),
    validateOutbound: (data: any) => request({ url: '/material/outbound/validate', method: 'POST', data }),
    validateCrossProjectTransfer: (data: any) => request({ url: '/material/transfer/validate-cross-project', method: 'POST', data }),
    approveTransfer: (data: any) => request({ url: '/material/transfer/approve', method: 'POST', data }),
    getTransferLog: (params: any) => request({ url: '/material/transfer/log', method: 'GET', params }),
  },
  
  gantt: {
    list: () => request({ url: '/gantt/list', method: 'GET' }),
    page: (params: any) => request({ url: '/gantt/page', method: 'GET', params }),
    get: (id: number) => request({ url: `/gantt/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/gantt', method: 'POST', data }),
    update: (data: any) => request({ url: '/gantt', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/gantt/${id}`, method: 'DELETE' }),
    submit: (id: number) => request({ url: `/gantt/${id}/submit`, method: 'POST' }),
    getProgress: (id: number) => request({ url: `/gantt/${id}/progress`, method: 'GET' }),
    updateProgress: (id: number, data: any) => request({ url: `/gantt/${id}/progress`, method: 'PUT', data }),
    getIncomeSplit: (contractId: number) => request({ url: `/gantt/income-split/${contractId}`, method: 'GET' }),
  },
  
  /** 审批待办（后端：/api/approval/todo） */
  todo: {
    list: (params: { userId: number; category?: string; bizType?: string; keyword?: string; page?: number; size?: number }) =>
      request({ url: '/approval/todo', method: 'GET', params }),
    count: (params: { userId: number }) => request({ url: '/approval/todo/count', method: 'GET', params }),
  },

  approval: {
    history: (instanceId: number) => request({ url: `/approval/${instanceId}/history`, method: 'GET' }),
    delegate: (instanceId: number, params: { fromUserId: number; toUserId: number; opinion?: string }) =>
      request({ url: `/approval/${instanceId}/delegate`, method: 'POST', params }),
  },
  
  announcement: {
    list: (params: any) => request({ url: '/system/announcement/list', method: 'GET', params }),
    get: (id: number) => request({ url: `/system/announcement/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/system/announcement', method: 'POST', data }),
    update: (data: any) => request({ url: '/system/announcement', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/system/announcement/${id}`, method: 'DELETE' }),
    submit: (id: number) => request({ url: `/system/announcement/${id}/submit`, method: 'POST' }),
    publish: (id: number) => request({ url: `/system/announcement/${id}/publish`, method: 'POST' }),
    offline: (id: number) => request({ url: `/system/announcement/${id}/offline`, method: 'POST' }),
    carousel: (params: any) => request({ url: '/system/announcement/carousel', method: 'GET', params }),
  },
  
  company: {
    search: (params: any) => request({ url: '/company/search', method: 'GET', params }),
    get: (id: number) => request({ url: `/company/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/company', method: 'POST', data }),
    update: (data: any) => request({ url: '/company', method: 'PUT', data }),
  },
  
  invoice: {
    list: (params: any) => request({ url: '/invoice/list', method: 'GET', params }),
    page: (params: any) => request({ url: '/invoice/page', method: 'GET', params }),
    summary: (params?: any) => request({ url: '/invoice/summary', method: 'GET', params }),
    get: (id: number) => request({ url: `/invoice/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/invoice', method: 'POST', data }),
    update: (data: any) => request({ url: '/invoice', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/invoice/${id}`, method: 'DELETE' }),
    verify: (id: number) => request({ url: `/invoice/${id}/verify`, method: 'POST' }),
  },
  
  payment: {
    plan: {
      list: (params: any) => request({ url: '/payment-plan/list', method: 'GET', params }),
      page: (params: any) => request({ url: '/payment-plan/page', method: 'GET', params }),
      get: (id: number) => request({ url: `/payment-plan/${id}`, method: 'GET' }),
      create: (data: any) => request({ url: '/payment-plan', method: 'POST', data }),
      update: (data: any) => request({ url: '/payment-plan', method: 'PUT', data }),
      delete: (id: number) => request({ url: `/payment-plan/${id}`, method: 'DELETE' }),
      summary: () => request({ url: '/payment-plan/summary', method: 'GET' }),
      monthly: (month: string) => request({ url: `/payment-plan/monthly/${month}`, method: 'GET' }),
      getByContract: (contractId: number) => request({ url: `/payment-plan/contracts/${contractId}`, method: 'GET' }),
      generateMonthly: (data: any) => request({ url: '/payment-plan/generate-monthly', method: 'POST', data }),
      updateStatus: (id: number, data: any) => request({ url: `/payment-plan/${id}/status`, method: 'PUT', data }),
      /** 与列表筛选一致；需 fetch + blob 下载 */
      exportCsvQuery: (params: {
        contractId?: number
        contractIds?: number[]
        projectId?: number
        planMonth?: string
        status?: number
      }) => {
        const q = new URLSearchParams()
        if (params.contractId != null) q.set('contractId', String(params.contractId))
        if (params.projectId != null) q.set('projectId', String(params.projectId))
        if (params.planMonth) q.set('planMonth', params.planMonth)
        if (params.status != null) q.set('status', String(params.status))
        if (params.contractIds?.length) {
          for (const id of params.contractIds) q.append('contractIds', String(id))
        }
        return `/api/payment-plan/export/csv?${q.toString()}`
      },
    },
  },
  
  product: {
    price: {
      baseList: (params: any) => request({ url: '/product/price/base/list', method: 'GET', params }),
      standardList: (params: any) => request({ url: '/product/price/standard/list', method: 'GET', params }),
      search: (params: any) => request({ url: '/product/price/search', method: 'GET', params }),
      history: (id: number) => request({ url: `/product/price/${id}/history`, method: 'GET' }),
      createBase: (data: any) => request({ url: '/product/price/base', method: 'POST', data }),
      updateBase: (id: number, data: any) => request({ url: `/product/price/base/${id}`, method: 'PUT', data }),
      submitBase: (id: number) => request({ url: `/product/price/base/${id}/submit`, method: 'POST' }),
      getSyncStatus: () => request({ url: '/product/price/sync/status', method: 'GET' }),
      manualSync: () => request({ url: '/product/price/sync/manual', method: 'POST' }),
    },
    inquiry: {
      list: (params: any) => request({ url: '/product/inquiry/list', method: 'GET', params }),
      page: (params: any) => request({ url: '/product/inquiry/page', method: 'GET', params }),
      get: (id: number) => request({ url: `/product/inquiry/${id}`, method: 'GET' }),
      create: (data: any) => request({ url: '/product/inquiry', method: 'POST', data }),
      update: (id: number, data: any) => request({ url: `/product/inquiry/${id}`, method: 'PUT', data }),
      submit: (id: number) => request({ url: `/product/inquiry/${id}/submit`, method: 'POST' }),
      cancel: (id: number) => request({ url: `/product/inquiry/${id}/cancel`, method: 'POST' }),
      addQuotation: (id: number, data: any) => request({ url: `/product/inquiry/${id}/quotations`, method: 'POST', data }),
      getQuotations: (id: number) => request({ url: `/product/inquiry/${id}/quotations`, method: 'GET' }),
      import: (data: any) => request({ url: '/product/inquiry/import', method: 'POST', data }),
    },
    compare: (data: any) => request({ url: '/product/compare', method: 'POST', data }),
  },
  
  cost: {
    list: (params: any) => request({ url: '/cost/list', method: 'GET', params }),
    page: (params: any) => request({ url: '/cost/page', method: 'GET', params }),
    get: (id: number) => request({ url: `/cost/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/cost', method: 'POST', data }),
    update: (data: any) => request({ url: '/cost', method: 'PUT', data }),
    summary: (params?: any) => request({ url: '/cost/summary', method: 'GET', params }),
    statistics: (params: any) => request({ url: '/cost/statistics', method: 'GET', params }),
    daily: (params: any) => request({ url: '/cost/daily', method: 'GET', params }),
    monthly: (params: any) => request({ url: '/cost/monthly', method: 'GET', params }),
    autoCollect: (data: any) => request({ url: '/cost/auto-collect', method: 'POST', data }),
  },
  
  budget: {
    list: (params: any) => request({ url: '/budget/list', method: 'GET', params }),
    page: (params: any) => request({ url: '/budget/page', method: 'GET', params }),
    get: (id: number) => request({ url: `/budget/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/budget', method: 'POST', data }),
    update: (data: any) => request({ url: '/budget', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/budget/${id}`, method: 'DELETE' }),
    summary: (params: any) => request({ url: '/budget/summary', method: 'GET', params }),
    usage: (id: number) => request({ url: `/budget/usage/${id}`, method: 'GET' }),
    adjust: (data: any) => request({ url: '/budget/adjust', method: 'POST', data }),
    transfer: (data: any) => request({ url: '/budget/transfer', method: 'POST', data }),
    warnings: (params: any) => request({ url: '/budget/warning', method: 'GET', params }),
  },
  
  report: {
    overview: (params?: any) => request({ url: '/report/overview', method: 'GET', params }),
    trend: (params?: { months?: number }) => request({ url: '/report/trend', method: 'GET', params }),
    /** 返回带查询参数的导出地址（需用 fetch + blob 下载，见自定义报表页） */
    exportCsvQuery: (params: { type: string; from: string; to: string; projectId?: number }) => {
      const q = new URLSearchParams({ type: params.type, from: params.from, to: params.to })
      if (params.projectId != null) q.set('projectId', String(params.projectId))
      return `/api/report/export/csv?${q.toString()}`
    },
  },

  paymentApply: {
    list: (params: any) => request({ url: '/payment-apply/list', method: 'GET', params }),
    page: (params: any) => request({ url: '/payment-apply/page', method: 'GET', params }),
    get: (id: number) => request({ url: `/payment-apply/${id}`, method: 'GET' }),
    create: (data: any) => request({ url: '/payment-apply', method: 'POST', data }),
    update: (data: any) => request({ url: '/payment-apply', method: 'PUT', data }),
    delete: (id: number) => request({ url: `/payment-apply/${id}`, method: 'DELETE' }),
    summary: (params?: any) => request({ url: '/payment-apply/summary', method: 'GET', params }),
    createLabor: (data: any) => request({ url: '/payment-apply/labor', method: 'POST', data }),
    createMaterial: (data: any) => request({ url: '/payment-apply/material', method: 'POST', data }),
    getByContract: (contractId: number) => request({ url: `/payment-apply/by-contract/${contractId}`, method: 'GET' }),
    submit: (id: number) => request({ url: `/payment-apply/${id}/submit`, method: 'POST' }),
    approve: (id: number, params: any) => request({ url: `/payment-apply/${id}/approve`, method: 'POST', params }),
    associateInvoice: (id: number, data: any) => request({ url: `/payment-apply/${id}/associate-invoice`, method: 'POST', data }),
  },
  
  system: {
    menu: {
      tree: () => request({ url: '/system/menu/tree', method: 'GET' }),
    },
    user: {
      list: (params?: any) => request({ url: '/system/user/list', method: 'GET', params }),
      selectList: () => request({ url: '/system/user/select-list', method: 'GET' }),
      get: (id: number) => request({ url: `/system/user/${id}`, method: 'GET' }),
      create: (data: any) => request({ url: '/system/user', method: 'POST', data }),
      update: (data: any) => request({ url: '/system/user', method: 'PUT', data }),
      delete: (id: number) => request({ url: `/system/user/${id}`, method: 'DELETE' }),
      updateRoles: (id: number, data: any) => request({ url: `/system/user/${id}/roles`, method: 'PUT', data }),
    },
    role: {
      list: (params?: any) => request({ url: '/system/role/list', method: 'GET', params }),
      selectList: () => request({ url: '/system/role/select-list', method: 'GET' }),
      get: (id: number) => request({ url: `/system/role/${id}`, method: 'GET' }),
      create: (data: any) => request({ url: '/system/role', method: 'POST', data }),
      update: (data: any) => request({ url: '/system/role', method: 'PUT', data }),
      delete: (id: number) => request({ url: `/system/role/${id}`, method: 'DELETE' }),
      getPermissions: (id: number) => request({ url: `/system/role/${id}/permissions`, method: 'GET' }),
      savePermissions: (id: number, data: any) => request({ url: `/system/role/${id}/permissions`, method: 'POST', data }),
    },
    department: {
      list: () => request({ url: '/system/department/list', method: 'GET' }),
      tree: () => request({ url: '/system/department/tree', method: 'GET' }),
      get: (id: number) => request({ url: `/system/department/${id}`, method: 'GET' }),
      create: (data: any) => request({ url: '/system/department', method: 'POST', data }),
      update: (data: any) => request({ url: '/system/department', method: 'PUT', data }),
      delete: (id: number) => request({ url: `/system/department/${id}`, method: 'DELETE' }),
    },
  },
}