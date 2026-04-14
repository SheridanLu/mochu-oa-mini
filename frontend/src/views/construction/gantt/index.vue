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
        <el-button type="primary" @click="handleCreate" v-if="canCreate">
          <el-icon><Plus /></el-icon>新建甘特图
        </el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="项目">
          <el-select v-model="filterForm.projectId" placeholder="请选择项目" clearable style="width: 200px">
            <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="草稿" value="1" />
            <el-option label="待审批" value="2" />
            <el-option label="已审批" value="3" />
            <el-option label="进行中" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="name" label="甘特图名称" min-width="150" />
        <el-table-column prop="projectName" label="关联项目" min-width="150" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="milestoneCount" label="里程碑" width="80" align="center" />
        <el-table-column prop="taskCount" label="任务数" width="80" align="center" />
        <el-table-column prop="progress" label="进度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :color="getProgressColor(row.progress)" />
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="canEdit(row)" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canSubmit(row)" type="primary" link @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="canDelete(row)" type="danger" link @click="handleDelete(row)">删除</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const router = useRouter()
const loading = ref(false)
const tableData = ref<any[]>([])
const projectList = ref<any[]>([])
const filterForm = reactive({ projectId: '', status: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })

const canCreate = true

const getStatusType = (status: number) => ['', 'info', 'warning', 'success', 'primary'][status] || 'info'
const getStatusText = (status: number) => ['', '草稿', '待审批', '已审批', '进行中'][status] || ''
const getProgressColor = (rate: number) => {
  if (rate >= 100) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

const canEdit = (row: any) => row.status === 1
const canSubmit = (row: any) => row.status === 1
const canDelete = (row: any) => row.status === 1

const fetchList = async () => {
  loading.value = true
  try {
    const params = { ...filterForm, page: pagination.page, size: pagination.size }
    const res = await api.gantt.page(params)
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
    }
  } catch (e: any) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { filterForm.projectId = ''; filterForm.status = ''; pagination.page = 1; fetchList() }
const handleRefresh = () => { fetchList(); ElMessage.success('刷新成功') }

const handleCreate = () => router.push('/construction/gantt/edit')
const handleView = (row: any) => router.push(`/construction/gantt/detail?id=${row.id}`)
const handleEdit = (row: any) => router.push(`/construction/gantt/edit?id=${row.id}`)

const handleSubmit = async (row: any) => {
  try {
    await ElMessageBox.confirm('提交后将进入审批流程，确定提交？', '提示')
    await api.gantt.submit(row.id)
    ElMessage.success('提交成功')
    fetchList()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message) }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('删除后无法恢复，确定删除？', '警告', { type: 'warning' })
    await api.gantt.delete(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message) }
}

const handleSizeChange = (size: number) => { pagination.size = size; fetchList() }
const handleCurrentChange = (page: number) => { pagination.page = page; fetchList() }

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