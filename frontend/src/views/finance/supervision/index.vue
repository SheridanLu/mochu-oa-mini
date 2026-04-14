<template>
  <div class="supervision-container">
    <div class="page-header">
      <div class="header-left">
        <h2>回款督办管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>回款督办</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleGenerate">生成督办计划</el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">督办中</div>
          <div class="stat-value primary">8</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">逾期</div>
          <div class="stat-value danger">3</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">总缺口金额</div>
          <div class="stat-value">¥2,450,000</div>
        </div>
      </el-col>
    </el-row>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="项目">
          <el-select v-model="filterForm.projectId" placeholder="请选择项目" clearable style="width: 200px">
            <el-option label="XX项目一" value="1" />
            <el-option label="XX项目二" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="filterForm.priority" placeholder="请选择" clearable style="width: 120px">
            <el-option label="高" value="1" />
            <el-option label="中" value="2" />
            <el-option label="低" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="督办状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待接收" value="1" />
            <el-option label="督办中" value="2" />
            <el-option label="已完成" value="3" />
            <el-option label="已终止" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="planNo" label="计划编号" width="140" />
        <el-table-column prop="projectName" label="项目" min-width="140" />
        <el-table-column prop="contractNo" label="合同" width="120" />
        <el-table-column prop="node" label="节点" width="120" />
        <el-table-column prop="gapAmount" label="回款缺口" width="120" align="right">
          <template #default="{ row }">
            <span :class="getAmountClass(row.gapAmount)">{{ formatAmount(row.gapAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="completionRate" label="完成率" width="100" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.completionRate" :color="getProgressColor(row.completionRate)" />
          </template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.overdueDays > 0" class="overdue-text">{{ row.overdueDays }}天</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">{{ getPriorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="supervisionStatus" label="督办状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSupervisionStatusType(row.supervisionStatus)">{{ getSupervisionStatusText(row.supervisionStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleApprove(row)">审批</el-button>
            <el-button type="primary" link @click="handleAssign(row)">转办</el-button>
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
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const filterForm = reactive({
  projectId: '',
  priority: '',
  status: ''
})

const tableData = ref([
  { id: 1, planNo: 'DB202604001', projectName: 'XX项目一期', contractNo: 'HT001', node: '地基完成', gapAmount: 500000, completionRate: 60, overdueDays: 5, priority: 1, supervisionStatus: 2 },
  { id: 2, planNo: 'DB202604002', projectName: 'XX项目二期', contractNo: 'HT002', node: '主体完成', gapAmount: 800000, completionRate: 40, overdueDays: 12, priority: 1, supervisionStatus: 2 },
  { id: 3, planNo: 'DB202603003', projectName: 'XX项目一期', contractNo: 'HT001', node: '竣工验收', gapAmount: 300000, completionRate: 85, overdueDays: 0, priority: 2, supervisionStatus: 1 }
])

const pagination = reactive({ page: 1, size: 20, total: 50 })

const formatAmount = (amount: number) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
}

const getAmountClass = (amount: number) => {
  if (amount > 500000) return 'amount-danger'
  if (amount > 200000) return 'amount-warning'
  return ''
}

const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

const getPriorityType = (priority: number) => {
  return ['danger', 'warning', 'info'][priority - 1] || 'info'
}

const getPriorityText = (priority: number) => {
  return ['高', '中', '低'][priority - 1] || ''
}

const getSupervisionStatusType = (status: number) => {
  return ['info', 'warning', 'success', 'danger'][status - 1] || 'info'
}

const getSupervisionStatusText = (status: number) => {
  return ['待接收', '督办中', '已完成', '已终止'][status - 1] || ''
}

const handleSearch = () => { pagination.page = 1 }
const handleReset = () => { filterForm.projectId = ''; filterForm.priority = ''; filterForm.status = ''; pagination.page = 1 }
const handleRefresh = () => { ElMessage.success('刷新成功') }
const handleGenerate = () => ElMessage.info('生成督办计划功能开发中')
const handleView = (row: any) => router.push(`/finance/supervision/detail?id=${row.id}`)
const handleApprove = (row: any) => { ElMessage.success('审批通过') }
const handleAssign = (row: any) => { ElMessage.info('转办功能开发中') }
</script>

<style scoped>
.supervision-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.stat-row { margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: 600; color: #303133; }
.stat-value.primary { color: #409eff; }
.stat-value.danger { color: #f56c6c; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
.overdue-text { color: #f56c6c; font-weight: 500; }
.amount-danger { color: #f56c6c; font-weight: 500; }
.amount-warning { color: #e6a23c; font-weight: 500; }
</style>