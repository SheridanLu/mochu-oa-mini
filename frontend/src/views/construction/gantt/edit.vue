<template>
  <div class="gantt-edit">
    <div class="page-header">
      <div class="header-left">
        <h2>{{ isEdit ? '编辑甘特图' : '新建甘特图' }}</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>施工管理</el-breadcrumb-item>
          <el-breadcrumb-item>甘特图</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEdit ? '编辑' : '新建' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleBack">取消</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" :disabled="!canSubmit" @click="handleSubmit">提交审批</el-button>
      </div>
    </div>

    <el-alert v-if="!canEdit" type="warning" title="已审批通过，结构已锁定，仅可更新进度" show-icon :closable="false" />

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card class="project-card">
          <template #header><span>基础信息</span></template>
          <el-form :model="form" label-width="100px">
            <el-form-item label="关联项目" required>
              <el-select v-model="form.projectId" placeholder="请选择项目" :disabled="isEdit" style="width: 100%">
                <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="甘特图名称" required>
              <el-input v-model="form.name" placeholder="请输入甘特图名称" :disabled="isEdit && form.status === 3" />
            </el-form-item>
            <el-form-item v-if="isEdit" label="状态">
              <el-tag :type="getStatusType(form.status)">{{ getStatusText(form.status) }}</el-tag>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="milestone-card" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>里程碑</span>
              <el-button type="primary" link @click="addMilestone" :disabled="!canEdit">+添加里程碑</el-button>
            </div>
          </template>
          <el-table :data="milestoneList" stripe>
            <el-table-column prop="name" label="里程碑名称" min-width="150">
              <template #default="{ row, $index }">
                <el-input v-model="row.name" placeholder="请输入名称" :disabled="!canEdit" />
              </template>
            </el-table-column>
            <el-table-column prop="planEndDate" label="计划完成时间" width="160">
              <template #default="{ row }">
                <el-date-picker v-model="row.planEndDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" :disabled="!canEdit" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述">
              <template #default="{ row }">
                <el-input v-model="row.description" placeholder="补充说明" :disabled="!canEdit" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" v-if="canEdit">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeMilestone($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="task-card" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>任务节点</span>
              <el-button type="primary" link @click="addTask" :disabled="!canEdit">+添加任务</el-button>
            </div>
          </template>
          <el-table :data="taskList" stripe>
            <el-table-column prop="milestoneId" label="关联里程碑" width="140">
              <template #default="{ row }">
                <el-select v-model="row.milestoneId" placeholder="选择里程碑" :disabled="!canEdit" style="width: 100%">
                  <el-option v-for="m in milestoneList" :key="m.id" :label="m.name" :value="m.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="任务名称" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.name" placeholder="任务名称" :disabled="!canEdit" />
              </template>
            </el-table-column>
            <el-table-column prop="planStartDate" label="计划开始" width="140">
              <template #default="{ row }">
                <el-date-picker v-model="row.planStartDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" :disabled="!canEdit" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column prop="planEndDate" label="计划完成" width="140">
              <template #default="{ row }">
                <el-date-picker v-model="row.planEndDate" type="date" placeholder="完成日期" value-format="YYYY-MM-DD" :disabled="!canEdit" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column prop="handlerName" label="负责人" width="120">
              <template #default="{ row }">
                <el-select v-model="row.handlerId" placeholder="选择负责人" :disabled="!canEdit" style="width: 100%" @visible-change="(v: boolean) => v && fetchUserList()">
                  <el-option v-for="u in userList" :key="u.id" :label="u.name" :value="u.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="hiddenWork" label="隐蔽工程" width="90" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.hiddenWork" :disabled="!canEdit" />
              </template>
            </el-table-column>
            <el-table-column prop="preTaskId" label="紧前关系" width="100">
              <template #default="{ row, $index }">
                <el-popover placement="top" trigger="click" :width="300">
                  <template #reference>
                    <el-button type="primary" link size="small">{{ row.preTaskIds?.length || 0 }}个</el-button>
                  </template>
                  <div class="pre-task-config">
                    <div v-for="(preId, idx) in row.preTaskIds" :key="idx" class="pre-task-item">
                      <el-select v-model="row.preTasks[idx].taskId" placeholder="选择任务" style="width: 80px">
                        <el-option v-for="t in taskList" :key="t.id" :label="t.name" :value="t.id" />
                      </el-select>
                      <el-select v-model="row.preTasks[idx].type" placeholder="类型" style="width: 60px">
                        <el-option label="FS" value="FS" />
                        <el-option label="SS" value="SS" />
                        <el-option label="FF" value="FF" />
                        <el-option label="SF" value="SF" />
                      </el-select>
                      <el-input-number v-model="row.preTasks[idx].lag" :min="-30" :max="30" style="width: 60px" />
                      <el-button type="danger" link @click="removePreTask(row, idx)">删除</el-button>
                    </div>
                    <el-button type="primary" link @click="addPreTask(row)">+添加</el-button>
                  </div>
                </el-popover>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" v-if="canEdit">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeTask($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="preview-card" style="margin-top: 20px" v-if="taskList.length > 0">
          <template #header>
            <div class="card-header">
              <span>甘特图预览</span>
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button label="day">日</el-radio-button>
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="gantt-preview">
            <div class="timeline-header">
              <div class="task-name-col">任务</div>
              <div class="timeline-col">
                <div v-for="d in timelineDates" :key="d" class="timeline-date">{{ d }}</div>
              </div>
            </div>
            <div class="timeline-body">
              <div v-for="task in taskList" :key="task.id" class="timeline-row">
                <div class="task-name">{{ task.name }}</div>
                <div class="timeline-bar" :style="getTaskBarStyle(task)" @click="canEdit && form.status === 3 ? openProgressDialog(task) : null">
                  <span class="task-bar-label">{{ task.name }}</span>
                  <div class="task-bar-right">
                    <el-tag v-if="task.hiddenWork" size="small" type="warning">隐蔽</el-tag>
                    <span v-if="task.progress !== undefined" class="progress-text">{{ task.progress }}%</span>
                  </div>
                  <div v-if="task.progress !== undefined && task.progress > 0" class="progress-fill" :style="{ width: task.progress + '%' }"></div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="validation-card">
          <template #header><span>校验信息</span></template>
          <div class="validation-list">
            <div v-for="(v, i) in validationErrors" :key="i" class="validation-item error">
              <el-icon><Close /></el-icon>
              <span>{{ v }}</span>
            </div>
            <el-empty v-if="validationErrors.length === 0" description="校验通过" :image-size="60" />
          </div>
        </el-card>

        <el-card class="split-card" style="margin-top: 20px" v-if="isEdit">
          <template #header>
            <div class="card-header">
              <span>拆分映射状态</span>
              <el-tag v-if="form.status === 3" type="success" size="small">已锁定</el-tag>
              <el-tag v-else-if="form.status === 4" type="warning" size="small">待重提</el-tag>
            </div>
          </template>
          <div class="split-status">
            <div class="split-item">
              <span class="label">已映射里程碑</span>
              <span class="value success">{{ mappedMilestones }}/{{ milestoneList.length }}</span>
            </div>
            <div class="split-item">
              <span class="label">已映射任务</span>
              <span class="value success">{{ mappedTasks }}/{{ taskList.length }}</span>
            </div>
            <div class="split-item">
              <span class="label">金额是否闭环</span>
              <span :class="amountMatched ? 'value success' : 'value danger'">
                {{ amountMatched ? '是' : '否' }}
                <el-tooltip v-if="!amountMatched" content="收入合同含税金额与拆分金额不一致，容差0.01">
                  <el-icon><WarningFilled /></el-icon>
                </el-tooltip>
              </span>
            </div>
            <el-divider />
            <div class="split-tip">
              <el-icon color="#e6a23c"><InfoFilled /></el-icon>
              <span>审批锁定状态下需撤回审批后编辑结构</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="progressDialogVisible" title="填报进度" width="500px">
      <el-form :model="progressForm" label-width="120px">
        <el-form-item label="任务名称">
          <span>{{ progressForm.taskName }}</span>
        </el-form-item>
        <el-form-item label="当前进度">
          <el-slider v-model="progressForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="进度描述" required>
          <el-input v-model="progressForm.description" type="textarea" :rows="3" placeholder="记录该阶段工作内容" />
        </el-form-item>
        <el-form-item label="实际开始日期">
          <el-date-picker v-model="progressForm.actualStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实际完成日期">
          <el-date-picker v-model="progressForm.actualEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <template v-if="progressForm.hiddenWork">
          <el-divider>隐蔽工程验收</el-divider>
          <el-form-item label="验收标记" required>
            <el-radio-group v-model="progressForm.hiddenAcceptStatus">
              <el-radio value="unaccepted">未验收</el-radio>
              <el-radio value="accepted">验收合格</el-radio>
              <el-radio value="rejected">验收不合格</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="progressForm.hiddenAcceptStatus === 'rejected'" label="整改说明" required>
            <el-input v-model="progressForm.hiddenRectifyNote" type="textarea" :rows="2" placeholder="请说明整改内容" />
          </el-form-item>
        </template>
        <el-form-item label="上传附件">
          <el-upload action="/api/common/upload" :auto-upload="false" :file-list="progressForm.attachments" list-type="text">
            <el-button type="primary">上传文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProgress">保存进度</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { InfoFilled, WarningFilled } from '@element-plus/icons-vue'
