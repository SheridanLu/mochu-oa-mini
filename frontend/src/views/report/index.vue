<template>
  <div class="report-container">
    <div class="page-header">
      <div class="header-left">
        <h2>报表中心</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>报表中心</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="chart-card">
          <h3>项目进度报表</h3>
          <p>项目进度与完成情况统计</p>
          <el-button type="primary" @click="$router.push('/report/project')">查看</el-button>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="chart-card">
          <h3>采购分析报表</h3>
          <p>采购成本与供应商分析</p>
          <el-button type="primary" @click="$router.push('/report/purchase')">查看</el-button>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="chart-card">
          <h3>财务分析报表</h3>
          <p>收入支出与成本分析</p>
          <el-button type="primary" @click="$router.push('/report/finance')">查看</el-button>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="chart-card">
          <h3>自定义报表</h3>
          <p>自定义查询与导出</p>
          <el-button type="primary" @click="$router.push('/report/custom')">查看</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.report-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.chart-card { text-align: center; padding: 30px; }
.chart-card h3 { margin: 0 0 10px; font-size: 18px; }
.chart-card p { color: #909399; margin-bottom: 20px; }
</style>