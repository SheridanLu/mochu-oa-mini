<template>
  <div class="payment-plan-page">
    <div class="page-header">
      <div class="header-left">
        <h2>付款计划管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>付款计划</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button :loading="exporting" @click="handleExport">导出 CSV</el-button>
        <el-button type="primary" :loading="loading" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-alert type="warning" :closable="false" class="tip-alert" show-icon>
      支出合同付款计划仅供内部查看与执行跟踪；导出能力受权限控制。
    </el-alert>

    <el-container class="main-layout">
      <el-aside width="280px" class="filter-aside">
        <el-card shadow="never" class="filter-card">
          <template #header>
            <span class="filter-title">筛选条件</span>
          </template>
          <el-form label-position="top" :model="filterForm" class="filter-form">
            <el-form-item label="计划月份">
              <el-date-picker
                v-model="filterForm.planMonth"
                type="month"
                placeholder="全部月份"
                value-format="YYYY-MM"
                clearable
                style="width: 100%"
                @change="handleFilterChange"
              />
            </el-form-item>
            <el-form-item label="项目">
              <el-select
                v-model="filterForm.projectId"
                placeholder="全部项目"
                clearable
                filterable
                style="width: 100%"
                @change="onProjectChange"
              >
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName || p.name" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="支出合同">
              <el-select
                v-model="filterForm.contractId"
                placeholder="全部合同"
                clearable
                filterable
                style="width: 100%"
                @change="handleFilterChange"
              >
                <el-option v-for="c in contractOptions" :key="c.id" :label="contractLabel(c)" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="款项类型">
              <el-select v-model="filterForm.paymentType" placeholder="全部" clearable style="width: 100%" @change="onPaymentTypeChange">
                <el-option label="材料款" :value="1" />
                <el-option label="设备款" :value="2" />
                <el-option label="人工费" :value="3" />
                <el-option label="其他" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="计划状态">
              <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 100%" @change="handleFilterChange">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width: 100%" @click="handleSearch">查询</el-button>
              <el-button style="width: 100%; margin-left: 0; margin-top: 8px" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-aside>

      <el-main class="content-main">
        <div class="stats-row">
          <div class="stats-spacer" />
          <div class="stat-cards">
            <div class="stat-card">
              <div class="stat-label">有效计划条数</div>
              <div class="stat-value primary">{{ stats.enabledCount }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">本月计划付款</div>
              <div class="stat-value">{{ formatAmount(stats.monthPlanAmount) }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">7 日内待付笔数</div>
              <div class="stat-value warning">{{ stats.dueWithin7Days }}</div>
            </div>
          </div>
        </div>

        <el-card shadow="never" class="table-card">
          <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
            <el-table-column prop="contractNo" label="合同编号" width="130" show-overflow-tooltip />
            <el-table-column prop="contractName" label="合同名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="projectName" label="项目" min-width="120" show-overflow-tooltip />
            <el-table-column prop="paymentTermName" label="付款条款" min-width="140" show-overflow-tooltip />
            <el-table-column prop="plannedAmount" label="计划金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.plannedAmount) }}</template>
            </el-table-column>
            <el-table-column prop="plannedPaymentDate" label="计划付款日" width="120" />
            <el-table-column prop="paymentCondition" label="付款条件" min-width="160" show-overflow-tooltip />
            <el-table-column label="状态" width="88" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="168" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">查看</el-button>
                <el-button
                  v-if="row.status === 1"
                  type="warning"
                  link
                  :loading="statusSavingId === row.id"
                  @click="handleToggleStatus(row, 0)"
                >
                  停用
                </el-button>
                <el-button
                  v-else
                  type="success"
                  link
                  :loading="statusSavingId === row.id"
                  @click="handleToggleStatus(row, 1)"
                >
                  启用
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.size"
              :page-sizes="[20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-main>
    </el-container>

    <el-dialog v-model="detailVisible" title="付款计划详情" width="560px" destroy-on-close @closed="clearDetail">
      <el-descriptions v-if="detailPlan" :column="1" border>
        <el-descriptions-item label="合同编号">{{ detailContract?.contractNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ detailContract?.contractName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="项目">{{ detailContract?.projectName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="付款条款">{{ detailPlan.paymentTermName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="计划金额">{{ formatAmount(detailPlan.plannedAmount) }}</el-descriptions-item>
        <el-descriptions-item label="计划付款日">{{ detailPlan.plannedPaymentDate ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="付款条件">{{ detailPlan.paymentCondition || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailPlan.status === 1 ? '启用' : '停用' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="!detailContractId" @click="goExpenseContract">打开支出合同</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const exporting = ref(false)
const statusSavingId = ref<number | null>(null)

const detailVisible = ref(false)
const detailPlan = ref<any>(null)
const detailContract = ref<any>(null)
const detailContractId = computed(() => {
  const c = detailContract.value
  const p = detailPlan.value
  if (c?.id != null) return num(c.id)
  if (p?.contractId != null) return num(p.contractId)
  return 0
})

const clearDetail = () => {
  detailPlan.value = null
  detailContract.value = null
}

const goExpenseContract = () => {
  const id = detailContractId.value
  if (!id) return
  detailVisible.value = false
  router.push({ path: '/contract/edit', query: { id: String(id), expense: '1' } })
}

const filterForm = reactive({
  planMonth: '' as string,
  projectId: undefined as number | undefined,
  contractId: undefined as number | undefined,
  paymentType: undefined as number | undefined,
  status: undefined as number | undefined
})

const projectOptions = ref<any[]>([])
const allExpenseContracts = ref<any[]>([])
const contractOptions = ref<any[]>([])

const stats = reactive({
  enabledCount: 0,
  monthPlanAmount: 0,
  dueWithin7Days: 0
})

const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 20, total: 0 })

const num = (v: any) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const formatAmount = (amount: number | string | null | undefined) => {
  const n = num(amount)
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(n)
}

const contractLabel = (c: any) => {
  const no = c.contractNo || ''
  const name = c.contractName || ''
  return [no, name].filter(Boolean).join(' ') || `合同 #${c.id}`
}

const syncQuery = () => {
  const q: Record<string, string> = {
    page: String(pagination.page),
    size: String(pagination.size)
  }
  if (filterForm.planMonth) q.planMonth = filterForm.planMonth
  if (filterForm.projectId != null) q.projectId = String(filterForm.projectId)
  if (filterForm.contractId != null) q.contractId = String(filterForm.contractId)
  if (filterForm.paymentType != null) q.paymentType = String(filterForm.paymentType)
  if (filterForm.status != null) q.status = String(filterForm.status)
  const current = route.query
  const keys = new Set([...Object.keys(q), ...Object.keys(current)])
  let changed = false
  for (const k of keys) {
    const a = q[k]
    const b = typeof current[k] === 'string' ? current[k] : undefined
    if (a !== b) {
      changed = true
      break
    }
  }
  if (changed) {
    router.replace({ path: '/finance/payment-plan', query: q })
  }
}

const loadProjects = async () => {
  try {
    const res: any = await api.project.list()
    if (res.code === 200) projectOptions.value = res.data || []
  } catch {
    projectOptions.value = []
  }
}

const loadExpenseContracts = async () => {
  try {
    const res: any = await api.contract.expense.list()
    allExpenseContracts.value = res.code === 200 ? res.data || [] : []
  } catch {
    allExpenseContracts.value = []
  }
  applyContractFilter()
}

const applyContractFilter = () => {
  let list = allExpenseContracts.value
  const pid = filterForm.projectId
  if (pid != null) {
    list = list.filter((c: any) => num(c.projectId) === pid)
  }
  const pt = filterForm.paymentType
  if (pt != null) {
    list = list.filter((c: any) => num(c.paymentType) === pt)
  }
  contractOptions.value = list
  if (filterForm.contractId != null) {
    const exists = contractOptions.value.some((c: any) => num(c.id) === num(filterForm.contractId))
    if (!exists) filterForm.contractId = undefined
  }
}

const handleFilterChange = () => {
  pagination.page = 1
  fetchList()
}

const onProjectChange = () => {
  applyContractFilter()
  handleFilterChange()
}

const onPaymentTypeChange = () => {
  applyContractFilter()
  handleFilterChange()
}

const enrichRow = (row: any) => {
  const cid = num(row.contractId)
  const c = allExpenseContracts.value.find((x: any) => num(x.id) === cid)
  return {
    ...row,
    contractNo: c?.contractNo || '—',
    contractName: c?.contractName || '—',
    projectName: c?.projectName || '—',
    plannedPaymentDate: row.plannedPaymentDate || '—'
  }
}

const fetchSummary = async () => {
  try {
    const res: any = await api.payment.plan.summary()
    if (res.code === 200 && res.data) {
      stats.enabledCount = num(res.data.enabledCount)
      stats.monthPlanAmount = num(res.data.monthPlanAmount)
      stats.dueWithin7Days = num(res.data.dueWithin7Days)
    }
  } catch {
    /* ignore */
  }
}

const fetchList = async () => {
  loading.value = true
  syncQuery()
  try {
    const params: Record<string, any> = {
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    if (filterForm.contractId != null) {
      params.contractId = filterForm.contractId
    } else if (filterForm.paymentType != null) {
      const ids = contractOptions.value.map((c: any) => num(c.id)).filter((id) => id > 0)
      if (ids.length === 0) {
        tableData.value = []
        pagination.total = 0
        loading.value = false
        return
      }
      params.contractIds = ids
    } else if (filterForm.projectId != null) {
      params.projectId = filterForm.projectId
    }
    if (filterForm.planMonth) params.planMonth = filterForm.planMonth
    if (filterForm.status != null) params.status = filterForm.status

    const res: any = await api.payment.plan.page(params)
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载失败')
      return
    }
    const page = res.data || {}
    tableData.value = (page.records || []).map(enrichRow)
    pagination.total = num(page.total)
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

const handleReset = () => {
  filterForm.planMonth = ''
  filterForm.projectId = undefined
  filterForm.contractId = undefined
  filterForm.paymentType = undefined
  filterForm.status = undefined
  applyContractFilter()
  pagination.page = 1
  fetchList()
}

const handleRefresh = () => {
  fetchSummary()
  fetchList()
  ElMessage.success('已刷新')
}

function buildExportParams(): Parameters<typeof api.payment.plan.exportCsvQuery>[0] {
  const params: Parameters<typeof api.payment.plan.exportCsvQuery>[0] = {}
  if (filterForm.contractId != null) {
    params.contractId = filterForm.contractId
  } else if (filterForm.paymentType != null) {
    const ids = contractOptions.value.map((c: any) => num(c.id)).filter((id) => id > 0)
    if (ids.length === 0) return {}
    params.contractIds = ids
  } else if (filterForm.projectId != null) {
    params.projectId = filterForm.projectId
  }
  if (filterForm.planMonth) params.planMonth = filterForm.planMonth
  if (filterForm.status != null) params.status = filterForm.status
  return params
}

const handleExport = async () => {
  const p = buildExportParams()
  if (filterForm.paymentType != null && !p.contractIds?.length) {
    ElMessage.warning('当前筛选下无合同，无法导出')
    return
  }
  exporting.value = true
  try {
    const token = localStorage.getItem('token')
    const url = api.payment.plan.exportCsvQuery(p)
    const res = await fetch(url, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (res.status === 401) {
      localStorage.removeItem('token')
      ElMessage.error('登录已过期，请重新登录')
      await router.push('/login')
      return
    }
    if (!res.ok) {
      const text = await res.text()
      let msg = text.trim() || `导出失败（${res.status}）`
      try {
        const j = JSON.parse(text) as { message?: string }
        if (j.message) msg = j.message
      } catch {
        /* 非 JSON */
      }
      throw new Error(msg)
    }
    const blob = await res.blob()
    const href = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = href
    a.download = 'payment-plan.csv'
    a.click()
    URL.revokeObjectURL(href)
    ElMessage.success('已开始下载')
  } catch (e: any) {
    ElMessage.error(e.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

const handleView = async (row: any) => {
  const id = num(row.id)
  if (!id) return
  try {
    const res: any = await api.payment.plan.get(id)
    if (res.code !== 200 || !res.data?.plan) {
      ElMessage.error(res.message || '加载详情失败')
      return
    }
    detailPlan.value = res.data.plan
    detailContract.value = res.data.contract || null
    detailVisible.value = true
  } catch (e: any) {
    ElMessage.error(e.message || '加载详情失败')
  }
}

const handleToggleStatus = async (row: any, nextStatus: 0 | 1) => {
  const id = num(row.id)
  if (!id) return
  if (nextStatus === 0) {
    try {
      await ElMessageBox.confirm('停用后该计划不再计入有效统计，确定停用？', '停用付款计划', {
        type: 'warning',
        confirmButtonText: '停用',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }
  }
  statusSavingId.value = id
  try {
    const res: any = await api.payment.plan.updateStatus(id, { status: nextStatus })
    if (res.code === 200) {
      ElMessage.success(nextStatus === 1 ? '已启用' : '已停用')
      await fetchSummary()
      await fetchList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    statusSavingId.value = null
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  fetchList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchList()
}

onMounted(async () => {
  const q = route.query
  if (typeof q.planMonth === 'string') filterForm.planMonth = q.planMonth
  if (typeof q.projectId === 'string') {
    const pid = Number(q.projectId)
    if (Number.isFinite(pid) && pid > 0) filterForm.projectId = pid
  }
  if (typeof q.contractId === 'string') {
    const cid = Number(q.contractId)
    if (Number.isFinite(cid) && cid > 0) filterForm.contractId = cid
  }
  if (typeof q.paymentType === 'string') {
    const pt = Number(q.paymentType)
    if (Number.isFinite(pt)) filterForm.paymentType = pt
  }
  if (typeof q.status === 'string') {
    const st = Number(q.status)
    if (st === 0 || st === 1) filterForm.status = st
  }
  if (typeof q.page === 'string') {
    const p = Number(q.page)
    if (Number.isFinite(p) && p > 0) pagination.page = p
  }
  if (typeof q.size === 'string') {
    const s = Number(q.size)
    if (Number.isFinite(s) && s > 0) pagination.size = s
  }
  await loadProjects()
  await loadExpenseContracts()
  // 若 URL 中合同不在当前筛选后的可选集，则清空
  if (filterForm.contractId != null) {
    const exists = contractOptions.value.some((c: any) => num(c.id) === num(filterForm.contractId))
    if (!exists) filterForm.contractId = undefined
  }
  await fetchSummary()
  await fetchList()
})
</script>

<style scoped>
.payment-plan-page {
  padding: 16px 20px 24px;
  min-height: calc(100vh - 100px);
  background: #f5f7fa;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.page-header h2 {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 600;
}
.tip-alert {
  margin-bottom: 16px;
  border-radius: 8px;
}
.main-layout {
  background: transparent;
  align-items: stretch;
}
.filter-aside {
  margin-right: 16px;
}
.filter-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.filter-title {
  font-weight: 600;
}
.filter-form :deep(.el-form-item) {
  margin-bottom: 14px;
}
.content-main {
  padding: 0;
}
.stats-row {
  display: flex;
  margin-bottom: 16px;
}
.stats-spacer {
  flex: 1;
}
.stat-cards {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.stat-card {
  width: 200px;
  background: #fff;
  border-radius: 8px;
  padding: 16px 18px;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.stat-label {
  font-size: 13px;
  color: #909399;
}
.stat-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.stat-value.primary {
  color: #409eff;
}
.stat-value.warning {
  color: #e6a23c;
}
.table-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}
</style>
