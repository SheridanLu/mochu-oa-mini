<template>
  <div class="supplier-container">
    <div class="page-header">
      <div class="header-left">
        <h2>供应商管理</h2>
        <el-breadcrumb separator="/"><el-breadcrumb-item>供应商管理</el-breadcrumb-item></el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">新增供应商</el-button>
      </div>
    </div>
    <el-tabs v-model="activeTab" class="supplier-tabs">
      <el-tab-pane label="供应商列表" name="list">
        <el-card class="filter-card">
          <el-form :inline="true">
            <el-form-item label="供应商类型">
              <el-select placeholder="请选择" style="width: 140px">
                <el-option label="材料供应商" value="1" />
                <el-option label="设备供应商" value="2" />
                <el-option label="劳务供应商" value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select placeholder="请选择" style="width: 100px">
                <el-option label="正常" value="1" />
                <el-option label="禁用" value="2" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary">搜索</el-button></el-form-item>
          </el-form>
        </el-card>
        <el-card class="table-card">
          <el-table :data="tableData" stripe>
            <el-table-column prop="supplierNo" label="供应商编号" width="130" />
            <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
            <el-table-column prop="supplierType" label="类型" width="100" align="center">
              <template #default="{ row }">{{ ['','材料供应商','设备供应商','劳务供应商','专业分包商'][row.supplierType] }}</template>
            </el-table-column>
            <el-table-column prop="contactName" label="联系人" width="100" />
            <el-table-column prop="contactPhone" label="联系电话" width="120" />
            <el-table-column prop="rating" label="评级" width="80" align="center">
              <template #default="{ row }">
                <el-rate v-model="row.rating" disabled text-color="#ff9900" />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default>
                <el-button type="primary" link>查看</el-button>
                <el-button type="primary" link>编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="设备供应商榜" name="equipment">
        <el-card class="table-card">
          <template #header>
            <div class="rank-header">
              <span>设备供应商排行榜</span>
              <el-tag type="warning">按成交量50%+成交金额50%权重排序</el-tag>
            </div>
          </template>
          <el-table :data="equipmentRank" stripe>
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
            <el-table-column prop="dealCount" label="成交量" width="100" align="center" />
            <el-table-column prop="dealAmount" label="成交金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.dealAmount) }}</template>
            </el-table-column>
            <el-table-column prop="rating" label="综合评分" width="100" align="center">
              <template #default="{ row }">
                <el-rate v-model="row.rating" disabled text-color="#ff9900" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="材料供应商榜" name="material">
        <el-card class="table-card">
          <template #header>
            <div class="rank-header">
              <span>材料供应商排行榜</span>
              <el-tag type="warning">按成交量50%+成交金额50%权重排序</el-tag>
            </div>
          </template>
          <el-table :data="materialRank" stripe>
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
            <el-table-column prop="dealCount" label="成交量" width="100" align="center" />
            <el-table-column prop="dealAmount" label="成交金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.dealAmount) }}</template>
            </el-table-column>
            <el-table-column prop="rating" label="综合评分" width="100" align="center">
              <template #default="{ row }">
                <el-rate v-model="row.rating" disabled text-color="#ff9900" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="劳务供应商榜" name="labor">
        <el-card class="table-card">
          <template #header>
            <div class="rank-header">
              <span>劳务供应商排行榜</span>
              <el-tag type="warning">按成交量50%+成交金额50%权重排序</el-tag>
            </div>
          </template>
          <el-table :data="laborRank" stripe>
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
            <el-table-column prop="dealCount" label="成交量" width="100" align="center" />
            <el-table-column prop="dealAmount" label="成交金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.dealAmount) }}</template>
            </el-table-column>
            <el-table-column prop="rating" label="综合评分" width="100" align="center">
              <template #default="{ row }">
                <el-rate v-model="row.rating" disabled text-color="#ff9900" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
const activeTab = ref('list')
const tableData = ref([
  { id: 1, supplierNo: 'GYS2026001', supplierName: 'XX建材有限公司', supplierType: 1, contactName: '张经理', contactPhone: '13800138000', rating: 5, status: 1 }
])
const formatAmount = (amount: number) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
const equipmentRank = ref([
  { supplierName: 'XX设备有限公司', dealCount: 25, dealAmount: 5800000, rating: 5 },
  { supplierName: 'XX机械公司', dealCount: 18, dealAmount: 4200000, rating: 4 },
  { supplierName: 'XX机电设备', dealCount: 12, dealAmount: 2800000, rating: 4 }
])
const materialRank = ref([
  { supplierName: 'XX建材有限公司', dealCount: 156, dealAmount: 12600000, rating: 5 },
  { supplierName: 'XX钢材贸易', dealCount: 89, dealAmount: 9800000, rating: 4 },
  { supplierName: 'XX化工材料', dealCount: 67, dealAmount: 5600000, rating: 4 }
])
const laborRank = ref([
  { supplierName: 'XX劳务派遣', dealCount: 320, dealAmount: 9600000, rating: 5 },
  { supplierName: 'XX建筑劳务', dealCount: 245, dealAmount: 7350000, rating: 4 },
  { supplierName: 'XX人力外包', dealCount: 180, dealAmount: 5400000, rating: 4 }
])
const handleCreate = () => console.log('新增供应商')
</script>
<style scoped>.supplier-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.supplier-tabs { margin-top: 20px; }
.rank-header { display: flex; justify-content: space-between; align-items: center; }
</style>