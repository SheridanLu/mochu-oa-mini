<template>
  <div class="detail-container">
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
        <el-card class="info-card">
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
            <el-descriptions-item label="关联项目">{{ data.projectName }}</el-descriptions-item>
            <el-descriptions-item label="关联合同">{{ data.contractNo }}</el-descriptions-item>
            <el-descriptions-item label="对账期间">{{ data.period }}</el-descriptions-item>
            <el-descriptions-item label="创建人">{{ data.creatorName }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="amount-card" style="margin-top: 20px">
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
                <div class="amount-value primary">{{ formatAmount(data.currentOutput) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="amount-item">
                <div class="amount-label">累计产值</div>
                <div class="amount-value">{{ formatAmount(data.totalOutput) }}</div>
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
                <div class="amount-value success">{{ formatAmount(data.totalReceipt) }}</div>
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

        <el-card class="attachment-card" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>附件</span>
              <el-button type="primary" link>+上传</el-button>
            </div>
          </template>
          <el-table :data="attachmentList" stripe>
            <el-table-column prop="fileName" label="文件名" />
            <el-table-column prop="fileSize" label="大小" width="100">
              <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="creatorName" label="上传人" width="100" />
            <el-table-column prop="createdAt" label="上传时间" width="160">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handlePreview(row)">预览</el-button>
                <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="diff-card">
          <template #header>
            <div class="card-header">
              <span>差异分析</span>
              <el-radio-group v-model="diffMode" size="small">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="diff">仅差异</el-radio-button>
              </el-radio-group>
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
                {{ diffRate >= 0 ? '+' : '' }}{{ formatPercent(diffRate) }}
              </span>
            </div>
          </div>
        </el-card>

        <el-card class="approval-card" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>审批记录</span>
            </div>
          </template>
          <el-timeline>
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

        <el-card class="action-card" style="margin-top: 20px" v-if="canApprove">
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
import { formatAmount as formatAmountUtil, formatDateTime, formatPercent } from '../../../../utils/format'
import api from '../../../../api'

const router = useRouter()
const route = useRoute()

const id = computed(() => Number(route.query.id) || 0)

const loading = ref(false)
const diffMode = ref('all')
const data = reactive<any>({
  statementNo: '',
  status: 1,
  projectName: '',
  contractNo: '',
  period: '',
  creatorName: '',
  contractAmount: 0,
  currentOutput: 0,
  totalOutput: 0,
  currentReceipt: 0,
  totalReceipt: 0,
  receivableBalance: 0
})

const attachmentList = ref<any[]>([])
const approvalLog = ref<any[]>([])
const approvalForm = reactive({ opinion: '' })

const lastPeriodAmount = ref(0)
const currentPeriodAmount = ref(0)

const diffAmount = computed(() => currentPeriodAmount.value - lastPeriodAmount.value)
const diffRate = computed(() => lastPeriodAmount.value > 0 ? diffAmount.value / lastPeriodAmount.value : 0)

const canEdit = computed(() => data.status === 1)
const canSubmit = computed(() => data.status === 1)
const canExport = computed(() => true)
const canApprove = computed(() => data.status === 2)

const getStatusType = (status: number) => {
  const types = ['', 'warning', 'primary', 'success', 'danger']
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts = ['', '已提交', '审批中', '已完成', '异常']
  return texts[status] || '未知'
}

const formatAmount = (amount: number) => formatAmountUtil(amount)
const formatPercent = (value: number) => formatPercentUtil(value)
const formatFileSize = (size: number) => {
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  return `${(size / 1024 / 1024).toFixed(1)}MB`
}
const formatPercentUtil = (value: number) => `${(value * 100).toFixed(2)}%`

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

const fetchDetail = async () => {
  if (!id.value) return
  loading.value = true
  try {
    const res = await api.statement.get(id.value)
    if (res.code === 200) {
      Object.assign(data, res.data)
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
    await api.statement.submit(id.value)
    ElMessage.success('提交成功')
    fetchDetail()
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  }
}
const handleExport = () => console.log('导出')
const handlePreview = (row: any) => window.open(row.previewUrl)
const handleDownload = (row: any) => console.log('下载', row)

const handleApprove = async () => {
  try {
    await api.statement.approve(id.value, { actionType: 'approve', opinion: approvalForm.opinion })
    ElMessage.success('审批通过')
    fetchDetail()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleReject = async () => {
  try {
    await api.statement.approve(id.value, { actionType: 'reject', opinion: approvalForm.opinion })
    ElMessage.success('已驳回')
    fetchDetail()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(() => { fetchDetail() })
</script>

<style scoped>
.detail-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 500; }
.amount-card .amount-item { text-align: center; padding: 15px; }
.amount-card .amount-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.amount-card .amount-value { font-size: 20px; font-weight: 600; color: #303133; }
.amount-card .amount-value.primary { color: #409eff; }
.amount-card .amount-value.success { color: #67c23a; }
.amount-card .amount-value.warning { color: #e6a23c; }
.diff-card .diff-summary { display: flex; flex-direction: column; gap: 12px; }
.diff-card .diff-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.diff-card .diff-label { color: #909399; }
.diff-card .diff-value { font-weight: 500; }
.diff-card .diff-value.up { color: #f56c6c; }
.diff-card .diff-value.down { color: #67c23a; }
.approval-card .approval-item { padding: 8px 0; }
.approval-card .approval-user { font-weight: 500; margin-bottom: 4px; }
.approval-card .approval-action { font-size: 14px; color: #909399; }
.approval-card .approval-opinion { margin-top: 4px; padding: 8px; background: #f5f7fa; border-radius: 4px; font-size: 14px; }
.action-card .action-buttons { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
</style>