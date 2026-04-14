<template>
  <div class="reconciliation-container">
    <div class="page-header">
      <div class="header-left">
        <h2>收入对账管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>收入对账</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleExport">导出</el-button>
        <el-button type="primary" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">待确认对账单</div>
          <div class="stat-value warning">5</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">异常对账单</div>
          <div class="stat-value danger">2</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">当月金额总计</div>
          <div class="stat-value">¥1,234,567.89</div>
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
        <el-form-item label="合同">
          <el-select v-model="filterForm.contractId" placeholder="请选择合同" clearable style="width: 200px">
            <el-option label="合同001" value="1" />
            <el-option label="合同002" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="已提交" value="1" />
            <el-option label="审批中" value="2" />
            <el-option label="已完成" value="3" />
            <el-option label="异常" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="对账周期">
          <el-date-picker
            v-model="filterForm.period"
            type="monthrange"
            range-separator="至"
            start-placeholder="开始月份"
            end-placeholder="结束月份"
            value-format="YYYY-MM"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="statementNo" label="对账单编号" width="160" />
        <el-table-column prop="projectName" label="项目名称" min-width="150" />
        <el-table-column prop="period" label="所属期间" width="100" />
        <el-table-column prop="contractAmount" label="合同金额" width="130" align="right">
          <template #default="{ row }">
            {{ formatAmount(row.contractAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="currentReceipt" label="当期回款" width="130" align="right">
          <template #default="{ row }">
            {{ formatAmount(row.currentReceipt) }}
          </template>
        </el-table-column>
        <el-table-column prop="receivableBalance" label="应收余额" width="130" align="right">
          <template #default="{ row }">
            {{ formatAmount(row.receivableBalance) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="审批状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 1" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="primary" link @click="handleSubmit(row)">提交</el-button>
            <el-button type="primary" link @click="handleExportRow(row)">导出</el-button>
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
import { ref, reactive } from 'vue'

const filterForm = reactive({
  projectId: '',
  contractId: '',
  status: '',
  period: [] as string[]
})

const tableData = ref([
  { id: 1, statementNo: 'DZ202604001', projectName: 'XX项目一期', period: '2026-04', contractAmount: 5000000, currentReceipt: 1000000, receivableBalance: 4000000, status: 1 },
  { id: 2, statementNo: 'DZ202604002', projectName: 'XX项目二期', period: '2026-04', contractAmount: 3000000, currentReceipt: 800000, receivableBalance: 2200000, status: 2 },
  { id: 3, statementNo: 'DZ202603003', projectName: 'XX项目一期', period: '2026-03', contractAmount: 5000000, currentReceipt: 2000000, receivableBalance: 3000000, status: 3 }
])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 100
})

const formatAmount = (amount: number) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
}

const getStatusType = (status: number) => {
  const types = ['', 'warning', 'primary', 'success', 'danger']
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts = ['', '已提交', '审批中', '已完成', '异常']
  return texts[status] || '未知'
}

const handleSearch = () => {
  console.log('搜索', filterForm)
}

const handleReset = () => {
  filterForm.projectId = ''
  filterForm.contractId = ''
  filterForm.status = ''
  filterForm.period = []
}

const handleRefresh = () => {
  console.log('刷新')
}

const handleExport = () => {
  console.log('导出')
}

const handleView = (row: any) => {
  console.log('查看', row)
}

const handleEdit = (row: any) => {
  console.log('编辑', row)
}

const handleSubmit = (row: any) => {
  console.log('提交', row)
}

const handleExportRow = (row: any) => {
  console.log('导出单行', row)
}

const handleSizeChange = (size: number) => {
  pagination.size = size
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
}
</script>

<style scoped>
.reconciliation-container {
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

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.danger {
  color: #f56c6c;
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