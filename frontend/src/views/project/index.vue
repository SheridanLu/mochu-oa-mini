<template>
  <div class="project-container">
    <div class="page-header">
      <div class="header-left">
        <h2>项目管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>项目管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button v-if="canCreateProject" type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>新建项目
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="项目状态">
          <el-select v-model="filterForm.status" placeholder="请选择" style="width: 120px" clearable>
            <el-option label="筹备" value="1" />
            <el-option label="进行中" value="2" />
            <el-option label="暂停" value="3" />
            <el-option label="中止" value="4" />
            <el-option label="完工" value="5" />
            <el-option label="结算" value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model="filterForm.projectName" placeholder="请输入项目名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" v-loading="loading">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="projectNo" label="项目编号" width="130" />
        <el-table-column prop="projectName" label="项目名称" min-width="180" />
        <el-table-column prop="projectType" label="项目类型" width="100" align="center">
          <template #default="{ row }">{{ row.projectType === 1 ? '虚拟项目' : '实体项目' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contractAmount" label="合同金额" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.contractAmount) }}</template>
        </el-table-column>
        <el-table-column prop="projectManagerName" label="项目经理" width="100" />
        <el-table-column prop="startDate" label="计划开工" width="110" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
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
const filterForm = reactive({ status: '', projectName: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const tableData = ref<any[]>([])

const canCreateProject = computed(() => {
  const roles = JSON.parse(localStorage.getItem('userInfo') || '{}').roles || []
  return roles.includes('GM') || roles.includes('PROJ_MGR') || roles.includes('PURCHASE')
})

const getStatusType = (status: number) => {
  const types = ['warning', 'primary', 'success', 'danger', 'info', 'success']
  return types[status - 1] || 'info'
}

const getStatusText = (status: number) => {
  const texts = ['筹备', '进行中', '暂停', '中止', '完工', '结算']
  return texts[status - 1] || ''
}

const formatAmount = (v: number) => v ? new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(v) : '-'

const canEdit = (row: any) => row.status === 1 || row.status === 6

const canSubmit = (row: any) => row.status === 1

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      status: filterForm.status,
      projectName: filterForm.projectName,
      page: pagination.page,
      size: pagination.size
    }
    const res = await api.project.list(params)
    tableData.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { filterForm.status = ''; filterForm.projectName = ''; pagination.page = 1; fetchList() }
const handleSizeChange = () => fetchList()
const handleCurrentChange = () => fetchList()

const handleCreate = () => router.push('/project/create')
const handleView = (row: any) => router.push(`/project/detail?id=${row.id}`)
const handleEdit = (row: any) => router.push(`/project/edit?id=${row.id}`)
const handleRefresh = () => { fetchList(); ElMessage.success('刷新成功') }

const handleSubmit = async (row: any) => {
  try {
    await ElMessageBox.confirm('提交后将进入审批流程，确定提交？', '提示', { type: 'warning' })
    await api.project.submit(row.id)
    ElMessage.success('提交审批成功')
    fetchList()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '提交失败')
  }
}

onMounted(() => { fetchList() })

const api = {
  list: async (params: any) => ({ code: 200, data: { list: [{ id: 1, projectNo: 'XM2026001', projectName: 'XX项目一期', projectType: 2, status: 1, contractAmount: 5000000, projectManagerName: '张三', startDate: '2026-01-01', createTime: '2026-01-01 10:00:00' }], total: 1 } }),
  submit: async (id: number) => { ElMessage.success('提交成功'); return { code: 200 } }
}
</script>

<style scoped>
.project-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
</style>