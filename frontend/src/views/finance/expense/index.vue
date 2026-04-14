<template>
  <div class="expense-container">
    <div class="page-header">
      <div class="header-left">
        <h2>日常报销管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>日常报销</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleExport">导出</el-button>
        <el-button type="primary" @click="handleCreate">新建报销单</el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">待审批</div>
          <div class="stat-value warning">12</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">已支付</div>
          <div class="stat-value success">45</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">预算预警项</div>
          <div class="stat-value danger">3</div>
        </div>
      </el-col>
    </el-row>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="部门">
          <el-select v-model="filterForm.departmentId" placeholder="请选择部门" clearable style="width: 150px">
            <el-option label="基础业务部" value="1" />
            <el-option label="软件业务部" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目">
          <el-select v-model="filterForm.projectId" placeholder="请选择项目" clearable style="width: 200px">
            <el-option label="XX项目一" value="1" />
            <el-option label="XX项目二" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型">
          <el-select v-model="filterForm.expenseType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="差旅费" value="1" />
            <el-option label="招待费" value="2" />
            <el-option label="办公费" value="3" />
            <el-option label="交通费" value="4" />
            <el-option label="其他" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="草稿" value="1" />
            <el-option label="待审批" value="2" />
            <el-option label="已通过" value="3" />
            <el-option label="已驳回" value="4" />
            <el-option label="已支付" value="5" />
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
        <el-table-column prop="reportNo" label="报销单编号" width="150" />
        <el-table-column prop="reporterName" label="报销人" width="100" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="projectName" label="项目" min-width="140" />
        <el-table-column prop="totalAmount" label="金额" width="120" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="budgetUsage" label="预算占用%" width="100" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.budgetUsage" :color="getBudgetColor(row.budgetUsage)" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 1" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="primary" link @click="handleSubmit(row)">提交</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" />
      </div>
    </el-card>

    <el-alert type="info" :closable="false" style="margin-top: 20px">
      <template #title>
        <span>预算占用≥80%预警；≥100%将禁止提交</span>
      </template>
    </el-alert>

    <el-dialog v-model="warningDialogVisible" title="预算预警" width="420px" :close-on-click-modal="false">
      <div class="warning-content">
        <div class="warning-icon" :class="warningLevel">
          <el-icon :size="48"><WarningFilled /></el-icon>
        </div>
        <div class="warning-text">
          <p v-if="warningLevel === 'red'" class="warning-red">预算已超支，无法提交报销单！</p>
          <p v-else class="warning-yellow">预算占用已超过80%，提交后可能影响项目预算！</p>
          <p class="warning-detail">项目：{{ warningData.projectName }} | 当前预算占用：{{ warningData.budgetUsage }}%</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="warningDialogVisible = false">取消</el-button>
        <el-button v-if="warningLevel === 'yellow'" type="warning" @click="confirmSubmit">确认提交</el-button>
        <el-button v-else type="danger" disabled>无法提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
import { WarningFilled } from '@element-plus/icons-vue'

const filterForm = reactive({ departmentId: '', projectId: '', expenseType: '', status: '' })
const tableData = ref([
  { id: 1, reportNo: 'BX202604001', reporterName: '张三', departmentName: '基础业务部', projectName: 'XX项目', totalAmount: 3500, budgetUsage: 65, status: 2, createTime: '2026-04-13 10:30' },
  { id: 2, reportNo: 'BX202604002', reporterName: '李四', departmentName: '软件业务部', projectName: 'XX项目', totalAmount: 1200, budgetUsage: 85, status: 1, createTime: '2026-04-12 16:20' },
  { id: 3, reportNo: 'BX202603015', reporterName: '王五', departmentName: '基础业务部', projectName: 'XX项目', totalAmount: 2800, budgetUsage: 100, status: 4, createTime: '2026-03-28 09:15' }
])
const pagination = reactive({ page: 1, size: 20, total: 80 })

const formatAmount = (amount: number) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
const getBudgetColor = (usage: number) => { if (usage >= 100) return '#f56c6c'; if (usage >= 80) return '#e6a23c'; return '#67c23a' }
const getStatusType = (status: number) => ['', 'info', 'warning', 'success', 'danger', 'success'][status] || 'info'
const getStatusText = (status: number) => ['', '草稿', '待审批', '已通过', '已驳回', '已支付'][status] || ''
const handleSearch = () => { pagination.page = 1 }
const handleReset = () => { filterForm.departmentId = ''; filterForm.projectId = ''; filterForm.expenseType = ''; filterForm.status = ''; pagination.page = 1 }
const handleExport = () => { ElMessage.info('导出功能开发中') }
const handleCreate = () => router.push('/finance/expense/edit')
const handleView = (row: any) => router.push(`/finance/expense/edit?id=${row.id}&mode=view`)
const handleEdit = (row: any) => router.push(`/finance/expense/edit?id=${row.id}`)
const handleSubmit = (row: any) => {
  warningData.value = { projectName: row.projectName, budgetUsage: row.budgetUsage }
  warningLevel.value = row.budgetUsage >= 100 ? 'red' : 'yellow'
  warningDialogVisible.value = true
}
const confirmSubmit = () => { warningDialogVisible.value = false; ElMessage.success('提交成功') }

const warningDialogVisible = ref(false)
const warningLevel = ref<'yellow' | 'red'>('yellow')
const warningData = reactive({ projectName: '', budgetUsage: 0 })
</script>

<style scoped>
.expense-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.stat-row { margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: 600; color: #303133; }
.stat-value.warning { color: #e6a23c; }
.stat-value.success { color: #67c23a; }
.stat-value.danger { color: #f56c6c; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
.warning-content { display: flex; align-items: center; gap: 20px; padding: 10px 0; }
.warning-icon { display: flex; align-items: center; justify-content: center; width: 80px; height: 80px; border-radius: 50%; }
.warning-icon.yellow { background: #fdf6ec; color: #e6a23c; }
.warning-icon.red { background: #fef0f0; color: #f56c6c; }
.warning-text { flex: 1; }
.warning-text p { margin: 0 0 8px 0; font-size: 16px; }
.warning-red { color: #f56c6c; font-weight: 600; }
.warning-yellow { color: #e6a23c; font-weight: 600; }
.warning-detail { color: #909399; font-size: 14px; }
</style>