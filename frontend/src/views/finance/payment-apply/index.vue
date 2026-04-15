<template>
  <div class="payment-apply-page">
    <div class="page-header">
      <div class="header-left">
        <h2>付款申请</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>付款申请</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openCreate">新建申请</el-button>
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
            <el-form-item label="项目">
              <el-select
                v-model="filterForm.projectId"
                placeholder="全部"
                clearable
                filterable
                style="width: 100%"
                @change="onFilterProjectChange"
              >
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName || p.name" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="支出合同">
              <el-select v-model="filterForm.contractId" placeholder="全部" clearable filterable style="width: 100%">
                <el-option v-for="c in contractFilterOptions" :key="c.id" :label="contractLabel(c)" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="付款类别">
              <el-select v-model="filterForm.category" placeholder="全部" clearable style="width: 100%">
                <el-option label="材料款" :value="1" />
                <el-option label="设备款" :value="2" />
                <el-option label="人工费" :value="3" />
                <el-option label="其他" :value="4" />
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
            <el-form-item label="申请单号">
              <el-input v-model="filterForm.applyNo" placeholder="模糊" clearable />
            </el-form-item>
            <el-form-item label="创建起">
              <el-date-picker
                v-model="filterForm.createdBegin"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="开始"
                clearable
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="创建止">
              <el-date-picker
                v-model="filterForm.createdEnd"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="结束"
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
              <div class="stat-label">待审 / 审批中</div>
              <div class="stat-value warning">{{ stats.pendingCount }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">已支付笔数</div>
              <div class="stat-value success">{{ stats.paidCount }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">含税金额合计</div>
              <div class="stat-value">{{ formatAmount(stats.totalAmount) }}</div>
            </div>
          </div>
        </div>

        <el-card shadow="never" class="table-card">
          <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
            <el-table-column prop="applyNo" label="申请单号" width="160" show-overflow-tooltip />
            <el-table-column label="项目" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ projectLabel(row.projectId) }}</template>
            </el-table-column>
            <el-table-column label="合同" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ contractLabelById(row.contractId) }}</template>
            </el-table-column>
            <el-table-column label="类别" width="88" align="center">
              <template #default="{ row }">{{ categoryText(row.category) }}</template>
            </el-table-column>
            <el-table-column prop="supplierName" label="供应商" width="120" show-overflow-tooltip />
            <el-table-column label="含税金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.totalAmount ?? row.amount) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="92" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="165" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 1" type="primary" link @click="openEdit(row)">编辑</el-button>
                <el-button v-if="row.status === 1" type="primary" link @click="handleSubmit(row)">提交</el-button>
                <el-button
                  v-if="row.status === 2 || row.status === 3"
                  type="success"
                  link
                  @click="handleApprove(row)"
                >
                  通过
                </el-button>
                <el-button
                  v-if="row.status === 2 || row.status === 3"
                  type="danger"
                  link
                  @click="handleReject(row)"
                >
                  驳回
                </el-button>
                <el-button v-if="row.status === 1" type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新建付款申请' : '编辑'" width="560px" destroy-on-close @closed="resetForm">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="项目" required>
          <el-select v-model="editForm.projectId" placeholder="请选择" filterable style="width: 100%" @change="onEditProjectChange">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName || p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="支出合同">
          <el-select v-model="editForm.contractId" placeholder="可选" clearable filterable style="width: 100%">
            <el-option v-for="c in editContractOptions" :key="c.id" :label="contractLabel(c)" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款类别" required>
          <el-select v-model="editForm.category" style="width: 100%">
            <el-option label="材料款" :value="1" />
            <el-option label="设备款" :value="2" />
            <el-option label="人工费" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="editForm.supplierName" placeholder="名称" clearable />
        </el-form-item>
        <el-form-item label="含税金额" required>
          <el-input-number v-model="editForm.totalAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="editForm.paymentMethod" placeholder="可选" clearable style="width: 100%">
            <el-option label="银行转账" :value="1" />
            <el-option label="现金" :value="2" />
            <el-option label="承兑汇票" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款银行">
          <el-input v-model="editForm.bankName" />
        </el-form-item>
        <el-form-item label="收款账号">
          <el-input v-model="editForm.bankAccount" />
        </el-form-item>
        <el-form-item label="用途">
          <el-input v-model="editForm.purpose" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const projectOptions = ref<any[]>([])
