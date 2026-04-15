<template>
  <div class="purchase-container">
    <div class="page-header">
      <div class="header-left">
        <h2>采购清单管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>采购管理</el-breadcrumb-item>
          <el-breadcrumb-item>采购清单</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleImport">从合同附件生成草稿</el-button>
        <el-button type="primary" @click="handleCreate">新建采购清单</el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="项目">
          <el-select v-model="filterForm.projectId" placeholder="请选择项目" clearable style="width: 200px">
            <el-option label="XX项目一" value="1" />
            <el-option label="XX项目二" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同编号">
          <el-input v-model="filterForm.contractNo" placeholder="请输入合同编号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="清单状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="草稿" value="1" />
            <el-option label="待审批" value="2" />
            <el-option label="已审批" value="3" />
            <el-option label="已完成" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源类型">
          <el-select v-model="filterForm.sourceType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="附件导入" value="2" />
            <el-option label="手工创建" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" v-loading="loading">
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="采购清单编号" width="150" />
        <el-table-column prop="contractNo" label="关联合同编号" width="130" />
        <el-table-column prop="projectName" label="关联项目" min-width="140" />
        <el-table-column prop="status" label="清单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceType" label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.sourceType === 2 ? 'warning' : 'info'">{{ row.sourceType === 2 ? '附件导入' : '手工创建' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalBudget" label="预算总额" width="120" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalBudget) }}</template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 1" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="primary" link @click="handleSubmit(row)">提交审批</el-button>
            <el-button v-if="row.status === 1" type="primary" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const router = useRouter()

const loading = ref(false)
const filterForm = reactive({ projectId: '', contractNo: '', status: '', sourceType: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const tableData = ref<any[]>([])

const canCreate = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const roles = userInfo.roles || []
  return roles.includes('BUDGET') || roles.includes('GM')
})

const formatAmount = (amount: number) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
const getStatusType = (status: number) => ['', 'info', 'warning', 'success', 'primary'][status] || 'info'
const getStatusText = (status: number) => ['', '草稿', '待审批', '已审批', '已完成'][status] || ''

const fetchList = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      projectId: filterForm.projectId || undefined,
      status: filterForm.status || undefined
    }
    const res: any = await api.purchase.page(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e: any) { ElMessage.error(e.message || '获取列表失败') } 
  finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(filterForm, { projectId: '', contractNo: '', status: '', sourceType: '' }); pagination.page = 1; fetchList() }
const handleRefresh = () => { fetchList(); ElMessage.success('刷新成功') }

const handleCreate = () => router.push('/purchase/edit')
const handleImport = () => router.push('/purchase/import')
const handleView = (row: any) => router.push(`/purchase/edit?id=${row.id}&mode=view`)
const handleEdit = (row: any) => router.push(`/purchase/edit?id=${row.id}`)

const handleSubmit = async (row: any) => {
  try {
    await ElMessageBox.confirm('提交后将进入审批流程（预算员→财务→总经理），确定提交？', '提示', { type: 'warning' })
    await api.purchase.submit(row.id)
    ElMessage.success('提交审批成功')
    fetchList()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '提交失败') }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('删除后无法恢复，确定删除？', '提示', { type: 'warning' })
    await api.purchase.delete(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '删除失败') }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.purchase-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
</style>