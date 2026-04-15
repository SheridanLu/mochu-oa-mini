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
          <el-form :inline="true" :model="filterForm">
            <el-form-item label="供应商名称">
              <el-input v-model="filterForm.supplierName" placeholder="请输入名称" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="供应商类型">
              <el-select v-model="filterForm.supplierType" placeholder="请选择" clearable style="width: 140px">
                <el-option label="材料供应商" value="1" />
                <el-option label="设备供应商" value="2" />
                <el-option label="劳务供应商" value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="filterForm.status" placeholder="请选择" clearable style="width: 100px">
                <el-option label="正常" value="1" />
                <el-option label="禁用" value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card class="table-card" v-loading="loading">
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
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">查看</el-button>
                <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const router = useRouter()
const activeTab = ref('list')
const loading = ref(false)
const tableData = ref<any[]>([])
const filterForm = reactive({
  supplierName: '',
  supplierType: '',
  status: ''
})

const formatAmount = (amount: number) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)

const equipmentRank = ref<any[]>([])
const materialRank = ref<any[]>([])
const laborRank = ref<any[]>([])

const handleCreate = () => router.push('/supplier/create')
const handleEdit = (row: any) => router.push(`/supplier/edit?id=${row.id}`)
const handleView = (row: any) => router.push(`/supplier/edit?id=${row.id}&view=true`)

const fetchList = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: 1,
      pageSize: 200,
      supplierName: filterForm.supplierName || undefined,
      supplierType: filterForm.supplierType || undefined,
      status: filterForm.status || undefined
    }
    const res: any = await api.supplier.page(params)
    tableData.value = res.data?.records || []
  } catch (e: any) {
    ElMessage.error(e.message || '获取供应商列表失败')
  } finally {
    loading.value = false
  }
}

const mapRankItem = (item: any) => ({
  supplierName: item.supplierName || item.name || `供应商-${item.supplierId ?? ''}`,
  dealCount: Number(item.dealCount ?? item.orderCount ?? 0),
  dealAmount: Number(item.dealAmount ?? item.totalAmount ?? 0),
  rating: Number(item.rating ?? item.score ?? 0),
  supplierType: Number(item.supplierType ?? item.type ?? 0),
})

const fetchRanks = async () => {
  try {
    const year = new Date().getFullYear()
    const res: any = await api.supplier.rating.list({ year })
    const raw = Array.isArray(res.data) ? res.data : []
    const rows = raw.map(mapRankItem)
    equipmentRank.value = rows.filter((r: any) => r.supplierType === 2)
    materialRank.value = rows.filter((r: any) => r.supplierType === 1)
    laborRank.value = rows.filter((r: any) => r.supplierType === 3)
  } catch {
    equipmentRank.value = []
    materialRank.value = []
    laborRank.value = []
  }
}

const handleSearch = () => {
  fetchList()
}

const handleReset = () => {
  Object.assign(filterForm, { supplierName: '', supplierType: '', status: '' })
  fetchList()
}

onMounted(() => {
  fetchList()
  fetchRanks()
})

watch(activeTab, (tab) => {
  if (tab !== 'list') fetchRanks()
})
</script>
<style scoped>.supplier-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.supplier-tabs { margin-top: 20px; }
.rank-header { display: flex; justify-content: space-between; align-items: center; }
</style>