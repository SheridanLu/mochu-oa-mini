<template>
  <div class="cost-container">
    <div class="page-header">
      <div class="header-left">
        <h2>成本归集</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>成本归集</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleExport">导出</el-button>
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
        <el-form-item label="成本类别">
          <el-select v-model="filterForm.costCategory" placeholder="请选择" clearable style="width: 140px">
            <el-option label="材料费" value="1" />
            <el-option label="人工费" value="2" />
            <el-option label="机械费" value="3" />
            <el-option label="管理费" value="4" />
            <el-option label="其他" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属期间">
          <el-date-picker v-model="filterForm.period" type="monthrange" range-separator="至" start-placeholder="开始月份" end-placeholder="结束月份" value-format="YYYY-MM" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" stripe style="width: 100%" show-summary>
        <el-table-column prop="projectName" label="项目" min-width="150" />
        <el-table-column prop="costCategoryName" label="成本类别" width="100" />
        <el-table-column prop="amount" label="金额" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="sourceType" label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag>{{ getSourceTypeText(row.sourceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceNo" label="来源单据号" width="140" />
        <el-table-column prop="collectedDate" label="归集日期" width="110" />
        <el-table-column prop="period" label="归属期间" width="100" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const filterForm = reactive({ projectId: '', costCategory: '', period: [] as string[] })
const tableData = ref([
  { id: 1, projectName: 'XX项目', costCategoryName: '材料费', amount: 125000, sourceType: 2, sourceNo: 'CG202604001', collectedDate: '2026-04-10', period: '2026-04', remark: '钢材采购' },
  { id: 2, projectName: 'XX项目', costCategoryName: '人工费', amount: 45000, sourceType: 1, sourceNo: 'BX202604002', collectedDate: '2026-04-08', period: '2026-04', remark: '施工人员工资' }
])
const pagination = reactive({ page: 1, size: 20, total: 35 })

const formatAmount = (amount: number) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
const getSourceTypeText = (type: number) => ['', '报销', '采购', '付款', '工资', '其他'][type] || ''
const handleSearch = () => { pagination.page = 1 }
const handleReset = () => { filterForm.projectId = ''; filterForm.costCategory = ''; filterForm.period = []; pagination.page = 1 }
const handleExport = () => { ElMessage.info('导出功能开发中') }
const handleCreate = () => ElMessage.info('新增成本功能开发中')
</script>

<style scoped>
.cost-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
</style>