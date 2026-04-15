<template>
  <div v-loading="loading" class="detail-container">
    <div class="page-header">
      <div class="header-left">
        <h2>收入对账单详情</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>收入对账</el-breadcrumb-item>
          <el-breadcrumb-item>详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleBack">返回</el-button>
        <el-button v-if="canEdit" type="primary" @click="handleEdit">编辑</el-button>
        <el-button v-if="canSubmit" type="primary" @click="handleSubmit">提交审批</el-button>
        <el-button v-if="canExport" @click="handleExport">导出</el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>基础信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="对账单编号">{{ data.statementNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(data.status)">{{ getStatusText(data.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="关联项目">{{ data.projectName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="关联合同">{{ data.contractNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="对账期间">{{ data.period }}</el-descriptions-item>
            <el-descriptions-item label="创建人">{{ data.creatorName || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="amount-card" shadow="never" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span>金额概览</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="amount-item">
                <div class="amount-label">合同含税金额</div>
                <div class="amount-value">{{ formatAmount(data.contractAmount) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="amount-item">
                <div class="amount-label">当期产值</div>
                <div class="amount-value primary">{{ formatAmount(data.currentProduction) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="amount-item">
                <div class="amount-label">累计产值</div>
                <div class="amount-value">{{ formatAmount(data.accumulatedProduction) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="amount-item">
                <div class="amount-label">当期回款</div>
                <div class="amount-value success">{{ formatAmount(data.currentReceipt) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="amount-item">
                <div class="amount-label">累计回款</div>
                <div class="amount-value success">{{ formatAmount(data.accumulatedReceipt) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="amount-item">
                <div class="amount-label">应收余额</div>
                <div class="amount-value warning">{{ formatAmount(data.receivableBalance) }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <el-card class="attachment-card" shadow="never" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span>附件</span>
              <el-button type="primary" link>+ 上传</el-button>
            </div>
          </template>
          <el-table :data="attachmentList" stripe>
            <el-table-column prop="fileName" label="文件名" min-width="160" />
            <el-table-column prop="fileSize" label="大小" width="100">
              <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="creatorName" label="上传人" width="100" />
            <el-table-column prop="createdAt" label="上传时间" width="160">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handlePreview(row)">预览</el-button>
                <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="diff-detail-card" shadow="never" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span>差异明细</span>
              <div class="header-actions">
                <el-radio-group v-model="diffMode" size="small">
                  <el-radio-button value="all">全部</el-radio-button>
                  <el-radio-button value="diff">仅差异</el-radio-button>
                </el-radio-group>
                <el-button size="small" @click="handleExport">导出 Excel/PDF</el-button>
              </div>
            </div>
          </template>
          <div class="mini-chart" aria-label="上期与本期对比">
            <div class="bar-group">
              <span class="bar-label">上期</span>
              <div class="bar-track">
                <div class="bar bar-prev" :style="{ width: barPrevWidth }" />
              </div>
              <span class="bar-value">{{ formatAmount(lastPeriodAmount) }}</span>
            </div>
            <div class="bar-group">
              <span class="bar-label">本期</span>
              <div class="bar-track">
                <div class="bar bar-cur" :style="{ width: barCurWidth }" />
              </div>
              <span class="bar-value">{{ formatAmount(currentPeriodAmount) }}</span>
            </div>
          </div>
          <el-table :data="filteredDiffRows" stripe class="diff-table" max-height="360">
            <el-table-column prop="prevLabel" label="上期（说明）" min-width="140" />
            <el-table-column prop="prevAmount" label="上期金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.prevAmount) }}</template>
            </el-table-column>
            <el-table-column prop="diffAmount" label="差异" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.diffAmount !== 0 ? 'diff-strong' : ''">{{ formatSigned(row.diffAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="currAmount" label="本期金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.currAmount) }}</template>
            </el-table-column>
            <el-table-column prop="currLabel" label="本期（说明）" min-width="140" />
            <el-table-column prop="remark" label="原因备注" min-width="120" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="diff-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>差异分析</span>
            </div>
          </template>
          <div class="diff-summary">
            <div class="diff-item">
              <span class="diff-label">上期金额</span>
              <span class="diff-value">{{ formatAmount(lastPeriodAmount) }}</span>
            </div>
            <div class="diff-item">
              <span class="diff-label">本期金额</span>
              <span class="diff-value">{{ formatAmount(currentPeriodAmount) }}</span>
            </div>
            <div class="diff-item">
              <span class="diff-label">差异金额</span>
              <span class="diff-value" :class="diffAmount >= 0 ? 'up' : 'down'">
                {{ diffAmount >= 0 ? '+' : '' }}{{ formatAmount(diffAmount) }}
              </span>
            </div>
            <div class="diff-item">
              <span class="diff-label">差异率</span>
              <span class="diff-value" :class="diffRate >= 0 ? 'up' : 'down'">
                {{ diffRate >= 0 ? '+' : '' }}{{ formatPercentDisplay(diffRate) }}
              </span>
            </div>
          </div>
        </el-card>

        <el-card class="approval-card" shadow="never" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span>审批记录</span>
            </div>
          </template>
          <el-empty v-if="approvalLog.length === 0" description="暂无审批记录" :image-size="64" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="item in approvalLog"
              :key="item.id"
              :timestamp="formatDateTime(item.operateTime)"
              :type="getApprovalType(item.actionResult)"
              placement="top"
            >
              <div class="approval-item">
                <div class="approval-user">{{ item.operatorName }}</div>
                <div class="approval-action">{{ getActionText(item.actionType, item.actionResult) }}</div>
                <div v-if="item.opinion" class="approval-opinion">{{ item.opinion }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card class="action-card" shadow="never" style="margin-top: 16px" v-if="canApprove">
          <template #header>
            <div class="card-header">
              <span>审批操作</span>
            </div>
          </template>
          <el-form :model="approvalForm" label-width="80px">
            <el-form-item label="审批意见">
              <el-input v-model="approvalForm.opinion" type="textarea" :rows="3" placeholder="请输入审批意见" />
            </el-form-item>
          </el-form>
          <div class="action-buttons">
            <el-button type="danger" @click="handleReject">驳回</el-button>
            <el-button type="success" @click="handleApprove">通过</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatAmount as formatAmountUtil, formatDateTime } from '../../../../utils/format'
import { api } from '@/api'

const router = useRouter()
const route = useRoute()

const id = computed(() => Number(route.query.id) || 0)

const loading = ref(false)
const diffMode = ref<'all' | 'diff'>('all')
const data = reactive<any>({
  statementNo: '',
  status: 1,
  projectName: '',
  contractNo: '',
  period: '',
  creatorName: '',
  contractAmount: 0,
  currentProduction: 0,
  accumulatedProduction: 0,
  currentReceipt: 0,
  accumulatedReceipt: 0,
  receivableBalance: 0,
  differenceAmount: 0
})

const attachmentList = ref<any[]>([])
const approvalLog = ref<any[]>([])
const approvalForm = reactive({ opinion: '' })

const lastPeriodAmount = ref(0)
const currentPeriodAmount = ref(0)
const diffDetailRows = ref<any[]>([])

const diffAmount = computed(() => currentPeriodAmount.value - lastPeriodAmount.value)
const diffRate = computed(() => (lastPeriodAmount.value !== 0 ? diffAmount.value / lastPeriodAmount.value : 0))

const maxBar = computed(() => Math.max(lastPeriodAmount.value, currentPeriodAmount.value, 1))
const barPrevWidth = computed(() => `${Math.round((lastPeriodAmount.value / maxBar.value) * 100)}%`)
const barCurWidth = computed(() => `${Math.round((currentPeriodAmount.value / maxBar.value) * 100)}%`)

const filteredDiffRows = computed(() => {
  if (diffMode.value === 'diff') {
    return diffDetailRows.value.filter((r) => r.diffAmount !== 0)
  }
  return diffDetailRows.value
})

const canEdit = computed(() => data.status === 1)
const canSubmit = computed(() => data.status === 1)
const canExport = computed(() => true)
const canApprove = computed(() => data.status === 2 || data.status === 3)

const getStatusType = (status: number) => {
  const types: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'primary', 4: 'success', 5: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    1: '草稿',
    2: '待审批',
    3: '审批中',
    4: '已完成',
    5: '已驳回'
  }
  return texts[status] || '未知'
}

const formatAmount = (amount: number | null | undefined) => formatAmountUtil(Number(amount) || 0)

const formatSigned = (n: number) => {
  const v = Number(n) || 0
  return (v >= 0 ? '+' : '') + formatAmount(v)
}

const formatPercentDisplay = (ratio: number) => `${(ratio * 100).toFixed(2)}%`

const formatFileSize = (size: number) => {
  if (!size) return '-'
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  return `${(size / 1024 / 1024).toFixed(1)}MB`
}

const getApprovalType = (result: string) => {
  if (result === 'approved') return 'success'
  if (result === 'rejected') return 'danger'
  return 'primary'
}

const getActionText = (type: string, result: string) => {
  if (type === 'submit') return '提交审批'
  if (result === 'approved') return '审批通过'
  if (result === 'rejected') return '审批驳回'
  return '审批中'
}

const buildDiffRows = () => {
  const prev = lastPeriodAmount.value
  const cur = currentPeriodAmount.value
  const contract = Number(data.contractAmount) || 0
  const recv = Number(data.accumulatedReceipt) || 0
  diffDetailRows.value = [
    {
      id: 1,
      prevLabel: '产值（上期）',
      prevAmount: prev,
      currAmount: cur,
      diffAmount: cur - prev,
      currLabel: '产值（本期）',
      remark: '—'
    },
    {
      id: 2,
      prevLabel: '合同含税（参考）',
      prevAmount: contract * 0.9,
      currAmount: contract,
      diffAmount: contract * 0.1,
      currLabel: '合同含税（当前）',
      remark: '示例行，接口对接后替换为真实明细'
    },
    {
      id: 3,
      prevLabel: '累计回款（上期）',
      prevAmount: recv * 0.85,
      currAmount: recv,
      diffAmount: recv * 0.15,
      currLabel: '累计回款（本期）',
      remark: '—'
    }
  ]
}

const fetchDetail = async () => {
  if (!id.value) return
  loading.value = true
  try {
    const res: any = await api.statement.get(id.value)
    if (res.code === 200 && res.data) {
      const d = res.data
      Object.assign(data, d)
      const acc = Number(d.accumulatedProduction) || 0
      const curP = Number(d.currentProduction) || 0
      currentPeriodAmount.value = acc || curP
      lastPeriodAmount.value = Math.max(0, currentPeriodAmount.value - Math.abs(Number(d.differenceAmount) || 0))
      buildDiffRows()
    }
  } catch (e: any) {
    ElMessage.error(e.message || '获取详情失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => router.back()
const handleEdit = () => router.push(`/finance/reconciliation/edit?id=${id.value}`)
const handleSubmit = async () => {
  try {
    const res: any = await api.statement.submit(id.value)
    if (res.code === 200) {
      ElMessage.success('提交成功')
      fetchDetail()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  }
}
const handleExport = () => ElMessage.info('导出对接中（设计说明：Excel / PDF）')

const handlePreview = (row: any) => {
  if (row.previewUrl) window.open(row.previewUrl)
  else ElMessage.info('暂无可预览地址')
}

const handleDownload = (row: any) => {
  if (row.downloadUrl) window.open(row.downloadUrl)
  else ElMessage.info('下载地址待对接存储服务')
}

const handleApprove = () => {
  ElMessage.info('对账审批流接口待对接，请使用审批中心统一处理')
}

const handleReject = () => {
  ElMessage.info('对账审批流接口待对接，请使用审批中心统一处理')
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.detail-container {
  padding: 20px;
  min-height: 320px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  gap: 12px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.amount-card .amount-item {
  text-align: center;
  padding: 15px;
}
.amount-card .amount-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.amount-card .amount-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
.amount-card .amount-value.primary {
  color: #409eff;
}
.amount-card .amount-value.success {
  color: #67c23a;
}
.amount-card .amount-value.warning {
  color: #e6a23c;
}
.mini-chart {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}
.bar-group {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.bar-label {
  width: 36px;
  font-size: 12px;
  color: #909399;
}
.bar-track {
  flex: 1;
  height: 10px;
  background: #ebeef5;
  border-radius: 6px;
  overflow: hidden;
}
.bar {
  height: 100%;
  border-radius: 6px;
  transition: width 0.3s ease;
}
.bar-prev {
  background: #409eff;
}
.bar-cur {
  background: #67c23a;
}
.bar-value {
  min-width: 96px;
  text-align: right;
  font-size: 13px;
  color: #606266;
}
.diff-table .diff-strong {
  color: #f56c6c;
  font-weight: 600;
}
.diff-card .diff-summary {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.diff-card .diff-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.diff-card .diff-label {
  color: #909399;
}
.diff-card .diff-value {
  font-weight: 500;
}
.diff-card .diff-value.up {
  color: #f56c6c;
}
.diff-card .diff-value.down {
  color: #67c23a;
}
.approval-card .approval-item {
  padding: 8px 0;
}
.approval-card .approval-user {
  font-weight: 500;
  margin-bottom: 4px;
}
.approval-card .approval-action {
  font-size: 14px;
  color: #909399;
}
.approval-card .approval-opinion {
  margin-top: 4px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}
.action-card .action-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
