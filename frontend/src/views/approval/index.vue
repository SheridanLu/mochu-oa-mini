<template>
  <div class="approval-container">
    <div class="page-header">
      <div class="header-left">
        <h2>工作待办中心</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>审批中心</el-breadcrumb-item>
          <el-breadcrumb-item>我的待办</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待办" name="todo">
        <template #label>
          <span class="tab-label">
            待办
            <el-badge :value="todoCount" :hidden="todoCount === 0" :max="99" />
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已办" name="done">
        <template #label>
          <span class="tab-label">
            已办
            <el-badge :value="doneCount" :hidden="doneCount === 0" :max="99" type="success" />
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已阅" name="read">
        <template #label>
          <span class="tab-label">
            已阅
            <el-badge :value="readCount" :hidden="readCount === 0" :max="99" type="info" />
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="搜索标题" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="filterForm.bizType" placeholder="全部" clearable style="width: 140px">
            <el-option label="项目立项" value="project" />
            <el-option label="合同审批" value="contract" />
            <el-option label="采购审批" value="purchase" />
            <el-option label="付款审批" value="payment" />
            <el-option label="报销审批" value="expense" />
            <el-option label="对账确认" value="statement" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="todoNo" label="待办编号" width="140" />
        <el-table-column prop="title" label="待办标题" min-width="200">
          <template #default="{ row }">
            <div class="todo-title" @click="handleView(row)">
              <el-tag v-if="row.priority === 1" type="danger" size="small" style="margin-right: 4px">紧急</el-tag>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="bizTypeText" label="业务类型" width="100" />
        <el-table-column prop="projectName" label="关联项目" min-width="120" />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="currentNode" label="当前节点" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">处理</el-button>
            <el-button v-if="activeTab === 'todo'" type="primary" link @click="handleTransfer(row)">转办</el-button>
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

    <el-dialog v-model="transferDialogVisible" title="转办" width="500px">
      <el-form :model="transferForm" label-width="80px">
        <el-form-item label="转办给">
          <el-select v-model="transferForm.handlerId" placeholder="选择处理人" style="width: 100%">
            <el-option v-for="user in userList" :key="user.id" :label="user.name" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转办意见">
          <el-input v-model="transferForm.opinion" type="textarea" :rows="3" placeholder="请输入转办意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTransferSubmit" :loading="transferLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '../../utils/format'
import api from '../../api'

const activeTab = ref('todo')
const loading = ref(false)
const tableData = ref<any[]>([])
const todoCount = ref(0)
const doneCount = ref(0)
const readCount = ref(0)
const userList = ref<any[]>([])
const transferDialogVisible = ref(false)
const transferLoading = ref(false)
const transferForm = reactive({
  todoId: 0,
  handlerId: 0,
  opinion: ''
})

const filterForm = reactive({
  keyword: '',
  bizType: '',
  dateRange: [] as string[]
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const BIZ_TYPE_MAP: Record<string, string> = {
  project: '项目立项',
  contract: '合同审批',
  purchase: '采购审批',
  payment: '付款审批',
  expense: '报销审批',
  statement: '对账确认'
}

const fetchCount = async () => {
  try {
    const res = await api.todo.count()
    if (res.code === 200) {
      todoCount.value = res.data?.todoCount || 0
      doneCount.value = res.data?.doneCount || 0
      readCount.value = res.data?.readCount || 0
    }
  } catch (e) {
    console.error('获取待办数量失败', e)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      category: activeTab.value,
      keyword: filterForm.keyword,
      bizType: filterForm.bizType,
      startTime: filterForm.dateRange?.[0],
      endTime: filterForm.dateRange?.[1],
      page: pagination.page,
      size: pagination.size
    }
    const res = await api.todo.list(params)
    if (res.code === 200) {
      tableData.value = (res.data?.list || []).map((item: any) => ({
        ...item,
        bizTypeText: BIZ_TYPE_MAP[item.bizType] || item.bizType
      }))
      pagination.total = res.data?.total || 0
    }
  } catch (e: any) {
    ElMessage.error(e.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pagination.page = 1
  fetchList()
}

const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

const handleReset = () => {
  filterForm.keyword = ''
  filterForm.bizType = ''
  filterForm.dateRange = []
  pagination.page = 1
  fetchList()
}

const handleRefresh = () => {
  fetchCount()
  fetchList()
  ElMessage.success('刷新成功')
}

const handleView = (row: any) => {
  window.location.href = `/approval/detail?id=${row.id}`
}

const handleTransfer = (row: any) => {
  transferForm.todoId = row.id
  transferForm.handlerId = 0
  transferForm.opinion = ''
  transferDialogVisible.value = true
}

const handleTransferSubmit = async () => {
  if (!transferForm.handlerId) {
    ElMessage.warning('请选择转办给')
    return
  }
  transferLoading.value = true
  try {
    await api.todo.handle(transferForm.todoId, {
      actionType: 'transfer',
      handlerId: transferForm.handlerId,
      opinion: transferForm.opinion
    })
    ElMessage.success('转办成功')
    transferDialogVisible.value = false
    handleRefresh()
  } catch (e: any) {
    ElMessage.error(e.message || '转办失败')
  } finally {
    transferLoading.value = false
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

onMounted(() => {
  fetchCount()
  fetchList()
})
</script>

<style scoped>
.approval-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 4px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  background: #fff;
  border-radius: 8px;
}

.todo-title {
  cursor: pointer;
  color: #409eff;
}

.todo-title:hover {
  text-decoration: underline;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0;
}
</style>