import api from '../../../api'

const router = useRouter()
const route = useRoute()
const id = computed(() => Number(route.query.id) || 0)
const isEdit = computed(() => id.value > 0)

const canEdit = computed(() => {
  if (!isEdit.value) return true
  return form.status === 1 || form.status === 4
})

const canSubmit = computed(() => {
  return canEdit.value && form.projectId > 0 && form.name && milestoneList.value.length > 0 && taskList.value.length > 0
})

const mappedMilestones = computed(() => milestoneList.value.filter(m => m.splitAmount > 0).length)
const mappedTasks = computed(() => taskList.value.filter(t => t.splitAmount > 0).length)
const amountMatched = computed(() => {
  const total = taskList.value.reduce((sum, t) => sum + (t.splitAmount || 0), 0)
  return Math.abs(total - contractAmount) <= 0.01
})
const contractAmount = ref(1000000)

const form = reactive({ id: 0, projectId: 0, name: '', status: 1 })
const projectList = ref<any[]>([])
const userList = ref<any[]>([])
const milestoneList = ref<any[]>([])
const taskList = ref<any[]>([])
const viewMode = ref('week')
const timelineDates = ref<string[]>([])
const validationErrors = ref<string[]>([])
const progressDialogVisible = ref(false)
const progressForm = reactive({
  taskId: 0,
  taskName: '',
  progress: 0,
  description: '',
  actualStartDate: '',
  actualEndDate: '',
  hiddenWork: false,
  hiddenAcceptStatus: 'unaccepted',
  hiddenRectifyNote: '',
  attachments: [] as any[]
})

