<template>
  <div class="flow-page">
    <div class="page-header">
      <div>
        <h2>流程管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>审批中心</el-breadcrumb-item>
          <el-breadcrumb-item>流程管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="actions">
        <el-button @click="loadList">刷新</el-button>
        <el-button type="warning" @click="handleInit">初始化默认流程</el-button>
      </div>
    </div>

    <el-card>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="bizType" label="业务类型" width="140" />
        <el-table-column prop="bizName" label="流程名称" width="220" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column label="流程JSON">
          <template #default="{ row }">
            <div class="json-preview">{{ row.flowJson }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑流程定义" width="760px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="业务类型">
          <el-input v-model="form.bizType" :disabled="!!form.id" placeholder="如 PROJECT / CONTRACT" />
        </el-form-item>
        <el-form-item label="流程名称">
          <el-input v-model="form.bizName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="流程JSON">
          <el-input v-model="form.flowJson" type="textarea" :rows="12" placeholder='{"nodes":[{"order":1,"type":"APPROVE","name":"节点1","role":"GM","timeout":86400}]}' />
        </el-form-item>
        <el-form-item label="模板生成">
          <div class="template-tools">
            <el-button size="small" @click="applyTemplate('TWO_STEP')">两级审批模板</el-button>
            <el-button size="small" @click="applyTemplate('THREE_STEP')">三级审批模板</el-button>
            <el-button size="small" @click="applyTemplate('FOUR_STEP')">四级审批模板</el-button>
            <el-button size="small" @click="applyTemplate('PROJECT')">项目立项模板</el-button>
            <el-button size="small" @click="applyTemplate('PURCHASE')">采购审批模板</el-button>
          </div>
        </el-form-item>
        <el-form-item label="校验结果">
          <el-alert
            v-if="flowValidation.ok"
            title="流程JSON校验通过"
            type="success"
            :closable="false"
            show-icon
          />
          <el-alert
            v-else
            :title="flowValidation.message"
            type="error"
            :closable="false"
            show-icon
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const list = ref<any[]>([])

const form = reactive({
  id: undefined as number | undefined,
  bizType: '',
  bizName: '',
  flowJson: '',
  status: 1
})

const flowValidation = computed(() => validateFlowJson(form.flowJson))

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await api.approval.flowDefs()
    if (res.code === 200) list.value = res.data || []
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载流程定义失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = (row: any) => {
  form.id = row.id
  form.bizType = row.bizType || ''
  form.bizName = row.bizName || ''
  form.flowJson = row.flowJson || ''
  form.status = Number(row.status) === 1 ? 1 : 0
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.bizType.trim() || !form.bizName.trim() || !form.flowJson.trim()) {
    ElMessage.warning('业务类型、流程名称、流程JSON不能为空')
    return
  }
  const check = validateFlowJson(form.flowJson)
  if (!check.ok) {
    ElMessage.error(check.message)
    return
  }
  try {
    const payload = {
      id: form.id,
      bizType: form.bizType.trim().toUpperCase(),
      bizName: form.bizName.trim(),
      flowJson: form.flowJson.trim(),
      status: form.status
    }
    await api.approval.saveFlowDef(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadList()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '保存失败')
  }
}

const handleInit = async () => {
  try {
    await ElMessageBox.confirm('将补齐系统默认流程定义，是否继续？', '提示', { type: 'warning' })
    await api.approval.initFlowDefs()
    ElMessage.success('初始化成功')
    await loadList()
  } catch {}
}

onMounted(() => {
  void loadList()
})

