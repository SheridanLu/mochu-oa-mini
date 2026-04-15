<template>
  <div class="reconciliation-page">
    <div class="page-header">
      <div class="header-left">
        <h2>收入对账管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>收入对账</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-tooltip content="按《UI 设计说明》展示列表筛选、统计与导出能力" placement="bottom">
          <el-button text type="primary" :icon="QuestionFilled">帮助</el-button>
        </el-tooltip>
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
            <el-form-item label="状态">
              <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 100%">
                <el-option label="草稿" :value="1" />
                <el-option label="待审批" :value="2" />
                <el-option label="审批中" :value="3" />
                <el-option label="已完成" :value="4" />
                <el-option label="已驳回" :value="5" />
              </el-select>
            </el-form-item>
            <el-form-item label="对账周期">
              <el-date-picker
                v-model="filterForm.period"
                type="monthrange"
                range-separator="至"
                start-placeholder="开始"
                end-placeholder="结束"
                value-format="YYYY-MM"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="创建日期">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始"
                end-placeholder="结束"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
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
              <div class="stat-label">待确认对账单</div>
              <div class="stat-value warning">{{ stats.pendingConfirm }}</div>
              <div class="stat-hint">待审批 + 审批中</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">异常对账单</div>
              <div class="stat-value danger">{{ stats.abnormalCount }}</div>
              <div class="stat-hint">已驳回或存在差异</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">当月金额总计</div>
              <div class="stat-value">{{ formatAmount(stats.monthAmountTotal) }}</div>
              <div class="stat-hint">本期对账周期为当月</div>
            </div>
          </div>
        </div>

        <el-card shadow="never" class="table-card">
          <el-table
            v-loading="loading"
            :data="tableData"
            stripe
            style="width: 100%"
            :row-class-name="rowClassName"
          >
            <el-table-column prop="statementNo" label="对账单编号" width="160" fixed />
            <el-table-column prop="projectName" label="项目名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="period" label="所属期间" width="100" />
            <el-table-column prop="contractAmount" label="合同金额" width="130" align="right">
              <template #default="{ row }">{{ formatAmount(row.contractAmount) }}</template>
            </el-table-column>
            <el-table-column prop="currentReceipt" label="当期回款" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.currentReceipt) }}</template>
            </el-table-column>
            <el-table-column prop="receivableBalance" label="应收余额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.receivableBalance) }}</template>
            </el-table-column>
            <el-table-column label="审批状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row)" size="small">{{ statusText(row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">查看</el-button>
                <el-button v-if="row.status === 1" type="primary" link @click="handleEdit(row)">编辑</el-button>
                <el-button v-if="row.status === 1" type="primary" link @click="handleSubmit(row)">提交</el-button>
                <el-button type="primary" link @click="handleExportRow(row)">导出</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'
import { api } from '@/api'

const router = useRouter()
const loading = ref(false)

const filterForm = reactive({
  projectId: undefined as number | undefined,
  contractId: undefined as number | undefined,
  status: undefined as number | undefined,
  period: [] as string[],
  dateRange: [] as string[]
})

const projectOptions = ref<any[]>([])
const contractOptions = ref<any[]>([])
const allIncomeContracts = ref<any[]>([])

const tableData = ref<any[]>([])

const stats = reactive({
  pendingConfirm: 0,
  abnormalCount: 0,
  monthAmountTotal: 0
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const num = (v: any) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const formatAmount = (amount: number | string | null | undefined) => {
  const n = num(amount)
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(n)
}

const statusText = (row: any) => {
  const s = row.status
  const map: Record<number, string> = {
    1: '草稿',
    2: '待审批',
    3: '审批中',
    4: '已完成',
    5: '已驳回'
  }
  if (isAbnormal(row) && s !== 5) return '异常'
  return map[s] || '未知'
}

const isAbnormal = (row: any) => {
  const d = num(row.differenceAmount)
  return Math.abs(d) > 0.01
}

const statusTagType = (row: any) => {
  const s = row.status
  if (s === 5 || isAbnormal(row)) return 'danger'
  if (s === 1) return 'info'
  if (s === 2) return 'warning'
  if (s === 3) return 'primary'
  if (s === 4) return 'success'
  return 'info'
}

const rowClassName = ({ row }: { row: any }) => {
  if (row.status === 5 || isAbnormal(row)) return 'row-risk'
  return ''
}

const contractLabel = (c: any) => {
  const no = c.contractNo || c.contractCode || ''
  const name = c.contractName || ''
  return [no, name].filter(Boolean).join(' ') || `合同 #${c.id}`
}

const loadProjects = async () => {
  try {
    const res: any = await api.project.list()
    if (res.code === 200) {
      projectOptions.value = res.data || []
    }
  } catch {
    projectOptions.value = []
  }
}

const loadIncomeContracts = async () => {
  try {
    const res: any = await api.contract.income.list()
    if (res.code === 200) {
      allIncomeContracts.value = res.data || []
    } else {
      allIncomeContracts.value = []
    }
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
  const proj = projectOptions.value.find((p: any) => num(p.id) === pid)
  return {
    ...row,
    projectName: proj?.projectName || proj?.name || (pid ? `项目 #${pid}` : '-')
  }
}

const fetchSummary = async () => {
  try {
    const res: any = await api.statement.summary()
    if (res.code === 200 && res.data) {
      stats.pendingConfirm = num(res.data.pendingConfirm)
      stats.abnormalCount = num(res.data.abnormalCount)
      stats.monthAmountTotal = num(res.data.monthAmountTotal)
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
    if (filterForm.status != null) params.status = filterForm.status
    if (filterForm.period?.length === 2) {
      params.periodStart = filterForm.period[0]
      params.periodEnd = filterForm.period[1]
    }
    if (filterForm.dateRange?.length === 2) {
      params.createdDateStart = filterForm.dateRange[0]
      params.createdDateEnd = filterForm.dateRange[1]
    }
    const res: any = await api.statement.page(params)
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载失败')
      return
    }
    const page = res.data || {}
    const records = page.records || []
    tableData.value = records.map(enrichRow)
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
  filterForm.projectId = undefined
  filterForm.contractId = undefined
  filterForm.status = undefined
  filterForm.period = []
  filterForm.dateRange = []
  applyContractFilter()
  pagination.page = 1
  fetchList()
}

const handleRefresh = () => {
  fetchSummary()
  fetchList()
  ElMessage.success('已刷新')
}

const handleExport = () => {
  ElMessage.info('列表导出开发中（将按当前筛选条件导出）')
}

const handleView = (row: any) => {
  router.push({ path: '/finance/reconciliation/detail', query: { id: String(row.id) } })
}

const handleEdit = (row: any) => {
  router.push({ path: '/finance/reconciliation/detail', query: { id: String(row.id) } })
}

const handleSubmit = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定提交该对账单进入审批？', '提交确认', { type: 'warning' })
    const res: any = await api.statement.submit(row.id)
    if (res.code === 200) {
      ElMessage.success('提交成功')
      handleRefresh()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    /* cancel */
  }
}

const handleExportRow = () => {
  ElMessage.info('单行导出开发中')
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
.reconciliation-page {
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

.header-left h2 {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
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
  color: #303133;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.content-main {
  padding: 0;
  overflow: visible;
}

.stats-row {
  display: flex;
  align-items: stretch;
  margin-bottom: 16px;
  gap: 12px;
}

.stats-spacer {
  flex: 1;
  min-width: 0;
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

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #c0c4cc;
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

:deep(.el-table .row-risk) {
  --el-table-tr-bg-color: #fff7f7;
}
</style>
