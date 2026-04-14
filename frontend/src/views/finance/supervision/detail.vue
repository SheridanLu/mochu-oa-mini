<template>
  <div class="supervision-detail">
    <div class="page-header">
      <div class="header-left">
        <h2>回款督办详情</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>回款督办</el-breadcrumb-item>
          <el-breadcrumb-item>详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="$router.back()">返回</el-button>
        <el-button v-if="canEdit" type="primary" @click="handleEdit">编辑</el-button>
        <el-button v-if="canSubmit" type="primary" @click="handleSubmit">提交审批</el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="base-info">
          <template #header><span>基本信息</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="计划编号">{{ data.planNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(data.status)">{{ getStatusText(data.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="关联项目">{{ data.projectName }}</el-descriptions-item>
            <el-descriptions-item label="关联合同">{{ data.contractNo }}</el-descriptions-item>
            <el-descriptions-item label="责任人">{{ data.handlerName }}</el-descriptions-item>
            <el-descriptions-item label="创建人">{{ data.creatorName }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="amount-info" style="margin-top: 20px">
          <template #header><span>回款对比</span></template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="amount-box">
                <div class="amount-label">应付金额</div>
                <div class="amount-value">{{ formatAmount(data.payableAmount) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="amount-box">
                <div class="amount-label">已回金额</div>
                <div class="amount-value success">{{ formatAmount(data.receivedAmount) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="amount-box">
                <div class="amount-label">回款缺口</div>
                <div class="amount-value danger">{{ formatAmount(data.gapAmount) }}</div>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="progress-box">
                <div class="progress-label">完成率</div>
                <el-progress :percentage="data.completionRate" :color="getProgressColor(data.completionRate)" />
              </div>
            </el-col>
          </el-row>
        </el-card>

        <el-card class="suggest-info" style="margin-top: 20px">
          <template #header><span>督办建议</span></template>
          <el-form :model="data" label-width="100px">
            <el-form-item label="优先级">
              <el-rate v-model="data.priority" disabled />
            </el-form-item>
            <el-form-item label="建议时限">
              <span>{{ formatDate(data.suggestedDate) }}</span>
            </el-form-item>
            <el-form-item label="行动建议">
              <div>{{ data.actionSuggestion }}</div>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="feedback-info" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>执行反馈</span>
              <el-button type="primary" link @click="showFeedbackDialog = true">添加反馈</el-button>
            </div>
          </template>
          <el-table :data="feedbackList">
            <el-table-column prop="feedbackTime" label="反馈时间" width="160" />
            <el-table-column prop="feedbackUser" label="反馈人" width="100" />
            <el-table-column prop="content" label="反馈内容" />
            <el-table-column prop="estimatedDate" label="预计回款时间" width="120" />
            <el-table-column label="附件" width="80">
              <template #default="{ row }">
                <el-button v-if="row.attachmentCount > 0" type="primary" link>查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="warning-card">
          <template #header><span>预警信息</span></template>
          <div class="warning-item" v-if="data.overdueDays > 0">
            <el-icon color="#f56c6c"><WarningFilled /></el-icon>
            <span>已逾期 {{ data.overdueDays }} 天</span>
          </div>
          <div class="warning-item" v-if="data.warningDays > 0 && data.overdueDays <= 0">
            <el-icon color="#e6a23c"><WarningFilled /></el-icon>
            <span>即将逾期（剩余{{ data.warningDays }}天）</span>
          </div>
          <div class="warning-item" v-if="data.overdueDays <= 0 && data.warningDays > 0">
            <el-icon color="#67c23a"><CircleCheck /></el-icon>
            <span>正常</span>
          </div>
        </el-card>

        <el-card class="approval-card" style="margin-top: 20px">
          <template #header><span>审批记录</span></template>
          <el-timeline>
            <el-timeline-item v-for="item in approvalLog" :key="item.id" :timestamp="item.operateTime" :type="item.actionResult === 'approved' ? 'success' : 'danger'">
              <div class="approval-user">{{ item.operatorName }}</div>
              <div class="approval-action">{{ item.actionType === 'approve' ? '通过' : '驳回' }}</div>
              <div v-if="item.opinion" class="approval-opinion">{{ item.opinion }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showFeedbackDialog" title="添加反馈" width="500px">
      <el-form :model="feedbackForm" label-width="100px">
        <el-form-item label="反馈内容">
          <el-input v-model="feedbackForm.content" type="textarea" :rows="3" placeholder="请输入反馈内容" />
        </el-form-item>
        <el-form-item label="预计回款">
          <el-date-picker v-model="feedbackForm.estimatedDate" type="date" placeholder="选择预计回款时间" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="上传附件">
          <el-upload action="/api/v1/attachment/upload" multiple>
            <el-button>点击上传</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFeedbackDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddFeedback">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatAmount } from '../../../utils/format'
import api from '../../../api'

const route = useRoute()
const router = useRouter()
const id = computed(() => Number(route.query.id) || 0)
const loading = ref(false)
const showFeedbackDialog = ref(false)

const data = reactive({
  planNo: '', status: 1, projectName: '', contractNo: '', handlerName: '', creatorName: '',
  payableAmount: 0, receivedAmount: 0, gapAmount: 0, completionRate: 0,
  priority: 2, suggestedDate: '', actionSuggestion: '', overdueDays: 0, warningDays: 0
})

const feedbackList = ref<any[]>([])
const approvalLog = ref<any[]>([])
const feedbackForm = reactive({ content: '', estimatedDate: '', attachments: [] as any[] })

const canEdit = computed(() => data.status === 1)
const canSubmit = computed(() => data.status === 1)

const getStatusType = (status: number) => ['', 'info', 'warning', 'success'][status] || 'info'
const getStatusText = (status: number) => ['', '待审批', '审批中', '进行中', '已完成'][status] || ''
const formatDate = (date: string) => date ? date.slice(0, 10) : '-'
const getProgressColor = (rate: number) => {
  if (rate >= 100) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

const fetchDetail = async () => {
  if (!id.value) return
  loading.value = true
  try {
    const res = await api.paymentSupervision.get(id.value)
    if (res.code === 200) Object.assign(data, res.data)
  } catch (e: any) { ElMessage.error(e.message) }
  finally { loading.value = false }
}

const handleEdit = () => router.push(`/finance/supervision/edit?id=${id.value}`)
const handleSubmit = async () => {
  try {
    await api.paymentSupervision.submit(id.value)
    ElMessage.success('提交成功')
    fetchDetail()
  } catch (e: any) { ElMessage.error(e.message) }
}
const handleAddFeedback = () => ElMessage.info('添加反馈功能开发中')

onMounted(() => { fetchDetail() })
</script>

<style scoped>
.supervision-detail { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.amount-box { text-align: center; padding: 15px; }
.amount-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.amount-value { font-size: 20px; font-weight: 600; color: #303133; }
.amount-value.success { color: #67c23a; }
.amount-value.danger { color: #f56c6c; }
.progress-box { padding: 15px; }
.progress-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.warning-item { display: flex; align-items: center; gap: 8px; padding: 10px; background: #f5f7fa; border-radius: 4px; margin-bottom: 10px; }
.approval-user { font-weight: 500; }
.approval-action { font-size: 14px; color: #909399; }
.approval-opinion { margin-top: 4px; padding: 8px; background: #f5f7fa; border-radius: 4px; }
</style>