const getStatusType = (status: number) => ['', 'info', 'warning', 'success', 'primary'][status] || 'info'
const getStatusText = (status: number) => ['', '草稿', '待审批', '已审批', '进行中'][status] || ''

const addMilestone = () => milestoneList.value.push({ id: Date.now(), name: '', planEndDate: '', description: '' })
const removeMilestone = (idx: number) => milestoneList.value.splice(idx, 1)
const addTask = () => taskList.value.push({ id: Date.now(), milestoneId: 0, name: '', planStartDate: '', planEndDate: '', handlerId: 0, hiddenWork: false, preTaskIds: [], preTasks: [] })
const removeTask = (idx: number) => taskList.value.splice(idx, 1)
const addPreTask = (task: any) => { task.preTasks = task.preTasks || []; task.preTasks.push({ taskId: 0, type: 'FS', lag: 0 }) }
const removePreTask = (task: any, idx: number) => task.preTasks.splice(idx, 1)

const openProgressDialog = (task: any) => {
  progressForm.taskId = task.id
  progressForm.taskName = task.name
  progressForm.progress = task.progress || 0
  progressForm.description = task.progressDescription || ''
  progressForm.actualStartDate = task.actualStartDate || ''
  progressForm.actualEndDate = task.actualEndDate || ''
  progressForm.hiddenWork = task.hiddenWork || false
  progressForm.hiddenAcceptStatus = task.hiddenAcceptStatus || 'unaccepted'
  progressForm.hiddenRectifyNote = ''
  progressForm.attachments = []
  progressDialogVisible.value = true
}

const saveProgress = () => {
  if (!progressForm.description) { ElMessage.warning('请填写进度描述'); return }
  if (progressForm.hiddenWork && progressForm.hiddenAcceptStatus === 'rejected' && !progressForm.hiddenRectifyNote) {
    ElMessage.warning('验收不合格时必填整改说明')
    return
  }
  const task = taskList.value.find(t => t.id === progressForm.taskId)
  if (task) {
    task.progress = progressForm.progress
    task.progressDescription = progressForm.description
    task.actualStartDate = progressForm.actualStartDate
    task.actualEndDate = progressForm.actualEndDate
    task.hiddenAcceptStatus = progressForm.hiddenAcceptStatus
  }
  progressDialogVisible.value = false
  ElMessage.success('进度保存成功')
}

