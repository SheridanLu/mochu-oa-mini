<template>
  <div class="contract-container">
    <div class="page-header">
      <div class="header-left">
        <h2>合同管理</h2>
        <el-breadcrumb separator="/"><el-breadcrumb-item>合同管理</el-breadcrumb-item></el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
        <el-button v-if="canCreateContract" type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>新建合同
        </el-button>
      </div>
    </div>
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="合同类型">
          <el-select v-model="filterForm.contractType" placeholder="请选择" style="width: 140px" @change="handleContractTypeChange">
            <el-option label="收入合同" value="1" />
            <el-option label="支出合同" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同名称">
          <el-input
            v-model="filterForm.contractName"
            placeholder="请输入合同名称"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="项目">
          <el-select
            v-model="filterForm.projectId"
            placeholder="全部项目"
            clearable
            filterable
            style="width: 180px"
            @change="handleSearch"
          >
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName || p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 120px" @change="handleSearch">
            <el-option label="草稿" value="1" />
            <el-option label="待审批" value="2" />
            <el-option label="审批中" value="3" />
            <el-option label="已签订" value="4" />
            <el-option label="进行中" value="5" />
            <el-option label="已变更" value="6" />
            <el-option label="已终止" value="7" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" v-loading="loading">
      <el-table :data="tableData" stripe>
        <el-table-column prop="contractNo" label="合同编号" width="140" />
        <el-table-column prop="contractName" label="合同名称" min-width="180" />
        <el-table-column prop="projectName" label="关联项目" min-width="140" />
        <el-table-column prop="contractType" label="合同类型" width="100" align="center">
          <template #default="{ row }">{{ row.contractType === 2 ? '支出合同' : '收入合同' }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="合同金额" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalAmountWithTax ?? row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signDate" label="签订日期" width="110" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="canEdit(row)" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canSubmit(row)" type="primary" link @click="handleSubmit(row)">提交审批</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { api } from '@/api'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const filterForm = reactive({
  contractType: '1' as '1' | '2',
  contractName: '',
  projectId: undefined as number | undefined,
  status: ''
})
const pagination = reactive({ page: 1, size: 20, total: 0 })
const tableData = ref<any[]>([])
const projectOptions = ref<any[]>([])
const typeSwitched = ref(false)
let contractNameTimer: ReturnType<typeof setTimeout> | null = null

const canCreateContract = computed(() => true)

const getStatusType = (status: number) =>
  ['info', 'warning', 'primary', 'success', 'primary', 'warning', 'danger'][status - 1] || 'info'
const getStatusText = (status: number) =>
  ['', '草稿', '待审批', '审批中', '已签订', '进行中', '已变更', '已终止'][status] || '—'
const formatAmount = (v: any) => {
  if (v == null || v === '') return '—'
  const n = Number(v)
  if (!Number.isFinite(n)) return '—'
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(n)
}

const canEdit = (row: any) => row.status === 1
const canSubmit = (row: any) => row.contractType === 1 && row.status === 1

const editQuery = (row: any) => {
  const q: Record<string, string> = { id: String(row.id) }
  if (row.contractType === 2) q.expense = '1'
  return q
}

const syncQuery = () => {
  const q: Record<string, string> = {
    type: filterForm.contractType,
    page: String(pagination.page),
    size: String(pagination.size)
  }
  if (filterForm.contractName) q.name = filterForm.contractName
  if (filterForm.projectId != null) q.projectId = String(filterForm.projectId)
  if (filterForm.status) q.status = filterForm.status
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
    router.replace({ path: '/contract', query: q })
  }
}

const loadProjects = async () => {
  try {
    const res: any = await api.project.list()
    projectOptions.value = res.code === 200 ? res.data || [] : []
    if (filterForm.projectId != null) {
      const exists = projectOptions.value.some((p: any) => Number(p.id) === Number(filterForm.projectId))
      if (!exists) {
        filterForm.projectId = undefined
        pagination.page = 1
      }
    }
  } catch {
    projectOptions.value = []
  }
}

