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
          <el-select v-model="filterForm.contractType" placeholder="请选择" style="width: 140px" clearable>
            <el-option label="收入合同" value="1" />
            <el-option label="支出合同" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择" style="width: 120px" clearable>
            <el-option label="草稿" value="1" />
            <el-option label="待审批" value="2" />
            <el-option label="审批中" value="3" />
            <el-option label="已签订" value="4" />
            <el-option label="进行中" value="5" />
            <el-option label="已变更" value="6" />
            <el-option label="已终止" value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同名称">
          <el-input v-model="filterForm.contractName" placeholder="请输入合同名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" v-loading="loading">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="contractNo" label="合同编号" width="140" />
        <el-table-column prop="contractName" label="合同名称" min-width="180" />
        <el-table-column prop="projectName" label="关联项目" min-width="140" />
        <el-table-column prop="contractType" label="合同类型" width="100" align="center">
          <template #default="{ row }">{{ row.contractType === 1 ? '收入合同' : '支出合同' }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="合同金额" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signDate" label="签订日期" width="110" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="canEdit(row)" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canSubmit(row)" type="primary" link @click="handleSubmit(row)">提交审批</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const filterForm = reactive({ contractType: '', status: '', contractName: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const tableData = ref<any[]>([])

const canCreateContract = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const roles = userInfo.roles || []
  return roles.includes('PURCHASE') || roles.includes('GM')
})

const getStatusType = (status: number) => ['info', 'warning', 'primary', 'success', 'primary', 'warning', 'danger'][status - 1] || 'info'
const getStatusText = (status: number) => ['', '草稿', '待审批', '审批中', '已签订', '进行中', '已变更', '已终止'][status] || ''
const formatAmount = (v: number) => v ? new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(v) : '-'

const canEdit = (row: any) => row.status === 1
const canSubmit = (row: any) => row.status === 1

const fetchList = async () => {
  loading.value = true
  try {
    const res = await api.contract.list({ ...filterForm, page: pagination.page, size: pagination.size })
    tableData.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (e: any) { ElMessage.error(e.message || '获取列表失败') } 
  finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(filterForm, { contractType: '', status: '', contractName: '' }); pagination.page = 1; fetchList() }
const handleSizeChange = () => fetchList()
const handleCurrentChange = () => fetchList()

const handleCreate = () => router.push('/contract/create')
const handleView = (row: any) => router.push(`/contract/detail?id=${row.id}`)
const handleEdit = (row: any) => router.push(`/contract/edit?id=${row.id}`)
const handleRefresh = () => { fetchList(); ElMessage.success('刷新成功') }

const handleSubmit = async (row: any) => {
  try {
    await ElMessageBox.confirm('提交后将进入审批流程（法务→总经理），确定提交？', '提示', { type: 'warning' })
    await api.contract.submit(row.id)
    ElMessage.success('提交审批成功')
    fetchList()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '提交失败') }
}

onMounted(() => { fetchList() })

const api = {
  list: async () => ({ code: 200, data: { list: [{ id: 1, contractNo: 'HT2026001', contractName: '施工总承包合同', projectName: 'XX项目', contractType: 1, totalAmount: 8000000, status: 1, signDate: '2026-01-15', createTime: '2026-01-10 10:00:00' }], total: 1 } }),
  submit: async () => ({ code: 200 })
}
</script>

<style scoped>
.contract-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
</style>