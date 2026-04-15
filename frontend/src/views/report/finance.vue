<template>
  <div class="sub-report">
    <div class="page-header">
      <div class="header-top">
        <el-button text @click="$router.push('/report')">← 返回概览</el-button>
        <div class="header-actions">
          <el-date-picker
            v-model="period"
            type="month"
            value-format="YYYY-MM"
            placeholder="统计月份"
            @change="loadData"
          />
          <el-button :loading="loading" @click="loadData">刷新</el-button>
          <el-button type="primary" plain @click="$router.push('/report/custom')">自定义导出 CSV</el-button>
        </div>
      </div>
      <h2>财务分析报表</h2>
      <p class="desc">所选自然月内报销、发票、成本与付款待审汇总（与报表概览同源接口）。</p>
    </div>
    <el-row v-loading="loading" :gutter="16">
      <el-col :span="12">
        <el-card><div class="k">报销金额</div><div class="v">{{ fmt(data.expense?.amount) }}</div><div class="s">{{ data.expense?.count }} 笔</div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><div class="k">发票（开票）</div><div class="v">{{ fmt(data.invoice?.amount) }}</div><div class="s">{{ data.invoice?.count }} 张</div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><div class="k">成本归集</div><div class="v">{{ fmt(data.cost?.amount) }}</div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><div class="k">付款申请待审</div><div class="v">{{ data.paymentApply?.pendingCount }} 笔</div><div class="s">{{ fmt(data.paymentApply?.pendingAmount) }}</div></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)
const period = ref('')
const data = ref<any>({})
const fmt = (v: any) =>
  v == null
    ? '—'
    : new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(Number(v) || 0)

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {}
    if (period.value) {
      const [y, m] = period.value.split('-')
      params.year = parseInt(y, 10)
      params.month = parseInt(m, 10)
    }
    const res: any = await api.report.overview(params)
    if (res.code === 200) data.value = res.data || {}
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const d = new Date()
  period.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
  await loadData()
})
</script>

<style scoped>
.sub-report {
  padding: 20px;
  max-width: 960px;
}
.page-header {
  margin-bottom: 20px;
}
.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.desc {
  color: #909399;
  font-size: 14px;
}
.k {
  font-size: 13px;
  color: #909399;
}
.v {
  font-size: 22px;
  font-weight: 600;
  margin: 8px 0;
}
.s {
  font-size: 12px;
  color: #606266;
}
</style>
