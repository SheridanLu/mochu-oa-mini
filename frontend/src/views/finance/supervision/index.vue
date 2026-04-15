<template>
  <div class="supervision-page">
    <div class="page-header">
      <div class="header-left">
        <h2>回款督办管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>回款督办</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleGenerate">生成督办计划</el-button>
        <el-button @click="handleExport">导出</el-button>
        <el-button type="primary" :loading="loading" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-container class="main-layout">
      <el-aside width="280px" class="filter-aside">
        <el-card shadow="never" class="filter-card">
          <template #header>
            <span class="filter-title">筛选条件</span>
          </template>
          <el-form label-position="top" :model="filterForm" class="filter-form">
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
            <el-form-item label="合同">
              <el-select v-model="filterForm.contractId" placeholder="全部合同" clearable filterable style="width: 100%">
                <el-option v-for="c in contractOptions" :key="c.id" :label="contractLabel(c)" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="优先级">
              <el-select v-model="filterForm.priority" placeholder="全部" clearable style="width: 100%">
                <el-option label="高" :value="1" />
                <el-option label="中" :value="2" />
                <el-option label="低" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="督办状态">
              <el-select v-model="filterForm.supervisionStatus" placeholder="全部" clearable style="width: 100%">
                <el-option label="待接收" :value="1" />
                <el-option label="督办中" :value="2" />
                <el-option label="已完成" :value="3" />
                <el-option label="已终止" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="审批状态">
              <el-select v-model="filterForm.approvalStatus" placeholder="全部" clearable style="width: 100%">
                <el-option label="草稿" :value="1" />
                <el-option label="待审批" :value="2" />
                <el-option label="审批中" :value="3" />
                <el-option label="已通过" :value="4" />
                <el-option label="已驳回" :value="5" />
              </el-select>
            </el-form-item>
            <el-form-item label="超期等级">
              <el-select v-model="filterForm.overdueLevel" placeholder="全部" clearable style="width: 100%">
                <el-option label="已超期" value="warning" />
                <el-option label="严重超期(>7天)" value="serious" />
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
              <div class="stat-label">督办中</div>
              <div class="stat-value primary">{{ stats.inProgress }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">逾期</div>
              <div class="stat-value danger">{{ stats.overdue }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">总缺口金额</div>
              <div class="stat-value">{{ formatAmount(stats.gapTotal) }}</div>
            </div>
          </div>
        </div>

        <el-card shadow="never" class="table-card">
          <el-table
            v-loading="loading"
            :data="tableData"
            stripe
            style="width: 100%"
            :row-class-name="supervisionRowClass"
          >
            <el-table-column prop="planNo" label="计划编号" width="150" fixed />
            <el-table-column prop="projectName" label="项目" min-width="130" show-overflow-tooltip />
            <el-table-column prop="contractNo" label="合同" width="120" show-overflow-tooltip />
            <el-table-column prop="ganttNode" label="节点" min-width="120" show-overflow-tooltip />
            <el-table-column prop="gapAmount" label="回款缺口" width="120" align="right">
              <template #default="{ row }">
                <span :class="gapClass(row.gapAmount)">{{ formatAmount(row.gapAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="完成率" width="110" align="center">
              <template #default="{ row }">
                <el-progress :percentage="completionPercent(row)" :color="getProgressColor(completionPercent(row))" :stroke-width="10" />
              </template>
            </el-table-column>
            <el-table-column prop="overdueDays" label="逾期天数" width="96" align="center">
              <template #default="{ row }">
                <span v-if="(row.overdueDays || 0) > 0" class="overdue-text">{{ row.overdueDays }} 天</span>
                <span v-else class="muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="优先级" width="88" align="center">
              <template #default="{ row }">
                <el-tag :type="getPriorityType(row.priority)" size="small">{{ getPriorityText(row.priority) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审批状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ approvalStatusText(row.approvalStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="督办状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getSupervisionStatusType(row.supervisionStatus)" size="small">
                  {{ getSupervisionStatusText(row.supervisionStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">查看</el-button>
                <el-button type="primary" link @click="handleApprove(row)">审批</el-button>
                <el-button type="primary" link @click="handleAssign(row)">转办</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const router = useRouter()
const loading = ref(false)

const filterForm = reactive({
  projectId: undefined as number | undefined,
  contractId: undefined as number | undefined,
  priority: undefined as number | undefined,
  supervisionStatus: undefined as number | undefined,
  approvalStatus: undefined as number | undefined,
  overdueLevel: '' as string
})

const projectOptions = ref<any[]>([])
const contractOptions = ref<any[]>([])
const allIncomeContracts = ref<any[]>([])

const stats = reactive({
  inProgress: 0,
  overdue: 0,
  gapTotal: 0
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
  const no = c.contractNo || c.contractCode || ''
  const name = c.contractName || ''
  return [no, name].filter(Boolean).join(' ') || `合同 #${c.id}`
}

const loadProjects = async () => {
  try {
    const res: any = await api.project.list()
    if (res.code === 200) projectOptions.value = res.data || []
  } catch {
    projectOptions.value = []
  }
}

const loadIncomeContracts = async () => {
  try {
    const res: any = await api.contract.income.list()
    allIncomeContracts.value = res.code === 200 ? res.data || [] : []
  } catch {
    allIncomeContracts.value = []
  }
  applyContractFilter()
}

const applyContractFilter = () => {
  const pid = filterForm.projectId
  if (!pid) {
    contractOptions.value = allIncomeContracts.value
    return
  }
  contractOptions.value = allIncomeContracts.value.filter((c: any) => num(c.projectId) === pid)
}

const onProjectChange = () => {
  filterForm.contractId = undefined
  applyContractFilter()
}

const enrichRow = (row: any) => {
  const pid = num(row.projectId)
  const cid = num(row.contractId)
  const proj = projectOptions.value.find((p: any) => num(p.id) === pid)
  const cont = allIncomeContracts.value.find((c: any) => num(c.id) === cid)
  return {
    ...row,
    projectName: proj?.projectName || proj?.name || (pid ? `项目 #${pid}` : '—'),
    contractNo: cont?.contractNo || cont?.contractName || (cid ? `#${cid}` : '—')
  }
}

const fetchSummary = async () => {
  try {
    const res: any = await api.paymentSupervision.summary()
    if (res.code === 200 && res.data) {
      stats.inProgress = num(res.data.inProgress)
      stats.overdue = num(res.data.overdue)
      stats.gapTotal = num(res.data.gapTotal)
    }
  } catch {
    /* ignore */
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    if (filterForm.projectId != null) params.projectId = filterForm.projectId
    if (filterForm.contractId != null) params.contractId = filterForm.contractId
    if (filterForm.priority != null) params.priority = filterForm.priority
    if (filterForm.supervisionStatus != null) params.supervisionStatus = filterForm.supervisionStatus
    if (filterForm.approvalStatus != null) params.approvalStatus = filterForm.approvalStatus
    if (filterForm.overdueLevel) params.overdueLevel = filterForm.overdueLevel

    const res: any = await api.paymentSupervision.page(params)
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

const completionPercent = (row: any) => {
  const v = num(row.completionRate)
  if (v <= 0) return 0
  if (v <= 1) return Math.round(v * 100)
  return Math.min(100, Math.round(v))
}

const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

const gapClass = (amount: number) => {
  const a = num(amount)
  if (a > 500000) return 'amount-danger'
  if (a > 200000) return 'amount-warning'
  return ''
}

const supervisionRowClass = ({ row }: { row: any }) => {
  const d = num(row.overdueDays)
  if (d > 7) return 'row-serious'
  if (d > 0) return 'row-warning'
  return ''
}

const getPriorityType = (priority: number) => ['danger', 'warning', 'info'][num(priority) - 1] || 'info'
const getPriorityText = (priority: number) => ['高', '中', '低'][num(priority) - 1] || '—'

const getSupervisionStatusType = (status: number) => {
  const t: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'success', 4: 'danger' }
  return t[num(status)] || 'info'
}

const getSupervisionStatusText = (status: number) =>
  ['待接收', '督办中', '已完成', '已终止'][num(status) - 1] || '—'

const approvalStatusText = (s: number) =>
  ['', '草稿', '待审批', '审批中', '已通过', '已驳回'][num(s)] || '—'

const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

const handleReset = () => {
  filterForm.projectId = undefined
  filterForm.contractId = undefined
  filterForm.priority = undefined
  filterForm.supervisionStatus = undefined
  filterForm.approvalStatus = undefined
  filterForm.overdueLevel = ''
  applyContractFilter()
  pagination.page = 1
  fetchList()
}

const handleRefresh = () => {
  fetchSummary()
  fetchList()
  ElMessage.success('已刷新')
}

const handleExport = () => ElMessage.info('导出功能开发中')

const handleGenerate = () => ElMessage.info('生成督办计划功能开发中')

const handleView = (row: any) => {
  router.push({ path: '/finance/supervision/detail', query: { id: String(row.id) } })
}

const handleApprove = () => {
  ElMessage.info('审批请跳转详情或审批中心')
}

const handleAssign = () => {
  ElMessage.info('转办功能开发中')
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
  await loadProjects()
  await loadIncomeContracts()
  await fetchSummary()
  await fetchList()
})
</script>

<style scoped>
.supervision-page {
  padding: 16px 20px 24px;
  min-height: calc(100vh - 100px);
  background: #f5f7fa;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 600;
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
.stat-value.danger {
  color: #f56c6c;
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
.overdue-text {
  color: #f56c6c;
  font-weight: 600;
}
.muted {
  color: #c0c4cc;
}
.amount-danger {
  color: #f56c6c;
  font-weight: 600;
}
.amount-warning {
  color: #e6a23c;
  font-weight: 600;
}
:deep(.el-table .row-warning) {
  background: #fff8f0 !important;
}
:deep(.el-table .row-serious) {
  background: #fff2f0 !important;
}
</style>
