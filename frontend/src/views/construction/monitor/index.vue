<template>
  <div class="monitor-container">
    <div class="page-header">
      <div class="header-left">
        <h2>进度监控 - {{ projectName }}</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>施工管理</el-breadcrumb-item>
          <el-breadcrumb-item>进度监控</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleBack">返回</el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">总体完成率</div>
          <div class="stat-value primary">{{ overallProgress }}%</div>
          <el-progress :percentage="overallProgress" :show-text="false" :stroke-width="8" />
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">里程碑达成</div>
          <div class="stat-value">{{ milestoneCompleted }}/{{ milestoneTotal }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">隐蔽工程验收</div>
          <div class="stat-value">
            <span class="success">{{ hiddenAccepted }}</span> /
            <span class="warning">{{ hiddenPending }}</span> /
            <span class="danger">{{ hiddenRejected }}</span>
          </div>
          <div class="stat-tip">合格/未验/不合格</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">逾期任务</div>
          <div class="stat-value danger">{{ overdueCount }}</div>
        </div>
      </el-col>
    </el-row>

    <el-card class="gantt-card">
      <template #header>
        <div class="card-header">
          <span>甘特图进度</span>
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="day">日</el-radio-button>
            <el-radio-button label="week">周</el-radio-button>
            <el-radio-button label="month">月</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div class="gantt-timeline">
        <div class="timeline-header">
          <div class="task-col">任务</div>
          <div class="progress-col">进度</div>
          <div class="dates-col">
            <div v-for="d in dates" :key="d" class="date-cell">{{ d }}</div>
          </div>
        </div>
        <div class="timeline-body">
          <template v-for="m in milestones" :key="m.id">
            <div class="milestone-row">
              <div class="task-col milestone">
                <el-icon><Flag /></el-icon>
                <span>{{ m.name }}</span>
                <el-tag size="small" :type="m.progress >= 100 ? 'success' : 'warning'">{{ m.progress }}%</el-tag>
              </div>
              <div class="progress-col">
                <el-progress :percentage="m.progress" :show-text="false" :stroke-width="10" />
              </div>
            </div>
            <div v-for="t in m.tasks" :key="t.id" class="task-row" :class="{ overdue: t.overdue, nearOverdue: t.nearOverdue }">
              <div class="task-col">
                <span>{{ t.name }}</span>
                <el-tag v-if="t.hiddenWork" size="small" type="warning">隐蔽</el-tag>
                <el-tooltip v-if="t.hiddenAcceptStatus === 'accepted'" content="验收合格" placement="top">
                  <el-icon color="#67c23a"><CircleCheck /></el-icon>
                </el-tooltip>
                <el-tooltip v-if="t.hiddenAcceptStatus === 'rejected'" content="验收不合格" placement="top">
                  <el-icon color="#f56c6c"><CircleClose /></el-icon>
                </el-tooltip>
                <el-tooltip v-if="t.hiddenAcceptStatus === 'unaccepted'" content="待验收" placement="top">
                  <el-icon color="#e6a23c"><Warning /></el-icon>
                </el-tooltip>
              </div>
              <div class="progress-col">
                <el-progress :percentage="t.progress" :color="getProgressColor(t)" :stroke-width="10" />
              </div>
            </div>
          </template>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="alert-card">
          <template #header>
            <span>预警信息</span>
          </template>
          <div class="alert-list">
            <div v-for="(a, i) in alerts" :key="i" class="alert-item" :class="a.type">
              <el-icon><WarningFilled v-if="a.type === 'overdue'" /><Clock v-else /></el-icon>
              <div class="alert-content">
                <div class="alert-title">{{ a.title }}</div>
                <div class="alert-desc">{{ a.desc }}</div>
              </div>
            </div>
            <el-empty v-if="alerts.length === 0" description="暂无预警信息" :image-size="60" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="path-card">
          <template #header>
            <span>关键路径</span>
          </template>
          <div class="path-list">
            <div v-for="(p, i) in criticalPath" :key="i" class="path-item">
              <span class="path-index">{{ i + 1 }}</span>
              <span>{{ p.name }}</span>
              <span class="path-duration">{{ p.duration }}天</span>
            </div>
            <el-empty v-if="criticalPath.length === 0" description="暂无关键路径" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Flag, CircleCheck, CircleClose, Warning, WarningFilled, Clock } from '@element-plus/icons-vue'
import { api } from '@/api'

const router = useRouter()
const route = useRoute()
const ganttId = computed(() => {
  const raw = route.query.id ?? route.query.ganttId
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? n : 0
})

const projectName = ref('XX项目一期')
const viewMode = ref('week')
const dates = ref(['04-07', '04-08', '04-09', '04-10', '04-11', '04-12', '04-13'])

const overallProgress = ref(65)
const milestoneCompleted = ref(2)
const milestoneTotal = ref(4)
const hiddenAccepted = ref(3)
const hiddenPending = ref(2)
const hiddenRejected = ref(1)
const overdueCount = ref(1)

