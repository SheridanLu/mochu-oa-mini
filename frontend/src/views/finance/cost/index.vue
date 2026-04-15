<template>
  <div class="cost-page">
    <div class="page-header">
      <div class="header-left">
        <h2>成本归集</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>成本归集</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleExport">导出</el-button>
        <el-button type="primary" :loading="loading" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" class="tip-alert" show-icon>
      来源类型：1 报销 · 2 采购 · 3 付款 · 4 工资 · 5 其他。后续可与采购、报销、付款单自动归集对接。
    </el-alert>

    <el-container class="main-layout">
      <el-aside width="280px" class="filter-aside">
        <el-card shadow="never" class="filter-card">
          <template #header>
            <span class="filter-title">筛选条件</span>
          </template>
          <el-form label-position="top" :model="filterForm" class="filter-form">
            <el-form-item label="项目">
              <el-select v-model="filterForm.projectId" placeholder="全部项目" clearable filterable style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName || p.name" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="成本类别">
              <el-select v-model="filterForm.costCategory" placeholder="全部" clearable style="width: 100%">
                <el-option label="材料费" :value="1" />
                <el-option label="人工费" :value="2" />
                <el-option label="机械费" :value="3" />
                <el-option label="管理费" :value="4" />
                <el-option label="其他" :value="5" />
              </el-select>
            </el-form-item>
            <el-form-item label="归属期间（起）">
              <el-date-picker
                v-model="filterForm.periodStart"
                type="month"
                placeholder="开始月份"
                value-format="YYYY-MM"
                clearable
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="归属期间（止）">
              <el-date-picker
                v-model="filterForm.periodEnd"
                type="month"
                placeholder="结束月份"
                value-format="YYYY-MM"
                clearable
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="归集日起">
              <el-date-picker
                v-model="filterForm.collectedBegin"
                type="date"
                placeholder="开始日期"
                value-format="YYYY-MM-DD"
                clearable
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="归集日止">
              <el-date-picker
                v-model="filterForm.collectedEnd"
                type="date"
                placeholder="结束日期"
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
              <div class="stat-label">记录条数</div>
              <div class="stat-value primary">{{ stats.totalCount }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">成本合计（筛选范围）</div>
              <div class="stat-value">{{ formatAmount(stats.totalAmount) }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">本月归集额（归集日）</div>
              <div class="stat-value warning">{{ formatAmount(stats.monthAmount) }}</div>
            </div>
          </div>
        </div>

        <el-card shadow="never" class="table-card">
          <el-table
            v-loading="loading"
            :data="tableData"
            stripe
            style="width: 100%"
            show-summary
            :summary-method="summaryMethod"
          >
            <el-table-column prop="projectName" label="项目" min-width="140" show-overflow-tooltip />
            <el-table-column prop="costCategoryName" label="成本类别" width="100" />
            <el-table-column prop="amount" label="金额" width="130" align="right">
              <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="sourceType" label="来源" width="100" align="center">
              <template #default="{ row }">
                <el-tag size="small">{{ getSourceTypeText(row.sourceType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sourceNo" label="来源单据号" width="140" show-overflow-tooltip />
            <el-table-column prop="collectedDate" label="归集日期" width="110" />
            <el-table-column prop="period" label="归属期间" width="100" />
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
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
import type { TableColumnCtx } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)

const filterForm = reactive({
  projectId: undefined as number | undefined,
  costCategory: undefined as number | undefined,
  periodStart: '' as string,
  periodEnd: '' as string,
  collectedBegin: '' as string,
  collectedEnd: '' as string
})

const projectOptions = ref<any[]>([])

const stats = reactive({
  totalCount: 0,
  totalAmount: 0,
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

/** 与库表一致：1 报销 2 采购 3 付款 4 工资 5 其他 */
const getSourceTypeText = (type: number | null | undefined) => {
  const m = ['', '报销', '采购', '付款', '工资', '其他']
  return m[num(type)] || '—'
}

const scopeParams = () => {
  const p: Record<string, any> = {}
  if (filterForm.projectId != null) p.projectId = filterForm.projectId
  if (filterForm.costCategory != null) p.costCategory = filterForm.costCategory
  if (filterForm.periodStart) p.periodStart = filterForm.periodStart
  if (filterForm.periodEnd) p.periodEnd = filterForm.periodEnd
  if (filterForm.collectedBegin) p.collectedBegin = filterForm.collectedBegin
  if (filterForm.collectedEnd) p.collectedEnd = filterForm.collectedEnd
  return p
}

const listParams = () => ({
  pageNum: pagination.page,
  pageSize: pagination.size,
  ...scopeParams()
})

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
    const res: any = await api.cost.summary(scopeParams())
    if (res.code === 200 && res.data) {
      stats.totalCount = num(res.data.totalCount)
      stats.totalAmount = num(res.data.totalAmount)
      stats.monthAmount = num(res.data.monthAmount)
    }
  } catch {
    /* ignore */
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await api.cost.page(listParams())
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

const summaryMethod = ({
  columns,
  data
}: {
  columns: TableColumnCtx<any>[]
  data: any[]
}) => {
  const sums: string[] = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.property === 'amount') {
      const t = data.reduce((s, row) => s + num(row.amount), 0)
      sums[index] = formatAmount(t)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const handleSearch = () => {
  pagination.page = 1
  fetchSummary()
  fetchList()
}

const handleReset = () => {
  filterForm.projectId = undefined
  filterForm.costCategory = undefined
  filterForm.periodStart = ''
  filterForm.periodEnd = ''
  filterForm.collectedBegin = ''
  filterForm.collectedEnd = ''
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
.cost-page {
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
  min-width: 180px;
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
