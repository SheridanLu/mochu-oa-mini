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
      <el-tab-pane label="待办" name="TODO">
        <template #label>
          <span class="tab-label">
            待办
            <el-badge :value="todoCount" :hidden="todoCount === 0" :max="99" />
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已办" name="DONE">
        <template #label>
          <span class="tab-label">
            已办
            <el-badge :value="doneCount" :hidden="doneCount === 0" :max="99" type="success" />
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已阅" name="READ">
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
            <el-button v-if="activeTab === 'TODO'" type="primary" link @click="handleTransfer(row)">转办</el-button>
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

    <el-dialog v-model="historyVisible" title="审批记录" width="720px" @closed="onHistoryClosed">
      <el-table :data="historyRows" stripe max-height="420">
        <el-table-column prop="nodeOrder" label="顺序" width="70" />
        <el-table-column prop="nodeName" label="节点" width="120" />
        <el-table-column prop="approverName" label="审批人" width="100" />
        <el-table-column prop="action" label="动作" width="90" />
        <el-table-column prop="opinion" label="意见" min-width="160" show-overflow-tooltip />
        <el-table-column prop="operateTime" label="时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.operateTime) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '../../utils/format'
import { api } from '../../api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('TODO')
const loading = ref(false)
const tableData = ref<any[]>([])
const todoCount = ref(0)
const doneCount = ref(0)
const readCount = ref(0)
const userList = ref<any[]>([])
const transferDialogVisible = ref(false)
const transferLoading = ref(false)
const transferForm = reactive({
  instanceId: 0,
  handlerId: 0,
  opinion: ''
})

const historyVisible = ref(false)
const historyRows = ref<any[]>([])

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

const bizTypeLabel = (bizType: string) => BIZ_TYPE_MAP[(bizType || '').toLowerCase()] || bizType || '-'

const currentUserId = () => {
  const id = userStore.userInfo?.id
  return id && id > 0 ? id : null
}

const loadUserOptions = async () => {
  try {
    const res = await api.system.user.list()
    if (res.code === 200) {
      userList.value = (res.data || []).map((u: any) => ({
        id: u.id,
        name: u.realName || u.username || String(u.id)
      }))
    }
  } catch (e) {
    console.error('加载用户列表失败', e)
  }
}

const fetchCount = async () => {
  const uid = currentUserId()
  if (!uid) return
  try {
    const res = await api.todo.count({ userId: uid })
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
  const uid = currentUserId()
  if (!uid) {
    ElMessage.warning('未获取到登录用户信息，请重新登录')
    return
  }
  loading.value = true
  try {
    const params = {
      userId: uid,
      category: activeTab.value,
      keyword: filterForm.keyword,
      bizType: filterForm.bizType || undefined,
      page: pagination.page,
      size: pagination.size
    }
    const res = await api.todo.list(params)
    if (res.code === 200) {
      tableData.value = (res.data?.list || []).map((item: any) => ({
        ...item,
        bizTypeText: bizTypeLabel(item.bizType)
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

const loadHistory = async (instanceId: number) => {
  try {
    const res = await api.approval.history(instanceId)
    if (res.code === 200) {
      historyRows.value = res.data || []
      historyVisible.value = true
    } else {
      ElMessage.error(res.message || '加载审批记录失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载审批记录失败')
  }
}

const handleView = (row: any) => {
  if (row.instanceId) {
    void loadHistory(row.instanceId)
  } else {
    ElMessage.warning('缺少流程实例，无法查看')
  }
}

const handleTransfer = (row: any) => {
  transferForm.instanceId = row.instanceId
  transferForm.handlerId = 0
  transferForm.opinion = ''
  transferDialogVisible.value = true
}

const handleTransferSubmit = async () => {
  if (!transferForm.handlerId) {
    ElMessage.warning('请选择转办给')
    return
  }
  const uid = currentUserId()
  if (!uid) {
    ElMessage.warning('未获取到当前用户')
    return
  }
  transferLoading.value = true
  try {
    const res: any = await api.approval.delegate(transferForm.instanceId, {
      fromUserId: uid,
      toUserId: transferForm.handlerId,
      opinion: transferForm.opinion
    })
    if (res.code !== 200) {
      ElMessage.error(res.message || '转办失败')
      return
    }
    ElMessage.success('转办成功')
    transferDialogVisible.value = false
    handleRefresh()
  } catch (e: any) {
    ElMessage.error(e.message || '转办失败')
  } finally {
    transferLoading.value = false
  }
}

const openInstanceFromQuery = () => {
  const raw = route.query.instanceId
  if (raw == null || raw === '') return
  const num = Number(Array.isArray(raw) ? raw[0] : raw)
  if (!Number.isFinite(num)) return
  void loadHistory(num)
}

const onHistoryClosed = () => {
  if (route.query.instanceId) {
    router.replace({ path: '/approval', query: {} })
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

watch(
  () => route.query.instanceId,
  () => openInstanceFromQuery()
)

onMounted(() => {
  void loadUserOptions()
  fetchCount()
  fetchList()
  openInstanceFromQuery()
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