const milestones = ref([
  {
    id: 1, name: '地基验收', progress: 100,
    tasks: [
      { id: 11, name: '地基开挖', progress: 100, overdue: false, nearOverdue: false, hiddenWork: false },
      { id: 12, name: '地基检测', progress: 100, overdue: false, nearOverdue: false, hiddenWork: true, hiddenAcceptStatus: 'accepted' }
    ]
  },
  {
    id: 2, name: '主体结构', progress: 75,
    tasks: [
      { id: 21, name: '钢结构吊装', progress: 80, overdue: false, nearOverdue: false, hiddenWork: true, hiddenAcceptStatus: 'unaccepted' },
      { id: 22, name: '模板搭建', progress: 70, overdue: false, nearOverdue: false, hiddenWork: false },
      { id: 23, name: '混凝土浇筑', progress: 0, overdue: true, nearOverdue: false, hiddenWork: false }
    ]
  },
  {
    id: 3, name: '装修工程', progress: 30,
    tasks: [
      { id: 31, name: '外墙装饰', progress: 30, overdue: false, nearOverdue: true, hiddenWork: false }
    ]
  }
])

const alerts = ref([
  { type: 'overdue', title: '混凝土浇筑已逾期', desc: '逾期3天，负责人：李四' },
  { type: 'warning', title: '外墙装饰即将逾期', desc: '距离计划完成还有2天' },
  { type: 'warning', title: '隐蔽工程待验收', desc: '钢结构吊装已完成，需补充验收资料' }
])

const criticalPath = ref([
  { name: '地基开挖', duration: 15 },
  { name: '地基检测', duration: 10 },
  { name: '钢结构吊装', duration: 30 },
  { name: '模板搭建', duration: 20 },
  { name: '混凝土浇筑', duration: 15 }
])

const getProgressColor = (task: any) => {
  if (task.overdue) return '#f56c6c'
  if (task.nearOverdue) return '#e6a23c'
  return '#409eff'
}

const handleBack = () => router.back()
const applyStats = () => {
  const ms = milestones.value || []
  milestoneTotal.value = ms.length
  milestoneCompleted.value = ms.filter((m: any) => Number(m.progress || 0) >= 100).length
  const tasks = ms.flatMap((m: any) => m.tasks || [])
  const totalTask = tasks.length || 1
  overallProgress.value = Math.round(tasks.reduce((s: number, t: any) => s + Number(t.progress || 0), 0) / totalTask)
  hiddenAccepted.value = tasks.filter((t: any) => t.hiddenAcceptStatus === 'accepted').length
  hiddenPending.value = tasks.filter((t: any) => t.hiddenAcceptStatus === 'unaccepted').length
  hiddenRejected.value = tasks.filter((t: any) => t.hiddenAcceptStatus === 'rejected').length
  overdueCount.value = tasks.filter((t: any) => t.overdue).length
}

const loadMonitor = async () => {
  if (!ganttId.value) {
    applyStats()
    return
  }
  try {
    const res: any = await api.gantt.get(ganttId.value)
    if (res.code === 200 && res.data) {
      projectName.value = res.data.projectName || projectName.value
      milestones.value = (res.data.milestones || []).map((m: any) => ({
        ...m,
        progress: Number(m.progress || 0),
        tasks: (m.tasks || []).map((t: any) => ({
          ...t,
          progress: Number(t.progress || 0),
          overdue: Boolean(t.overdue),
          nearOverdue: Boolean(t.nearOverdue),
        })),
      }))
      applyStats()
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载进度监控失败')
  }
}

const handleRefresh = async () => {
  await loadMonitor()
  ElMessage.success('刷新成功')
}

onMounted(() => {
  loadMonitor()
})
</script>

<style scoped>
.monitor-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.stat-row { margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: 600; color: #303133; }
.stat-value.primary { color: #409eff; }
.stat-value.danger { color: #f56c6c; }
.stat-value .success { color: #67c23a; }
.stat-value .warning { color: #e6a23c; }
.stat-value .danger { color: #f56c6c; }
.stat-tip { font-size: 12px; color: #909399; margin-top: 4px; }
.gantt-card { background: #fff; border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.gantt-timeline { overflow-x: auto; }
.timeline-header, .task-row, .milestone-row { display: flex; align-items: center; border-bottom: 1px solid #f0f0f0; }
.task-col { width: 200px; padding: 12px; display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.task-col.milestone { font-weight: 600; color: #409eff; }
.progress-col { width: 150px; padding: 12px; flex-shrink: 0; }
.dates-col { display: flex; flex: 1; }
.date-cell { flex: 1; padding: 12px; text-align: center; border-left: 1px solid #f0f0f0; font-size: 12px; }
.task-row.overdue { background: #fef0f0; }
.task-row.nearOverdue { background: #fdf6ec; }
.alert-card, .path-card { background: #fff; border-radius: 8px; }
.alert-item { display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.alert-item.overdue { color: #f56c6c; }
.alert-item.warning { color: #e6a23c; }
.alert-title { font-weight: 500; }
.alert-desc { font-size: 12px; color: #909399; margin-top: 4px; }
.path-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.path-index { width: 24px; height: 24px; background: #409eff; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.path-duration { color: #909399; font-size: 12px; margin-left: auto; }
</style>