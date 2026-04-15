<template>
  <div class="budget-page">
    <div class="page-header">
      <div class="header-left">
        <h2>部门预算</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>部门预算</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openCreate">新建预算</el-button>
        <el-button :loading="loading" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

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
            <el-form-item label="年度">
              <el-date-picker
                v-model="filterForm.year"
                type="year"
                placeholder="全部"
                value-format="YYYY"
                clearable
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="月份">
              <el-select v-model="filterForm.month" placeholder="全部" clearable style="width: 100%">
                <el-option v-for="m in 12" :key="m" :label="`${m} 月`" :value="m" />
              </el-select>
            </el-form-item>
            <el-form-item label="预算类型">
              <el-select v-model="filterForm.budgetType" placeholder="全部" clearable style="width: 100%">
                <el-option label="年度预算" :value="1" />
                <el-option label="月度预算" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 100%">
                <el-option label="生效" :value="1" />
                <el-option label="调整中" :value="2" />
                <el-option label="已冻结" :value="3" />
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
              <div class="stat-label">预算条数</div>
              <div class="stat-value primary">{{ stats.recordCount }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">预算总额</div>
              <div class="stat-value">{{ formatAmount(stats.totalBudget) }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">已使用 / 占用率</div>
              <div class="stat-value warning">{{ formatAmount(stats.usedAmount) }}</div>
              <div class="stat-sub">{{ stats.usageRatio }}%</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">预警条数（≥80%）</div>
              <div class="stat-value danger">{{ stats.warningCount }}</div>
            </div>
          </div>
        </div>

        <el-card shadow="never" class="table-card">
          <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
            <el-table-column prop="departmentName" label="部门" width="120" show-overflow-tooltip />
            <el-table-column prop="projectName" label="项目" min-width="120" show-overflow-tooltip />
            <el-table-column label="预算类型" width="100" align="center">
              <template #default="{ row }">{{ row.budgetType === 1 ? '年度' : '月度' }}</template>
            </el-table-column>
            <el-table-column prop="year" label="年" width="72" align="center" />
            <el-table-column label="月" width="64" align="center">
              <template #default="{ row }">{{ row.month != null ? row.month : '—' }}</template>
            </el-table-column>
            <el-table-column prop="amount" label="预算金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="usedAmount" label="已用" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.usedAmount) }}</template>
            </el-table-column>
            <el-table-column prop="availableAmount" label="可用" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.availableAmount) }}</template>
            </el-table-column>
            <el-table-column label="使用率" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="usagePct(row) >= 100" type="danger" size="small">{{ usagePct(row) }}%</el-tag>
                <el-tag v-else-if="usagePct(row) >= 80" type="warning" size="small">{{ usagePct(row) }}%</el-tag>
                <span v-else>{{ usagePct(row) }}%</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="88" align="center">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新建预算' : '编辑预算'" width="520px" destroy-on-close @closed="resetForm">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="部门" required>
          <el-select v-model="editForm.departmentId" placeholder="请选择" filterable style="width: 100%" @change="onDeptChange">
            <el-option v-for="d in departmentOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目">
          <el-select v-model="editForm.projectId" placeholder="不选表示部门公共" clearable filterable style="width: 100%" @change="onProjectChange">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName || p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算类型" required>
          <el-radio-group v-model="editForm.budgetType">
            <el-radio :label="1">年度预算</el-radio>
            <el-radio :label="2">月度预算</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年度" required>
          <el-date-picker v-model="editForm.year" type="year" value-format="YYYY" placeholder="年度" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="editForm.budgetType === 2" label="月份" required>
          <el-select v-model="editForm.month" placeholder="月份" style="width: 100%">
            <el-option v-for="m in 12" :key="m" :label="`${m} 月`" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算金额" required>
          <el-input-number v-model="editForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'edit'" label="已用金额">
          <el-input-number v-model="editForm.usedAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option label="生效" :value="1" />
            <el-option label="调整中" :value="2" />
            <el-option label="已冻结" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const filterForm = reactive({
  departmentId: undefined as number | undefined,
  projectId: undefined as number | undefined,
  year: '' as string,
  month: undefined as number | undefined,
  budgetType: undefined as number | undefined,
  status: undefined as number | undefined
})

const departmentOptions = ref<any[]>([])
const projectOptions = ref<any[]>([])

const stats = reactive({
  recordCount: 0,
  totalBudget: 0,
  usedAmount: 0,
  usageRatio: '0',
  warningCount: 0
})

const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 20, total: 0 })

const editForm = reactive({
  id: undefined as number | undefined,
  departmentId: undefined as number | undefined,
  departmentName: '',
  projectId: undefined as number | undefined,
  projectName: '',
  budgetType: 2,
  year: '',
  month: undefined as number | undefined,
  amount: 0,
  usedAmount: 0,
  status: 1
})

const num = (v: any) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const formatAmount = (v: any) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(num(v))

const usagePct = (row: any) => {
  const rate = row.usageRate
  if (rate != null) return Math.round(num(rate) * 100) / 100
  const a = num(row.amount)
  if (a <= 0) return 0
  return Math.round((num(row.usedAmount) / a) * 10000) / 100
}

const statusText = (s: number) => (['', '生效', '调整中', '已冻结'][s] || '—')
const statusType = (s: number) => (s === 1 ? 'success' : s === 2 ? 'warning' : 'info')

