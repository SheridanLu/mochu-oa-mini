<template>
  <div class="payment-plan-container">
    <div class="page-header">
      <div class="header-left">
        <h2>付款计划管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>付款计划</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="月份">
          <el-date-picker v-model="filterForm.month" type="month" placeholder="选择月份" value-format="YYYY-MM" style="width: 150px" />
        </el-form-item>
        <el-form-item label="项目">
          <el-select v-model="filterForm.projectId" placeholder="请选择项目" clearable style="width: 200px">
            <el-option label="XX项目一" value="1" />
            <el-option label="XX项目二" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="款项类型">
          <el-select v-model="filterForm.paymentType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="材料款" value="1" />
            <el-option label="设备款" value="2" />
            <el-option label="人工费" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-alert type="warning" :closable="false" style="margin-bottom: 20px">
      <template #title>
        <span style="font-weight: 500">支出合同付款计划表仅供系统内部查看，禁止导出与打印</span>
      </template>
    </el-alert>

    <el-card class="table-card">
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="contractNo" label="合同编号" width="140" />
        <el-table-column prop="contractName" label="合同名称" min-width="180" />
        <el-table-column prop="plannedAmount" label="计划金额" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.plannedAmount) }}</template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="已付金额" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column prop="currentPayable" label="本次应付" width="130" align="right">
          <template #default="{ row }">
            <span class="highlight">{{ formatAmount(row.currentPayable) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="计划付款日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
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
import { ref, reactive } from 'vue'

const filterForm = reactive({ month: '', projectId: '', paymentType: '' })
const tableData = ref([
  { id: 1, contractNo: 'ZC001', contractName: '材料采购合同', plannedAmount: 500000, paidAmount: 300000, currentPayable: 200000, paymentDate: '2026-04-25', status: 1 },
  { id: 2, contractNo: 'ZC002', contractName: '设备租赁合同', plannedAmount: 800000, paidAmount: 400000, currentPayable: 400000, paymentDate: '2026-04-20', status: 2 }
])
const pagination = reactive({ page: 1, size: 20, total: 30 })

const formatAmount = (amount: number) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
const getStatusType = (status: number) => ['', 'warning', 'success', 'info'][status] || 'info'
const getStatusText = (status: number) => ['', '待付款', '已付', '已结清'][status] || ''
const handleSearch = () => { pagination.page = 1 }
const handleReset = () => { filterForm.month = ''; filterForm.projectId = ''; filterForm.paymentType = ''; pagination.page = 1 }
const handleRefresh = () => { ElMessage.success('刷新成功') }
const handleView = (row: any) => { ElMessage.info('查看详情: ' + row.planNo) }
const handleCreate = () => ElMessage.info('新增付款计划功能开发中')
</script>

<style scoped>
.payment-plan-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
.highlight { color: #409eff; font-weight: 600; }
</style>