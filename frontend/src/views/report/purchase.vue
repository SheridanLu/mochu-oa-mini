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
      <h2>采购分析报表</h2>
      <p class="desc">所选自然月内新建的采购订单笔数（与报表概览同源接口）。</p>
    </div>
    <el-card v-loading="loading" shadow="never" class="metric-card">
      <div class="k">采购订单数</div>
      <div class="v success">{{ data.purchase?.orderCount ?? '—' }}</div>
      <p class="hint">金额明细可在采购模块中查看；自定义导出支持按日期区间拉取关联数据。</p>
      <el-button type="primary" style="margin-top: 12px" @click="$router.push('/purchase')">打开采购清单</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)
const period = ref('')
const data = ref<any>({})

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
  max-width: 640px;
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
.metric-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.k {
  font-size: 13px;
  color: #909399;
}
.v {
  font-size: 28px;
  font-weight: 600;
  margin-top: 8px;
}
.v.success {
  color: #67c23a;
}
.hint {
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}
</style>
