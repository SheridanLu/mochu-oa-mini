<template>
  <div class="sub-report">
    <div class="page-header">
      <div class="header-top">
        <el-button text @click="$router.push('/report')">← 返回概览</el-button>
        <div class="header-actions">
          <el-button :loading="loading" @click="loadData">刷新</el-button>
          <el-button type="primary" plain @click="$router.push('/report/custom')">自定义导出 CSV</el-button>
        </div>
      </div>
      <h2>项目进度报表</h2>
      <p class="desc">项目总数为当前库内全量统计，与月份无关；详细进度在「项目管理」中维护。</p>
    </div>
    <el-alert type="info" :closable="false" class="info-bar" show-icon>
      报表概览中的「项目总数」为实时计数；若需按项目筛选导出报销/发票等，请使用自定义导出并选择项目。
    </el-alert>
    <el-card v-loading="loading" shadow="never" class="metric-card">
      <div class="k">在库项目数</div>
      <div class="v primary">{{ data.project?.total ?? '—' }}</div>
      <el-button type="primary" style="margin-top: 16px" @click="$router.push('/project')">打开项目列表</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)
const data = ref<any>({})

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await api.report.overview()
    if (res.code === 200) data.value = res.data || {}
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.sub-report {
  padding: 20px;
  max-width: 640px;
}
.page-header {
  margin-bottom: 16px;
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
.info-bar {
  margin-bottom: 16px;
  border-radius: 8px;
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
.v.primary {
  color: #409eff;
}
</style>
