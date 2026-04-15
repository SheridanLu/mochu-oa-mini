<template>
  <div class="report-page">
    <div class="page-header">
      <div class="header-left">
        <h2>报表中心</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>报表中心</el-breadcrumb-item>
          <el-breadcrumb-item>概览</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-date-picker
          v-model="period"
          type="month"
          value-format="YYYY-MM"
          placeholder="统计月份"
          @change="loadOverview"
        />
        <el-button :loading="loading" @click="loadOverview">刷新</el-button>
      </div>
    </div>

    <el-alert v-if="overview.period" type="info" :closable="false" class="period-bar" show-icon>
      当前统计周期：<strong>{{ overview.period }}</strong>（自然月）
    </el-alert>

    <el-row :gutter="16" class="metric-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="metric-card">
          <div class="metric-label">项目总数</div>
          <div class="metric-value primary">{{ overview.project?.total ?? '—' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="metric-card">
          <div class="metric-label">对账单（本月）</div>
          <div class="metric-value">{{ overview.reconciliation?.statementCount ?? '—' }}</div>
          <div class="metric-sub">本期回款 {{ formatAmount(overview.reconciliation?.currentReceiptSum) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="metric-card">
          <div class="metric-label">报销（本月）</div>
          <div class="metric-value warning">{{ formatAmount(overview.expense?.amount) }}</div>
          <div class="metric-sub">{{ overview.expense?.count ?? 0 }} 笔</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="metric-card">
          <div class="metric-label">成本归集（本月）</div>
          <div class="metric-value">{{ formatAmount(overview.cost?.amount) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="metric-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="metric-card">
          <div class="metric-label">发票开票（本月）</div>
          <div class="metric-value">{{ formatAmount(overview.invoice?.amount) }}</div>
          <div class="metric-sub">{{ overview.invoice?.count ?? 0 }} 张</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="metric-card">
          <div class="metric-label">付款申请待审</div>
          <div class="metric-value danger">{{ overview.paymentApply?.pendingCount ?? '—' }}</div>
          <div class="metric-sub">{{ formatAmount(overview.paymentApply?.pendingAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="metric-card">
          <div class="metric-label">采购订单（本月）</div>
          <div class="metric-value success">{{ overview.purchase?.orderCount ?? '—' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="metric-card">
          <div class="metric-label">对账差异额</div>
          <div class="metric-value">{{ formatAmount(overview.reconciliation?.differenceSum) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <div class="chart-section">
      <div class="chart-head">
        <h3 class="section-title">近月趋势</h3>
        <el-select v-model="trendMonths" style="width: 120px" @change="loadTrend">
          <el-option label="近 3 月" :value="3" />
          <el-option label="近 6 月" :value="6" />
          <el-option label="近 12 月" :value="12" />
        </el-select>
      </div>
      <el-card shadow="never" class="chart-card" v-loading="trendLoading">
        <div ref="chartRef" class="trend-chart" />
      </el-card>
    </div>

    <h3 class="section-title">专题报表</h3>
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="link-card" shadow="hover" @click="$router.push('/report/project')">
          <h3>项目进度</h3>
          <p>在库项目总数（全库实时），跳转列表与 CSV 导出</p>
          <el-button type="primary" link @click.stop="$router.push('/report/project')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="link-card" shadow="hover" @click="$router.push('/report/purchase')">
          <h3>采购分析</h3>
          <p>按自然月统计新建采购订单笔数</p>
          <el-button type="primary" link @click.stop="$router.push('/report/purchase')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="link-card" shadow="hover" @click="$router.push('/report/finance')">
          <h3>财务分析</h3>
          <p>所选月份报销、发票、成本与付款待审汇总</p>
          <el-button type="primary" link @click.stop="$router.push('/report/finance')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="link-card" shadow="hover" @click="$router.push('/report/custom')">
          <h3>自定义导出</h3>
          <p>按类型与日期区间导出 CSV</p>
          <el-button type="primary" link @click.stop="$router.push('/report/custom')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { ECharts } from 'echarts'
import { api } from '@/api'

const loading = ref(false)
const trendLoading = ref(false)
const period = ref('')
const trendMonths = ref(6)
const overview = ref<Record<string, any>>({})
const chartRef = ref<HTMLDivElement | null>(null)
let chartInst: ECharts | null = null

const num = (v: any) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const formatAmount = (v: any) => {
  if (v == null || v === '') return '—'
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(num(v))
}

const loadOverview = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {}
    if (period.value) {
      const [y, m] = period.value.split('-')
      params.year = parseInt(y, 10)
      params.month = parseInt(m, 10)
    }
    const res: any = await api.report.overview(params)
    if (res.code === 200 && res.data) {
      overview.value = res.data
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const resizeChart = () => chartInst?.resize()

const loadTrend = async () => {
  trendLoading.value = true
  try {
    const res: any = await api.report.trend({ months: trendMonths.value })
    if (res.code !== 200 || !res.data?.points) return
    await nextTick()
    if (!chartRef.value) return
    const points = res.data.points as any[]
    const x = points.map((p) => p.period)
    const toN = (v: any) => (v == null || v === '' ? 0 : Number(v))
    if (!chartInst) {
      const mod = await import('echarts')
      const echartsLib = (mod as { default?: typeof mod }).default ?? mod
      chartInst = echartsLib.init(chartRef.value)
    }
    chartInst.setOption(
      {
        color: ['#e6a23c', '#409eff', '#67c23a', '#f56c6c'],
        tooltip: { trigger: 'axis' },
        legend: { data: ['报销', '发票', '成本', '本期回款'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '56', top: '48', containLabel: true },
        xAxis: { type: 'category', data: x, boundaryGap: false },
        yAxis: { type: 'value', name: '元' },
        series: [
          { name: '报销', type: 'line', smooth: true, data: points.map((p) => toN(p.expenseAmount)) },
          { name: '发票', type: 'line', smooth: true, data: points.map((p) => toN(p.invoiceAmount)) },
          { name: '成本', type: 'line', smooth: true, data: points.map((p) => toN(p.costAmount)) },
          { name: '本期回款', type: 'line', smooth: true, data: points.map((p) => toN(p.receiptSum)) }
        ]
      },
      true
    )
    chartInst.resize()
  } catch (e: any) {
    ElMessage.error(e.message || '趋势图加载失败')
  } finally {
    trendLoading.value = false
  }
}

onMounted(async () => {
  const d = new Date()
  period.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
  await loadOverview()
  await nextTick()
  await loadTrend()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chartInst?.dispose()
  chartInst = null
})
</script>

<style scoped>
.report-page {
  padding: 16px 20px 32px;
  min-height: calc(100vh - 100px);
  background: #f5f7fa;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}
.page-header h2 {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.period-bar {
  margin-bottom: 16px;
  border-radius: 8px;
}
.metric-row {
  margin-bottom: 16px;
}
.metric-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  margin-bottom: 16px;
}
.metric-label {
  font-size: 13px;
  color: #909399;
}
.metric-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.metric-value.primary {
  color: #409eff;
}
.metric-value.warning {
  color: #e6a23c;
}
.metric-value.danger {
  color: #f56c6c;
}
.metric-value.success {
  color: #67c23a;
}
.metric-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
.section-title {
  margin: 8px 0 16px;
  font-size: 16px;
  font-weight: 600;
}
.chart-section {
  margin-bottom: 24px;
}
.chart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.chart-head .section-title {
  margin: 0;
}
.chart-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.trend-chart {
  width: 100%;
  height: 360px;
}
.link-card {
  text-align: center;
  padding: 24px 16px;
  cursor: pointer;
  border-radius: 8px;
  margin-bottom: 16px;
  transition: border-color 0.2s;
}
.link-card:hover {
  border-color: #409eff;
}
.link-card h3 {
  margin: 0 0 8px;
  font-size: 17px;
}
.link-card p {
  color: #909399;
  font-size: 13px;
  margin: 0 0 12px;
  min-height: 40px;
}
</style>
