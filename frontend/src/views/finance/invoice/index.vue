<template>
  <div class="invoice-container">
    <div class="page-header">
      <div class="header-left">
        <h2>发票管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>发票管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="发票类型">
          <el-select v-model="filterForm.invoiceType" placeholder="请选择" clearable style="width: 150px">
            <el-option label="增值税专用发票" value="1" />
            <el-option label="增值税普通发票" value="2" />
            <el-option label="电子发票" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="filterForm.verified" placeholder="请选择" clearable style="width: 120px">
            <el-option label="已认证" value="1" />
            <el-option label="未认证" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目">
          <el-select v-model="filterForm.projectId" placeholder="请选择项目" clearable style="width: 200px">
            <el-option label="XX项目一" value="1" />
            <el-option label="XX项目二" value="2" />
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
        <el-table-column prop="invoiceNo" label="发票编号" width="150" />
        <el-table-column prop="invoiceType" label="发票类型" width="130" align="center">
          <template #default="{ row }">{{ getInvoiceTypeText(row.invoiceType) }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="openedDate" label="开票日期" width="110" />
        <el-table-column prop="projectName" label="关联项目" min-width="140" />
        <el-table-column prop="isVerified" label="认证状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isVerified === 1" type="success">已认证</el-tag>
            <el-tag v-else type="warning">未认证</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expireWarning" label="过期预警" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.expireWarning" type="danger">即将过期</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="!row.isVerified" type="primary" link @click="handleVerify(row)">认证</el-button>
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

const filterForm = reactive({ invoiceType: '', verified: '', projectId: '' })
const tableData = ref([
  { id: 1, invoiceNo: 'FP202604001', invoiceType: 1, amount: 50000, openedDate: '2026-04-10', projectName: 'XX项目', isVerified: 1, expireWarning: false },
  { id: 2, invoiceNo: 'FP202604002', invoiceType: 2, amount: 12000, openedDate: '2026-04-05', projectName: 'XX项目', isVerified: 0, expireWarning: true },
  { id: 3, invoiceNo: 'FP202603015', invoiceType: 3, amount: 8500, openedDate: '2026-03-20', projectName: 'XX项目', isVerified: 1, expireWarning: false }
])
const pagination = reactive({ page: 1, size: 20, total: 45 })

const formatAmount = (amount: number) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
const getInvoiceTypeText = (type: number) => ['', '增值税专用发票', '增值税普通发票', '电子发票'][type] || ''
const handleSearch = () => console.log('搜索', filterForm)
const handleReset = () => { filterForm.invoiceType = ''; filterForm.verified = ''; filterForm.projectId = '' }
const handleRefresh = () => console.log('刷新')
const handleView = (row: any) => console.log('查看', row)
const handleVerify = (row: any) => console.log('认证', row)
</script>

<style scoped>
.invoice-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
</style>