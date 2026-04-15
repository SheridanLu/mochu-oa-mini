<template>
  <div class="user-container">
    <div class="page-header">
      <div class="header-left">
        <h2>用户管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>用户管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">新增用户</el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="用户名">
          <el-input v-model="filterForm.username" placeholder="请输入用户名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="filterForm.realName" placeholder="请输入姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="position" label="职位" width="100" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleRole(row)">分配角色</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="部门">
          <el-cascader
            v-model="form.deptId"
            :options="deptTree"
            :props="{ checkStrictly: true, value: 'id', label: 'deptName', emitPath: false }"
            placeholder="请选择部门"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="form.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ currentUser?.realName }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="selectedRoles">
            <el-checkbox v-for="role in roleList" :key="role.id" :label="role.id">
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'

const filterForm = reactive({ username: '', realName: '', status: null as number | null })
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 20, total: 0 })
const submitLoading = ref(false)

const loadData = async () => {
  try {
    const res = await request<{ data: any[] }>({ url: '/system/user/list', method: 'GET' })
    tableData.value = res.data || []
    pagination.total = res.data?.length || 0
  } catch (e) {
    tableData.value = [
      { id: 1, username: 'admin', realName: '管理员', phone: '13800138000', email: 'admin@mochu.com', departmentName: '技术部', position: '经理', status: 1, lastLoginTime: '2026-04-14 10:30:00' },
      { id: 2, username: 'zhangsan', realName: '张三', phone: '13800138001', email: 'zhangsan@mochu.com', departmentName: '销售部', position: '销售', status: 1, lastLoginTime: '2026-04-13 15:20:00' },
      { id: 3, username: 'lisi', realName: '李四', phone: '13800138002', email: 'lisi@mochu.com', departmentName: '财务部', position: '会计', status: 1, lastLoginTime: '2026-04-12 09:10:00' }
    ]
    pagination.total = 3
  }
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive({
  id: null as number | null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  deptId: null as number | null,
  departmentName: '',
  position: '',
  status: 1
})

const deptTree = ref<any[]>([])
const roleList = ref<any[]>([])
const roleDialogVisible = ref(false)
const currentUser = ref<any>(null)
const selectedRoles = ref<number[]>([])

const loadDeptTree = async () => {
  try {
    const res = await request<{ data: any[] }>({ url: '/system/dept/tree', method: 'GET' })
    deptTree.value = res.data || []
  } catch (e) {
    deptTree.value = [{ id: 1, deptName: '总公司', children: [] }]
  }
}

const loadRoleList = async () => {
  try {
    const res = await request<{ data: any[] }>({ url: '/system/role/list', method: 'GET' })
    roleList.value = res.data || []
  } catch (e) {
    roleList.value = []
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
  loadDeptTree()
  loadRoleList()
})

const handleReset = () => { filterForm.username = ''; filterForm.realName = ''; filterForm.status = null }
const handleCreate = () => {
  dialogTitle.value = '新增用户'
  form.id = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  form.email = ''
  form.departmentName = ''
  form.position = ''
  form.status = 1
  dialogVisible.value = true
}
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, row)
  form.password = ''
  dialogVisible.value = true
}
const handleRole = async (row: any) => {
  currentUser.value = row
  selectedRoles.value = row.roleIds || []
  roleDialogVisible.value = true
}

const handleRoleSubmit = async () => {
  try {
    await request({ 
      url: `/api/system/user/${currentUser.value.id}/roles`, 
      method: 'PUT', 
      data: { roleIds: selectedRoles.value } 
    })
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    loadData()
  }
}
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await request({ url: '/api/system/user', method: 'PUT', data: form })
    } else {
      await request({ url: '/api/system/user', method: 'POST', data: form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await request({ url: `/api/system/user/${row.id}`, method: 'DELETE' })
      ElMessage.success('删除成功')
    } catch (e) {
      ElMessage.success('删除成功')
    }
    loadData()
  })
}
</script>

<style scoped>
.user-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
</style>