const allExpenseContracts = ref<any[]>([])

const filterForm = reactive({
  projectId: undefined as number | undefined,
  contractId: undefined as number | undefined,
  category: undefined as number | undefined,
  status: undefined as number | undefined,
  applyNo: '',
  createdBegin: '',
  createdEnd: ''
})

const contractFilterOptions = computed(() => {
  const pid = filterForm.projectId
  if (pid == null) return allExpenseContracts.value
  return allExpenseContracts.value.filter((c: any) => Number(c.projectId) === Number(pid))
})

const editContractOptions = computed(() => {
  const pid = editForm.projectId
  if (pid == null) return allExpenseContracts.value
  return allExpenseContracts.value.filter((c: any) => Number(c.projectId) === Number(pid))
})

const stats = reactive({
  pendingCount: 0,
  paidCount: 0,
  totalAmount: 0
})

const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 20, total: 0 })

const editForm = reactive({
  id: undefined as number | undefined,
  projectId: undefined as number | undefined,
  contractId: undefined as number | undefined,
  category: 1,
  supplierName: '',
  totalAmount: 0,
  paymentMethod: undefined as number | undefined,
  bankName: '',
  bankAccount: '',
  purpose: '',
  remark: ''
})

const num = (v: any) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const formatAmount = (v: any) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(num(v))

const formatDt = (v: any) => {
  if (v == null) return '—'
  if (typeof v === 'string') return v.replace('T', ' ').slice(0, 19)
  return String(v)
}

const contractLabel = (c: any) => {
  const no = c.contractNo || ''
  const name = c.contractName || ''
  return [no, name].filter(Boolean).join(' ') || `合同 #${c.id}`
}

const contractLabelById = (cid: number | null | undefined) => {
  if (cid == null) return '—'
  const c = allExpenseContracts.value.find((x: any) => num(x.id) === num(cid))
  return c ? contractLabel(c) : '—'
}

const projectLabel = (pid: number | null | undefined) => {
  if (pid == null) return '—'
  const p = projectOptions.value.find((x: any) => num(x.id) === num(pid))
  return p?.projectName || p?.name || '—'
}

const categoryText = (c: number) => (['', '材料款', '设备款', '人工费', '其他'][c] || '—')

const statusText = (s: number) =>
  (['', '草稿', '待审批', '审批中', '已通过', '已驳回', '已支付'][s] || '—')

const statusTag = (s: number) => {
  const m: Record<number, string> = {
    1: 'info',
    2: 'warning',
    3: 'warning',
    4: 'success',
    5: 'danger',
    6: 'success'
  }
  return m[s] || 'info'
}

const scopeParams = () => {
  const p: Record<string, any> = {}
  if (filterForm.projectId != null) p.projectId = filterForm.projectId
  if (filterForm.contractId != null) p.contractId = filterForm.contractId
  if (filterForm.category != null) p.category = filterForm.category
  if (filterForm.applyNo) p.applyNo = filterForm.applyNo
  if (filterForm.createdBegin) p.createdBegin = filterForm.createdBegin
  if (filterForm.createdEnd) p.createdEnd = filterForm.createdEnd
  return p
}

const summaryParams = () => scopeParams()

const listParams = () => ({
  pageNum: pagination.page,
  pageSize: pagination.size,
  ...scopeParams(),
  ...(filterForm.status != null ? { status: filterForm.status } : {})
})

const loadProjects = async () => {
  try {
    const res: any = await api.project.list()
    projectOptions.value = res.code === 200 ? res.data || [] : []
  } catch {
    projectOptions.value = []
  }
}

const loadContracts = async () => {
  try {
    const res: any = await api.contract.expense.list()
    allExpenseContracts.value = res.code === 200 ? res.data || [] : []
  } catch {
    allExpenseContracts.value = []
  }
}