const scopeParams = () => {
  const p: Record<string, any> = {}
  if (filterForm.departmentId != null) p.departmentId = filterForm.departmentId
  if (filterForm.projectId != null) p.projectId = filterForm.projectId
  if (filterForm.year) p.year = parseInt(filterForm.year, 10)
  if (filterForm.month != null) p.month = filterForm.month
  if (filterForm.budgetType != null) p.budgetType = filterForm.budgetType
  if (filterForm.status != null) p.status = filterForm.status
  return p
}

const summaryParams = () => {
  const p: Record<string, any> = {}
  if (filterForm.departmentId != null) p.departmentId = filterForm.departmentId
  if (filterForm.projectId != null) p.projectId = filterForm.projectId
  if (filterForm.year) p.year = parseInt(filterForm.year, 10)
  return p
}

const listParams = () => ({
  pageNum: pagination.page,
  pageSize: pagination.size,
  ...scopeParams()
})

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

const fetchSummary = async () => {
  try {
    const res: any = await api.budget.summary(summaryParams())
    if (res.code === 200 && res.data) {
      stats.recordCount = num(res.data.recordCount)
      stats.totalBudget = num(res.data.totalBudget)
      stats.usedAmount = num(res.data.usedAmount)
      stats.usageRatio = String(num(res.data.usageRatio).toFixed(2))
      stats.warningCount = num(res.data.warningCount)
    }
  } catch {
    /* ignore */
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await api.budget.page(listParams())
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载失败')
      return
    }
    const page = res.data || {}
    tableData.value = page.records || []
    pagination.total = num(page.total)
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchSummary()
  fetchList()
}

const handleReset = () => {
  filterForm.departmentId = undefined
  filterForm.projectId = undefined
  filterForm.year = ''
  filterForm.month = undefined
  filterForm.budgetType = undefined
  filterForm.status = undefined
  pagination.page = 1
  fetchSummary()
  fetchList()
}

const handleRefresh = () => {
  fetchSummary()
  fetchList()
  ElMessage.success('已刷新')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  fetchList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchList()
}

const resetForm = () => {
  editForm.id = undefined
  editForm.departmentId = undefined
  editForm.departmentName = ''
  editForm.projectId = undefined
  editForm.projectName = ''
  editForm.budgetType = 2
  editForm.year = String(new Date().getFullYear())
  editForm.month = new Date().getMonth() + 1
  editForm.amount = 0
  editForm.usedAmount = 0
  editForm.status = 1
}

const onDeptChange = (id: number) => {
  const d = departmentOptions.value.find((x: any) => x.id === id)
  editForm.departmentName = d?.deptName || d?.name || ''
}

const onProjectChange = (id: number | undefined) => {
  if (id == null) {
    editForm.projectName = ''
    return
  }
  const p = projectOptions.value.find((x: any) => x.id === id)
  editForm.projectName = p?.projectName || p?.name || ''
}

const openCreate = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  dialogMode.value = 'edit'
  editForm.id = row.id
  editForm.departmentId = row.departmentId
  editForm.departmentName = row.departmentName || ''
  editForm.projectId = row.projectId
  editForm.projectName = row.projectName || ''
  editForm.budgetType = row.budgetType ?? 2
  editForm.year = row.year != null ? String(row.year) : String(new Date().getFullYear())
  editForm.month = row.month
  editForm.amount = num(row.amount)
  editForm.usedAmount = num(row.usedAmount)
  editForm.status = row.status ?? 1
  dialogVisible.value = true
}

const submitForm = async () => {
  if (editForm.departmentId == null) {
    ElMessage.warning('请选择部门')
    return
  }
  if (!editForm.year) {
    ElMessage.warning('请选择年度')
    return
  }
  if (editForm.budgetType === 2 && editForm.month == null) {
    ElMessage.warning('请选择月份')
    return
  }
  saving.value = true
  try {
    const payload: any = {
      departmentId: editForm.departmentId,
      departmentName: editForm.departmentName,
      projectId: editForm.projectId ?? null,
      projectName: editForm.projectName || null,
      budgetType: editForm.budgetType,
      year: parseInt(editForm.year, 10),
      month: editForm.budgetType === 2 ? editForm.month : null,
      amount: editForm.amount,
      status: editForm.status
    }
    if (dialogMode.value === 'edit') {
      payload.id = editForm.id
      payload.usedAmount = editForm.usedAmount
    } else {
      payload.usedAmount = 0
    }
    const res: any =
      dialogMode.value === 'create'
        ? await api.budget.create(payload)
        : await api.budget.update(payload)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      await fetchSummary()
      await fetchList()
    }
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定删除部门「${row.departmentName || row.id}」的这条预算吗？`, '确认', {
      type: 'warning'
    })
    const res: any = await api.budget.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      await fetchSummary()
      await fetchList()
    }
  } catch {
    /* cancel */
  }
}

onMounted(async () => {
  const y = new Date().getFullYear()
  filterForm.year = String(y)
  await loadDepartments()
  await loadProjects()
  await fetchSummary()
  await fetchList()
})
</script>

<style scoped>
.budget-page {
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
  min-width: 160px;
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
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
.stat-value.primary {
  color: #409eff;
}
.stat-value.warning {
  color: #e6a23c;
}
.stat-value.danger {
  color: #f56c6c;
}
.stat-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
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
