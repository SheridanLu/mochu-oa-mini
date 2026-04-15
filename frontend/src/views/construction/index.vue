<template>
  <div class="gantt-container">
    <div class="page-header">
      <div class="header-left">
        <h2>甘特图管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>施工管理</el-breadcrumb-item>
          <el-breadcrumb-item>甘特图</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
        <el-button v-if="canCreateGantt" type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>新建甘特图
        </el-button>
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
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="草稿" value="1" />
            <el-option label="待审批" value="2" />
            <el-option label="已审批" value="3" />
            <el-option label="进行中" value="4" />
            <el-option label="已完成" value="5" />
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
        <el-table-column prop="projectName" label="项目名称" min-width="150" />
        <el-table-column prop="ganttName" label="甘特图名称" min-width="150" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="100" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :color="getProgressColor(row.progress)" />
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="计划开始" width="110" />
        <el-table-column prop="endDate" label="计划结束" width="110" />
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="updateTime" label="最后更新" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="canEditRow(row)" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canSubmit(row)" type="primary" link @click="handleSubmit(row)">提交审批</el-button>
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
import { Plus, Refresh } from '@element-plus/icons-vue'
import { api } from '@/api'

const router = useRouter()
const loading = ref(false)
const filterForm = reactive({ projectId: '', status: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const tableData = ref<any[]>([])

const canCreateGantt = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const roles = userInfo.roles || []
  return roles.includes('PROJ_MGR') || roles.includes('GM')
})

const getStatusType = (status: number) => ['', 'info', 'warning', 'success', 'primary', 'success'][status] || 'info'
const getStatusText = (status: number) => ['', '草稿', '待审批', '已审批', '进行中', '已完成'][status] || ''
const getProgressColor = (progress: number) => {
  if (progress >= 80) return '#67c23a'
  if (progress >= 50) return '#e6a23c'
  return '#f56c6c'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await api.gantt.page({
      pageNum: pagination.page,
      pageSize: pagination.size,
      projectId: filterForm.projectId || undefined,
      status: filterForm.status || undefined
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e: any) { ElMessage.error(e.message || '获取列表失败') } 
  finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(filterForm, { projectId: '', status: '' }); pagination.page = 1; fetchList() }
const handleRefresh = () => { fetchList(); ElMessage.success('刷新成功') }
const handleCreate = () => router.push('/construction/gantt/edit')
const handleView = (row: any) => router.push(`/construction/gantt/edit?id=${row.id}&mode=view`)
const handleEdit = (row: any) => router.push(`/construction/gantt/edit?id=${row.id}`)

const handleSubmit = async (row: any) => {
  try {
    await ElMessageBox.confirm('提交后将进入审批流程（项目经理→总经理），审批通过后甘特图结构将锁定，确定提交？', '提示', { type: 'warning' })
    await api.gantt.submit(row.id)
    ElMessage.success('提交审批成功')
    fetchList()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '提交失败') }
}

const canSubmit = (row: any) => row.status === 1
const canEditRow = (row: any) => row.status === 1 || row.status === 4

onMounted(() => { fetchList() })
</script>

<style scoped>
.gantt-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
</style>