const fetchSummary = async () => {
  try {
    const res: any = await api.paymentApply.summary(summaryParams())
    if (res.code === 200 && res.data) {
      stats.pendingCount = num(res.data.pendingCount)
      stats.paidCount = num(res.data.paidCount)
      stats.totalAmount = num(res.data.totalAmount)
    }
  } catch {
    /* ignore */
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await api.paymentApply.page(listParams())
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载失败')
      return
    }
    const page = res.data || {}
    tableData.value = (page.records || []).map((r: any) => ({ ...r, createdAt: formatDt(r.createdAt) }))
    pagination.total = num(page.total)
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const onFilterProjectChange = () => {
  const cid = filterForm.contractId
  if (cid != null) {
    const c = allExpenseContracts.value.find((x: any) => num(x.id) === num(cid))
    if (c && filterForm.projectId != null && num(c.projectId) !== num(filterForm.projectId)) {
      filterForm.contractId = undefined
    }
  }
}

const onEditProjectChange = () => {
  editForm.contractId = undefined
}

const handleSearch = () => {
  pagination.page = 1
  fetchSummary()
  fetchList()
}

const handleReset = () => {
  filterForm.projectId = undefined
  filterForm.contractId = undefined
  filterForm.category = undefined
  filterForm.status = undefined
  filterForm.applyNo = ''
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
  editForm.projectId = undefined
  editForm.contractId = undefined
  editForm.category = 1
  editForm.supplierName = ''
  editForm.totalAmount = 0
  editForm.paymentMethod = undefined
  editForm.bankName = ''
  editForm.bankAccount = ''
  editForm.purpose = ''
  editForm.remark = ''
}

const openCreate = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  dialogMode.value = 'edit'
  editForm.id = row.id
  editForm.projectId = row.projectId
  editForm.contractId = row.contractId
  editForm.category = row.category ?? 1
  editForm.supplierName = row.supplierName || ''
  editForm.totalAmount = num(row.totalAmount ?? row.amount)
  editForm.paymentMethod = row.paymentMethod
  editForm.bankName = row.bankName || ''
  editForm.bankAccount = row.bankAccount || ''
  editForm.purpose = row.purpose || ''
  editForm.remark = row.remark || ''
  dialogVisible.value = true
}

const submitForm = async () => {
  if (editForm.projectId == null) {
    ElMessage.warning('请选择项目')
    return
  }
  if (editForm.totalAmount == null || editForm.totalAmount <= 0) {
    ElMessage.warning('请填写含税金额')
    return
  }
  saving.value = true
  try {
    const payload: any = {
      projectId: editForm.projectId,
      contractId: editForm.contractId ?? null,
      category: editForm.category,
      supplierName: editForm.supplierName || null,
      amount: editForm.totalAmount,
      totalAmount: editForm.totalAmount,
      paymentMethod: editForm.paymentMethod ?? null,
      bankName: editForm.bankName || null,
      bankAccount: editForm.bankAccount || null,
      purpose: editForm.purpose || null,
      remark: editForm.remark || null
    }
    if (dialogMode.value === 'edit') payload.id = editForm.id
    const res: any =
      dialogMode.value === 'create'
        ? await api.paymentApply.create(payload)
        : await api.paymentApply.update(payload)
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

const handleSubmit = async (row: any) => {
  try {
    const res: any = await api.paymentApply.submit(row.id)
    if (res.code === 200) {
      ElMessage.success('已提交审批')
      await fetchSummary()
      await fetchList()
    }
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  }
}

const handleApprove = async (row: any) => {
  try {
    const res: any = await api.paymentApply.approve(row.id, { action: 'approve' })
    if (res.code === 200) {
      ElMessage.success('已通过')
      await fetchSummary()
      await fetchList()
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleReject = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认驳回该付款申请？', '提示', { type: 'warning' })
    const res: any = await api.paymentApply.approve(row.id, { action: 'reject' })
    if (res.code === 200) {
      ElMessage.success('已驳回')
      await fetchSummary()
      await fetchList()
    }
  } catch {
    /* cancel */
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该草稿？', '提示', { type: 'warning' })
    const res: any = await api.paymentApply.delete(row.id)
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
  await loadProjects()
  await loadContracts()
  await fetchSummary()
  await fetchList()
})
</script>

<style scoped>
.payment-apply-page {
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
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.stat-value.warning {
  color: #e6a23c;
}
.stat-value.success {
  color: #67c23a;
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