function applyTemplate(type: 'TWO_STEP' | 'THREE_STEP' | 'FOUR_STEP' | 'PROJECT' | 'PURCHASE') {
  const templates: Record<string, any> = {
    TWO_STEP: {
      nodes: [
        { order: 1, type: 'APPROVE', name: '一级审批', role: 'DEPT_MANAGER', timeout: 86400 },
        { order: 2, type: 'APPROVE', name: '二级审批', role: 'GM', timeout: 86400 }
      ]
    },
    THREE_STEP: {
      nodes: [
        { order: 1, type: 'APPROVE', name: '一级审批', role: 'DEPT_MANAGER', timeout: 86400 },
        { order: 2, type: 'APPROVE', name: '二级审批', role: 'FINANCE', timeout: 86400 },
        { order: 3, type: 'APPROVE', name: '三级审批', role: 'GM', timeout: 86400 }
      ]
    },
    FOUR_STEP: {
      nodes: [
        { order: 1, type: 'APPROVE', name: '一级审批', role: 'APPLICANT_LEADER', timeout: 86400 },
        { order: 2, type: 'APPROVE', name: '二级审批', role: 'DEPT_MANAGER', timeout: 86400 },
        { order: 3, type: 'APPROVE', name: '三级审批', role: 'FINANCE', timeout: 86400 },
        { order: 4, type: 'APPROVE', name: '四级审批', role: 'GM', timeout: 86400 }
      ]
    },
    PROJECT: {
      nodes: [
        { order: 1, type: 'APPROVE', name: '预算员审批', role: 'BUDGET', timeout: 86400 },
        { order: 2, type: 'APPROVE', name: '总经理审批', role: 'GM', timeout: 86400 }
      ]
    },
    PURCHASE: {
      nodes: [
        { order: 1, type: 'APPROVE', name: '预算员审批', role: 'BUDGET', timeout: 86400 },
        { order: 2, type: 'APPROVE', name: '财务审批', role: 'FINANCE', timeout: 86400 },
        { order: 3, type: 'APPROVE', name: '总经理审批', role: 'GM', timeout: 86400 }
      ]
    }
  }
  form.flowJson = JSON.stringify(templates[type], null, 2)
  ElMessage.success('已应用流程模板，可继续编辑后保存')
}

function validateFlowJson(raw: string): { ok: boolean; message: string } {
  if (!raw.trim()) return { ok: false, message: '流程JSON不能为空' }
  let parsed: any
  try {
    parsed = JSON.parse(raw)
  } catch (e: any) {
    return { ok: false, message: `JSON语法错误：${e?.message || '请检查格式'}` }
  }
  if (!parsed || typeof parsed !== 'object') {
    return { ok: false, message: '流程JSON必须是对象结构' }
  }
  if (!Array.isArray(parsed.nodes) || parsed.nodes.length === 0) {
    return { ok: false, message: '必须包含非空 nodes 数组' }
  }
  const orders = new Set<number>()
  for (let i = 0; i < parsed.nodes.length; i++) {
    const node = parsed.nodes[i]
    const idx = i + 1
    if (!node || typeof node !== 'object') {
      return { ok: false, message: `第${idx}个节点不是对象` }
    }
    const order = Number(node.order)
    if (!Number.isInteger(order) || order <= 0) {
      return { ok: false, message: `第${idx}个节点 order 必须是正整数` }
    }
    if (orders.has(order)) {
      return { ok: false, message: `节点顺序 order=${order} 重复` }
    }
    orders.add(order)
    if (!String(node.type || '').trim()) {
      return { ok: false, message: `第${idx}个节点 type 不能为空` }
    }
    if (!String(node.name || '').trim()) {
      return { ok: false, message: `第${idx}个节点 name 不能为空` }
    }
    if (!String(node.role || '').trim()) {
      return { ok: false, message: `第${idx}个节点 role 不能为空` }
    }
    if (node.timeout != null) {
      const timeout = Number(node.timeout)
      if (!Number.isInteger(timeout) || timeout <= 0) {
        return { ok: false, message: `第${idx}个节点 timeout 必须是正整数秒` }
      }
    }
  }
  const sorted = [...orders].sort((a, b) => a - b)
  for (let i = 0; i < sorted.length; i++) {
    if (sorted[i] !== i + 1) {
      return { ok: false, message: `order 需从1开始连续递增，当前缺少 ${i + 1}` }
    }
  }
  return { ok: true, message: 'ok' }
}
</script>

<style scoped>
.flow-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0 0 6px; }
.actions { display: flex; gap: 8px; }
.json-preview { white-space: pre-wrap; word-break: break-all; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 12px; color: #606266; max-height: 120px; overflow: auto; }
.template-tools { display: flex; gap: 8px; flex-wrap: wrap; }
</style>
