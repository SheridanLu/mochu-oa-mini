<template>
  <div class="invoice-page">
    <div class="page-header">
      <div class="header-left">
        <h2>发票管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>发票管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" :loading="loading" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-container class="main-layout">
      <el-aside width="260px" class="filter-aside">
        <el-card shadow="never" class="filter-card">
          <template #header>
            <span class="filter-title">筛选条件</span>
          </template>
          <el-form label-position="top" :model="filterForm" class="filter-form">
            <el-form-item label="发票类型">
              <el-select v-model="filterForm.invoiceType" placeholder="全部" clearable style="width: 100%">
                <el-option label="增值税专用发票" :value="1" />
                <el-option label="增值税普通发票" :value="2" />
                <el-option label="电子发票" :value="3" />
                <el-option label="其他" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="认证状态">
              <el-select v-model="filterForm.isVerified" placeholder="全部" clearable style="width: 100%">
                <el-option label="已认证" :value="1" />
                <el-option label="未认证" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item label="项目">
              <el-select v-model="filterForm.projectId" placeholder="全部项目" clearable filterable style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName || p.name" :value="p.id" />
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
              <div class="stat-label">发票总数</div>
              <div class="stat-value primary">{{ stats.total }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">已认证</div>
              <div class="stat-value success">{{ stats.verified }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">未认证</div>
              <div class="stat-value warning">{{ stats.unverified }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">本月开票额（价税）</div>
              <div class="stat-value">{{ formatAmount(stats.monthAmount) }}</div>
            </div>
          </div>
        </div>

        <el-card shadow="never" class="table-card">
          <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
            <el-table-column prop="invoiceNo" label="发票号码" width="150" show-overflow-tooltip />
            <el-table-column prop="invoiceType" label="发票类型" width="150" align="center">
              <template #default="{ row }">{{ getInvoiceTypeText(row.invoiceType) }}</template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="价税合计" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.totalAmount ?? row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="openedDate" label="开票日期" width="110" />
            <el-table-column label="关联项目" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ projectLabel(row.projectId) }}</template>
            </el-table-column>
            <el-table-column prop="isVerified" label="认证状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isVerified === 1" type="success" size="small">已认证</el-tag>
                <el-tag v-else type="warning" size="small">未认证</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="100" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="expireSoon(row) && row.isVerified !== 1" class="warn-text">建议尽快认证</span>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">查看</el-button>
                <el-button v-if="row.isVerified !== 1" type="primary" link @click="handleVerify(row)">认证</el-button>
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
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)

const filterForm = reactive({
  invoiceType: undefined as number | undefined,
  isVerified: undefined as number | undefined,
  projectId: undefined as number | undefined
})

const projectOptions = ref<any[]>([])

const stats = reactive({
  total: 0,
  verified: 0,
  unverified: 0,
  monthAmount: 0
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

const getInvoiceTypeText = (type: number) => {
  const m = ['', '增值税专用发票', '增值税普通发票', '电子发票', '其他']
  return m[type] || '—'
}

const projectLabel = (projectId: number | null | undefined) => {
  if (projectId == null) return '—'
  const p = projectOptions.value.find((x: any) => num(x.id) === num(projectId))
  return p?.projectName || p?.name || '—'
}

/** 开票超过 300 天仍未认证则提示 */
const expireSoon = (row: any) => {
  if (!row.openedDate) return false
  const d = new Date(String(row.openedDate))
  if (Number.isNaN(d.getTime())) return false
  const days = (Date.now() - d.getTime()) / (86400 * 1000)
  return days > 300
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
  if (filterForm.projectId != null) p.projectId = filterForm.projectId
  if (filterForm.invoiceType != null) p.invoiceType = filterForm.invoiceType
  return p
}

const listParams = () => ({
  pageNum: pagination.page,
  pageSize: pagination.size,
  ...summaryParams(),
  ...(filterForm.isVerified !== undefined && filterForm.isVerified !== null
    ? { isVerified: filterForm.isVerified }
    : {})
})

const fetchSummary = async () => {
  try {
    const res: any = await api.invoice.summary(summaryParams())
    if (res.code === 200 && res.data) {
      stats.total = num(res.data.total)
      stats.verified = num(res.data.verified)
      stats.unverified = num(res.data.unverified)
      stats.monthAmount = num(res.data.monthAmount)
    }
  } catch {
    /* ignore */
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await api.invoice.page(listParams())
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
  filterForm.invoiceType = undefined
  filterForm.isVerified = undefined
  filterForm.projectId = undefined
  pagination.page = 1
  fetchSummary()
  fetchList()
}

const handleRefresh = () => {
  fetchSummary()
  fetchList()
  ElMessage.success('已刷新')
}

const handleView = (row: any) => {
  ElMessage.info(`发票 ${row.invoiceNo || '—'}，价税合计 ${formatAmount(row.totalAmount ?? row.amount)}`)
}

const handleVerify = async (row: any) => {
  try {
    const res: any = await api.invoice.verify(row.id)
    if (res.code === 200) ElMessage.success('认证请求已处理')
    else ElMessage.error(res.message || '失败')
    fetchSummary()
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.message || '失败')
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
  await loadProjects()
  await fetchSummary()
  await fetchList()
})
</script>

<style scoped>
.invoice-page {
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
.stat-value.success {
  color: #67c23a;
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
.warn-text {
  color: #e6a23c;
  font-size: 13px;
}
</style>
