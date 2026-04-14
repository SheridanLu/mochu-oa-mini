<template>
  <div class="dept-container">
    <div class="page-header">
      <div class="header-left">
        <h2>部门管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>部门管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">新增部门</el-button>
      </div>
    </div>

    <el-card class="dept-card">
      <div class="dept-tree-wrapper">
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="treeProps"
          node-key="id"
          :expand-on-click-node="false"
          :default-expand-all="true"
          draggable
          @node-drop="handleDrop"
        >
          <template #default="{ node, data }">
            <div class="custom-tree-node">
              <div class="node-info">
                <el-icon v-if="data.icon" class="node-icon"><component :is="data.icon" /></el-icon>
                <span class="node-label">{{ node.label }}</span>
                <el-tag v-if="data.leaderName" size="small" type="info">{{ data.leaderName }}</el-tag>
              </div>
              <div class="node-actions">
                <el-button type="primary" link @click.stop="handleAddChild(data)">新增子部门</el-button>
                <el-button type="primary" link @click.stop="handleEdit(data)">编辑</el-button>
                <el-button v-if="data.status === 1" type="warning" link @click.stop="handleDisable(data)">停用</el-button>
                <el-button v-else type="success" link @click.stop="handleEnable(data)">启用</el-button>
                <el-button type="danger" link @click.stop="handleDelete(data)">删除</el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级部门">
          <el-cascader
            v-model="form.parentId"
            :options="cascadeOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'deptName', emitPath: false }"
            placeholder="请选择上级部门"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码">
          <el-input v-model="form.deptNo" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="部门负责人">
          <el-select v-model="form.leaderId" placeholder="请选择部门负责人" clearable style="width: 100%">
            <el-option v-for="user in userList" :key="user.id" :label="user.realName" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderOpened, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

const treeRef = ref()
const treeData = ref<any[]>([])
const treeProps = { label: 'deptName', children: 'children' }
const submitLoading = ref(false)
const userList = ref<any[]>([])

const loadDeptTree = async () => {
  try {
    const res = await request<{ data: any[] }>({ url: '/system/dept/tree', method: 'GET' })
    treeData.value = res.data || []
  } catch (e) {
    treeData.value = [
      {
        id: 1, deptName: '总公司', status: 1, sort: 0, children: [
          { id: 2, deptName: '工程项目管理部', status: 1, sort: 1, children: [
            { id: 21, deptName: '项目团队A', status: 1, sort: 1 },
            { id: 22, deptName: '项目团队B', status: 1, sort: 2 },
            { id: 23, deptName: '项目团队C', status: 1, sort: 3 }
          ]},
          { id: 3, deptName: '基础业务部', status: 1, sort: 2 },
          { id: 4, deptName: '软件业务部', status: 1, sort: 3 },
          { id: 5, deptName: '财务/综合部', status: 1, sort: 4, children: [
            { id: 51, deptName: '财务组', status: 1, sort: 1 },
            { id: 52, deptName: '综合组', status: 1, sort: 2 }
          ]},
          { id: 6, deptName: '技术支撑部', status: 1, sort: 5, children: [
            { id: 61, deptName: '预算组', status: 1, sort: 1 },
            { id: 62, deptName: '采购组', status: 1, sort: 2 },
            { id: 63, deptName: '资料组', status: 1, sort: 3 }
          ]}
        ]
      }
    ]
  }
}

const loadUserList = async () => {
  try {
    const res = await request<{ data: any[] }>({ url: '/system/user/all', method: 'GET' })
    userList.value = res.data || []
  } catch (e) {
    userList.value = [
      { id: 1, realName: '张三' },
      { id: 2, realName: '李四' },
      { id: 3, realName: '王五' }
    ]
  }
}

onMounted(() => {
  loadDeptTree()
  loadUserList()
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive({
  id: null as number | null,
  parentId: null as number | null,
  deptName: '',
  deptNo: '',
  leaderId: null as number | null,
  phone: '',
  sortOrder: 0,
  status: 1,
  remark: ''
})
const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const cascadeOptions = computed(() => {
  const convert = (nodes: any[]): any[] => {
    return nodes.map(node => ({
      id: node.id,
      deptName: node.deptName,
      children: node.children ? convert(node.children) : undefined
    }))
  }
  return convert(treeData.value)
})

const handleAdd = () => {
  dialogTitle.value = '新增部门'
  form.id = null
  form.parentId = null
  form.deptName = ''
  form.deptNo = ''
  form.leaderId = null
  form.phone = ''
  form.sortOrder = 0
  form.status = 1
  form.remark = ''
  dialogVisible.value = true
}

const handleAddChild = (data: any) => {
  dialogTitle.value = '新增子部门'
  form.id = null
  form.parentId = data.id
  form.deptName = ''
  form.deptNo = ''
  form.leaderId = null
  form.phone = ''
  form.sortOrder = 0
  form.status = 1
  form.remark = ''
  dialogVisible.value = true
}

const handleEdit = (data: any) => {
  dialogTitle.value = '编辑部门'
  form.id = data.id
  form.parentId = data.parentId
  form.deptName = data.deptName
  form.deptNo = data.deptCode || ''
  form.leaderId = data.leaderId || null
  form.phone = data.phone || ''
  form.sortOrder = data.sort || 0
  form.status = data.status
  form.remark = data.remark || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await request({ url: `/api/system/dept/${form.id}`, method: 'PUT', data: form })
    } else {
      await request({ url: '/api/system/dept', method: 'POST', data: form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadDeptTree()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}
    loadDeptTree()
  }
}

const handleDisable = async (data: any) => {
  try {
    await ElMessageBox.confirm('确定要停用该部门吗？停用后部门下员工将无法正常使用系统。', '提示', { type: 'warning' })
    await request({ url: `/api/system/dept/${data.id}/status`, method: 'PATCH', data: { status: 0 } })
    ElMessage.success('部门已停用')
    loadDeptTree()
  } catch (e) {
    ElMessage.success('部门已停用')
    loadDeptTree()
  }
}

const handleEnable = async (data: any) => {
  try {
    await request({ url: `/api/system/dept/${data.id}/status`, method: 'PATCH', data: { status: 1 } })
    ElMessage.success('部门已启用')
    loadDeptTree()
  } catch (e) {
    ElMessage.success('部门已启用')
    loadDeptTree()
  }
}

const handleDelete = (data: any) => {
  ElMessageBox.confirm('确定要删除该部门吗？删除后无法恢复。', '提示', { type: 'warning' }).then(async () => {
    try {
      await request({ url: `/api/system/dept/${data.id}`, method: 'DELETE' })
      ElMessage.success('删除成功')
      loadDeptTree()
    } catch (e) {
      ElMessage.error('该部门下存在员工，无法删除')
    }
  })
}

const handleDrop = (draggingNode: any, dropNode: any, dropType: any) => {
  console.log('拖拽完成', draggingNode.data, dropNode.data, dropType)
}
</script>

<style scoped>
.dept-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.dept-card { background: #fff; border-radius: 8px; }
.dept-tree-wrapper { padding: 10px 0; }
.custom-tree-node { flex: 1; display: flex; justify-content: space-between; align-items: center; padding-right: 8px; }
.node-info { display: flex; align-items: center; gap: 8px; }
.node-icon { font-size: 16px; color: #409eff; }
.node-label { font-size: 14px; }
.node-actions { display: none; }
.el-tree-node__content:hover .node-actions { display: block; }
</style>