const fetchUserList = async () => { userList.value = [] }

const getTaskBarStyle = (task: any) => {
  if (!task.planStartDate || !task.planEndDate) return {}
  return { left: '100px', width: '200px', background: task.hiddenWork ? '#e6a23c' : '#409eff' }
}

const validate = () => {
  validationErrors.value = []
  milestoneList.value.forEach((m, i) => {
    if (!m.name) validationErrors.value.push(`第${i + 1}行里程碑名称不能为空`)
    if (!m.planEndDate) validationErrors.value.push(`第${i + 1}行里程碑计划完成时间不能为空`)
  })
  taskList.value.forEach((t, i) => {
    if (!t.milestoneId) validationErrors.value.push(`第${i + 1}行任务未选择关联里程碑`)
    if (!t.name) validationErrors.value.push(`第${i + 1}行任务名称不能为空`)
    if (!t.planStartDate) validationErrors.value.push(`第${i + 1}行任务计划开始时间不能为空`)
    if (!t.planEndDate) validationErrors.value.push(`第${i + 1}行任务计划完成时间不能为空`)
    if (t.planStartDate && t.planEndDate && t.planStartDate > t.planEndDate) validationErrors.value.push(`第${i + 1}行任务计划开始时间不能晚于完成时间`)
  })
  return validationErrors.value.length === 0
}

const handleBack = () => router.back()
const handleSaveDraft = async () => {
  if (!form.projectId || !form.name) { ElMessage.warning('请填写基础信息'); return }
  try {
    const data = { ...form, status: 1, milestones: milestoneList.value, tasks: taskList.value }
    if (isEdit.value) { await api.gantt.update(data) } else { await api.gantt.create(data) }
    ElMessage.success('保存草稿成功')
    router.back()
  } catch (e: any) { ElMessage.error(e.message) }
}

const handleSubmit = async () => {
  if (!validate()) { ElMessage.warning('请修正校验错误'); return }
  try {
    const data = { ...form, milestones: milestoneList.value, tasks: taskList.value }
    if (!isEdit.value) {
      await api.gantt.create(data)
      ElMessage.success('创建成功')
    }
    router.back()
  } catch (e: any) { ElMessage.error(e.message) }
}

const fetchDetail = async () => {
  if (!id.value) return
  try {
    const res = await api.gantt.get(id.value)
    if (res.code === 200) {
      Object.assign(form, res.data)
      milestoneList.value = res.data.milestones || []
      taskList.value = res.data.tasks || []
    }
  } catch (e: any) { ElMessage.error(e.message) }
}

onMounted(() => { if (id.value) fetchDetail() })
</script>

<style scoped>
.gantt-edit { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pre-task-config { display: flex; flex-direction: column; gap: 8px; }
.pre-task-item { display: flex; gap: 4px; align-items: center; }
.gantt-preview { overflow-x: auto; }
.timeline-header, .timeline-row { display: flex; align-items: center; border-bottom: 1px solid #f0f0f0; }
.task-name-col { width: 100px; flex-shrink: 0; padding: 8px; font-weight: 500; }
.timeline-col { display: flex; flex: 1; }
.timeline-date { width: 50px; flex-shrink: 0; padding: 8px; text-align: center; font-size: 12px; border-left: 1px solid #f0f0f0; }
.task-name { width: 100px; flex-shrink: 0; padding: 8px; font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.timeline-bar { position: relative; height: 24px; border-radius: 4px; margin: 4px; display: flex; align-items: center; justify-content: space-between; padding: 0 8px; }
.task-bar-label { font-size: 12px; color: #fff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.validation-card .validation-item { display: flex; align-items: center; gap: 8px; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.validation-card .validation-item.error { color: #f56c6c; }
.split-card .split-status { display: flex; flex-direction: column; gap: 12px; }
.split-card .split-item { display: flex; justify-content: space-between; align-items: center; }
.split-card .split-item .label { color: #909399; font-size: 14px; }
.split-card .split-item .value { font-weight: 600; }
.split-card .split-item .value.success { color: #67c23a; }
.split-card .split-item .value.danger { color: #f56c6c; }
.split-card .split-tip { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #909399; }
.task-bar-right { display: flex; align-items: center; gap: 4px; }
.progress-text { font-size: 11px; color: #fff; }
.progress-fill { position: absolute; left: 0; top: 0; height: 100%; background: rgba(46, 139, 87, 0.5); border-radius: 4px; }
</style>