const fetchList = async () => {
  loading.value = true
  syncQuery()
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      contractName: filterForm.contractName || undefined,
      projectId: filterForm.projectId,
      status: filterForm.status ? parseInt(filterForm.status, 10) : undefined
    }
    if (filterForm.contractType === '2') {
      const res: any = await api.contract.expense.page(params)
      if (res.code !== 200) {
        ElMessage.error(res.message || '加载失败')
        tableData.value = []
        pagination.total = 0
        return
      }
      const page = res.data
      tableData.value = (page?.records || []).map((r: any) => ({ ...r, contractType: 2 }))
      pagination.total = page?.total ?? 0
    } else {
      const res: any = await api.contract.income.page(params)
      if (res.code !== 200) {
        ElMessage.error(res.message || '加载失败')
        tableData.value = []
        pagination.total = 0
        return
      }
      const page = res.data
      tableData.value = (page?.records || []).map((r: any) => ({ ...r, contractType: 1 }))
      pagination.total = page?.total ?? 0
    }
    if (typeSwitched.value && filterForm.projectId != null && pagination.total === 0) {
      ElMessage.info('当前项目在该合同类型下暂无数据')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    typeSwitched.value = false
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchList()
}
const handleContractTypeChange = () => {
  // 切换合同类型时清理可能不适配的筛选条件后再查询
  filterForm.status = ''
  typeSwitched.value = true
  pagination.page = 1
  fetchList()
}
const handleReset = () => {
  Object.assign(filterForm, { contractType: '1', contractName: '', projectId: undefined, status: '' })
  pagination.page = 1
  fetchList()
}
const handleSizeChange = () => fetchList()
const handleCurrentChange = () => fetchList()

const handleCreate = () => router.push('/contract/create')
const handleView = (row: any) => router.push({ path: '/contract/edit', query: editQuery(row) })
const handleEdit = (row: any) => router.push({ path: '/contract/edit', query: editQuery(row) })
const handleRefresh = () => {
  fetchList()
  ElMessage.success('已刷新')
}

const handleSubmit = async (row: any) => {
  if (row.contractType !== 1) return
  try {
    await ElMessageBox.confirm('提交后将进入审批流程（法务→总经理），确定提交？', '提示', { type: 'warning' })
    const res: any = await api.contract.income.submit(row.id)
    if (res.code !== 200) {
      ElMessage.error(res.message || '提交失败')
      return
    }
    ElMessage.success('提交审批成功')
    fetchList()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '提交失败')
  }
}

onMounted(() => {
  const q = route.query
  if (q.type === '1' || q.type === '2') {
    filterForm.contractType = q.type
  }
  if (typeof q.name === 'string') {
    filterForm.contractName = q.name
  }
  if (typeof q.projectId === 'string') {
    const pid = Number(q.projectId)
    if (Number.isFinite(pid) && pid > 0) filterForm.projectId = pid
  }
  if (typeof q.status === 'string') {
    filterForm.status = q.status
  }
  if (typeof q.page === 'string') {
    const p = Number(q.page)
    if (Number.isFinite(p) && p > 0) pagination.page = p
  }
  if (typeof q.size === 'string') {
    const s = Number(q.size)
    if (Number.isFinite(s) && s > 0) pagination.size = s
  }
  loadProjects()
  fetchList()
})

onUnmounted(() => {
  if (contractNameTimer) {
    clearTimeout(contractNameTimer)
    contractNameTimer = null
  }
})

watch(
  () => filterForm.contractName,
  (val) => {
    if (contractNameTimer) {
      clearTimeout(contractNameTimer)
      contractNameTimer = null
    }
    // 清空时由 @clear 立即查询，这里不重复触发
    if (!val) return
    contractNameTimer = setTimeout(() => {
      pagination.page = 1
      fetchList()
    }, 400)
  }
)
</script>

<style scoped>
.contract-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}
.filter-card {
  margin-bottom: 20px;
}
.table-card {
  background: #fff;
  border-radius: 8px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0;
}
</style>
