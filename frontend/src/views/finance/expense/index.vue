<template>
  <div class="expense-page">
    <div class="page-header">
      <div class="header-left">
        <h2>日常报销管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>日常报销</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleExport">导出</el-button>
        <el-button type="primary" :loading="loading" @click="handleRefresh">刷新</el-button>
        <el-button type="primary" @click="handleCreate">新建报销单</el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" class="tip-alert" show-icon>
      状态：1 草稿 · 2 待审批 · 3 审批中 · 4 已通过 · 5 已驳回 · 6 已支付。提交前将按当月部门/项目预算校验。
    </el-alert>

    <el-container class="main-layout">
      <el-aside width="280px" class="filter-aside">
        <el-card shadow="never" class="filter-card">
          <template #header>
            <span class="filter-title">筛选条件</span>
          </template>
          <el-form label-position="top" :model="filterForm" class="filter-form">
            <el-form-item label="部门">
              <el-select
                v-model="filterForm.departmentId"
                placeholder="全部部门"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option v-for="d in departmentOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="项目">
              <el-select v-model="filterForm.projectId" placeholder="全部项目" clearable filterable style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName || p.name" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="费用类别">
              <el-select v-model="filterForm.expenseCategory" placeholder="全部" clearable style="width: 100%">
                <el-option label="差旅费" :value="1" />
                <el-option label="招待费" :value="2" />
                <el-option label="办公费" :value="3" />
                <el-option label="交通费" :value="4" />
                <el-option label="其他" :value="5" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 100%">
                <el-option label="草稿" :value="1" />
                <el-option label="待审批" :value="2" />
                <el-option label="审批中" :value="3" />
                <el-option label="已通过" :value="4" />
                <el-option label="已驳回" :value="5" />
                <el-option label="已支付" :value="6" />
              </el-select>
            </el-form-item>
            <el-form-item label="创建起">
              <el-date-picker
                v-model="filterForm.createdBegin"
                type="date"
                placeholder="开始"
                value-format="YYYY-MM-DD"
                clearable
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="创建止">
              <el-date-picker
                v-model="filterForm.createdEnd"
                type="date"
                placeholder="结束"
                value-format="YYYY-MM-DD"
                clearable
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
              <div class="stat-label">待审批 / 审批中</div>
              <div class="stat-value warning">{{ stats.pendingApproval }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">已支付</div>
              <div class="stat-value success">{{ stats.paid }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">预算预警（≥80%）</div>
              <div class="stat-value danger">{{ stats.budgetWarning }}</div>
            </div>
          </div>
        </div>

        <el-card shadow="never" class="table-card">
          <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
            <el-table-column prop="reportNo" label="报销单编号" width="150" show-overflow-tooltip />
            <el-table-column prop="reporterName" label="报销人" width="100" />
            <el-table-column prop="departmentName" label="部门" width="120" show-overflow-tooltip />
            <el-table-column prop="projectName" label="项目" min-width="120" show-overflow-tooltip />
            <el-table-column label="费用类别" width="100" align="center">
              <template #default="{ row }">{{ expenseCategoryText(row.expenseCategory) }}</template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column prop="expenseDate" label="费用日期" width="110" />
            <el-table-column prop="status" label="状态" width="96" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="165" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">查看</el-button>
                <el-button v-if="row.status === 1" type="primary" link @click="handleEdit(row)">编辑</el-button>
                <el-button v-if="row.status === 1" type="primary" link @click="handleSubmit(row)">提交</el-button>
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

    <el-dialog v-model="warningDialogVisible" title="预算校验" width="420px" :close-on-click-modal="false">
      <div class="warning-content">
        <div class="warning-icon" :class="warningLevel">
          <el-icon :size="48"><WarningFilled /></el-icon>
        </div>
        <div class="warning-text">
          <p v-if="warningLevel === 'red'" class="warning-red">{{ warningMessage || '预算已超支，无法提交报销单。' }}</p>
          <p v-else class="warning-yellow">{{ warningMessage || '预算占用已超过 80%，请确认是否继续提交。' }}</p>
          <p class="warning-detail">项目：{{ warningData.projectName }} | 报销金额：{{ formatAmount(warningData.amount) }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="warningDialogVisible = false">取消</el-button>
        <el-button v-if="warningLevel === 'yellow'" type="warning" @click="confirmSubmit">确认提交</el-button>
        <el-button v-else type="danger" disabled>无法提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import { api } from '@/api'

const router = useRouter()
const loading = ref(false)

const filterForm = reactive({
  departmentId: undefined as number | undefined,
  projectId: undefined as number | undefined,
  expenseCategory: undefined as number | undefined,
  status: undefined as number | undefined,
  createdBegin: '' as string,
  createdEnd: '' as string
})

const departmentOptions = ref<any[]>([])
const projectOptions = ref<any[]>([])

const stats = reactive({
  pendingApproval: 0,
  paid: 0,
  budgetWarning: 0
})

const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 20, total: 0 })

const warningDialogVisible = ref(false)
const warningLevel = ref<'yellow' | 'red'>('yellow')
const warningMessage = ref('')
const warningData = reactive({ projectName: '', amount: 0 })
const pendingSubmitRow = ref<any>(null)

const num = (v: any) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const formatAmount = (amount: number | string | null | undefined) => {
  const n = num(amount)
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(n)
}

const expenseCategoryText = (c: number | null | undefined) => {
  const m = ['', '差旅费', '招待费', '办公费', '交通费', '其他']
  return m[num(c)] || '—'
}

const getStatusText = (status: number) => {
  const m: Record<number, string> = {
    1: '草稿',
    2: '待审批',
    3: '审批中',
    4: '已通过',
    5: '已驳回',
    6: '已支付'
  }
  return m[status] || '—'
}

const getStatusType = (status: number) => {
  const m: Record<number, string> = {
    1: 'info',
    2: 'warning',
    3: 'warning',
    4: 'success',
    5: 'danger',
    6: 'success'
  }
  return m[status] || 'info'
}

const loadDepartments = async () => {
  try {
    const res: any = await api.system.department.list()
    departmentOptions.value = res.code === 200 ? res.data || [] : []
  } catch {
    departmentOptions.value = []
  }
}

const loadProjects = async () => {
  try {
    const res: any = await api.project.list()
    projectOptions.value = res.code === 200 ? res.data || [] : []
  } catch {
    projectOptions.value = []
  }
}

const summaryParams = () => {
  const p: Record<string, any> = {}
  if (filterForm.departmentId != null) p.departmentId = filterForm.departmentId
  if (filterForm.projectId != null) p.projectId = filterForm.projectId
  if (filterForm.expenseCategory != null) p.expenseCategory = filterForm.expenseCategory
  if (filterForm.createdBegin) p.createdBegin = filterForm.createdBegin
  if (filterForm.createdEnd) p.createdEnd = filterForm.createdEnd
  return p
}

const listParams = () => ({
  ...summaryParams(),
  pageNum: pagination.page,
  pageSize: pagination.size,
  ...(filterForm.status != null ? { status: filterForm.status } : {})
})

const fetchSummary = async () => {
  try {
    const res: any = await api.expense.report.summary(summaryParams())
    if (res.code === 200 && res.data) {
      stats.pendingApproval = num(res.data.pendingApproval)
      stats.paid = num(res.data.paid)
      stats.budgetWarning = num(res.data.budgetWarning)
    }
  } catch {
    /* ignore */
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await api.expense.report.page(listParams())
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载失败')
      return
    }
    const page = res.data || {}
    tableData.value = (page.records || []).map((row: any) => ({
      ...row,
      createdAt: formatDateTime(row.createdAt)
    }))
    pagination.total = num(page.total)
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const formatDateTime = (v: any) => {
  if (v == null) return '—'
  if (typeof v === 'string') return v.replace('T', ' ').slice(0, 19)
  return String(v)
}

const handleSearch = () => {
  pagination.page = 1
  fetchSummary()
  fetchList()
}

const handleReset = () => {
  filterForm.departmentId = undefined
  filterForm.projectId = undefined
  filterForm.expenseCategory = undefined
  filterForm.status = undefined
  filterForm.createdBegin = ''
  filterForm.createdEnd = ''
  pagination.page = 1
  fetchSummary()
  fetchList()
}

const handleRefresh = () => {
  fetchSummary()
  fetchList()
  ElMessage.success('已刷新')
}

const handleExport = () => ElMessage.info('导出功能开发中')

const handleCreate = () => router.push('/finance/expense/edit')

const handleView = (row: any) => router.push(`/finance/expense/edit?id=${row.id}&mode=view`)

const handleEdit = (row: any) => router.push(`/finance/expense/edit?id=${row.id}`)

const submitRemote = async (row: any) => {
  const res: any = await api.expense.report.submit(row.id)
  if (res.code !== 200) {
    ElMessage.error(res.message || '提交失败')
    return
  }
  ElMessage.success('提交成功')
  warningDialogVisible.value = false
  pendingSubmitRow.value = null
  await fetchSummary()
  await fetchList()
}

const handleSubmit = async (row: any) => {
  pendingSubmitRow.value = row
  warningData.projectName = row.projectName || '—'
  warningData.amount = num(row.totalAmount)

  if (!row.departmentId) {
    try {
      await ElMessageBox.confirm('未维护部门信息，将直接提交（不做预算校验）。是否继续？', '提示', {
        type: 'warning'
      })
      await submitRemote(row)
    } catch {
      /* cancel */
    }
    return
  }

  try {
    const res: any = await api.expense.report.budgetCheck({
      departmentId: row.departmentId,
      projectId: row.projectId,
      amount: row.totalAmount
    })
    if (res.code !== 200) {
      ElMessage.error(res.message || '预算校验失败')
      return
    }
    const data = res.data || {}
    if (data.status === 'no_budget') {
      try {
        await ElMessageBox.confirm(String(data.message || '未找到预算配置') + '，是否仍要提交？', '提示', { type: 'warning' })
        await submitRemote(row)
      } catch {
        /* cancel */
      }
      return
    }
    if (data.canSubmit === false) {
      warningLevel.value = 'red'
      warningMessage.value = String(data.warning || '预算已超支，无法提交')
      warningDialogVisible.value = true
      return
    }
    if (data.status === 'warning') {
      warningLevel.value = 'yellow'
      warningMessage.value = String(data.warning || '')
      warningDialogVisible.value = true
      return
    }
    await submitRemote(row)
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  }
}

const confirmSubmit = async () => {
  const row = pendingSubmitRow.value
  if (!row) return
  await submitRemote(row)
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
  await loadDepartments()
  await loadProjects()
  await fetchSummary()
  await fetchList()
})
</script>

<style scoped>
.expense-page {
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
.stat-value.success {
  color: #67c23a;
}
.stat-value.warning {
  color: #e6a23c;
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
.warning-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px 0;
}
.warning-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
}
.warning-icon.yellow {
  background: #fdf6ec;
  color: #e6a23c;
}
.warning-icon.red {
  background: #fef0f0;
  color: #f56c6c;
}
.warning-text {
  flex: 1;
}
.warning-text p {
  margin: 0 0 8px 0;
  font-size: 15px;
}
.warning-red {
  color: #f56c6c;
  font-weight: 600;
}
.warning-yellow {
  color: #e6a23c;
  font-weight: 600;
}
.warning-detail {
  color: #909399;
  font-size: 13px;
}
</style>
