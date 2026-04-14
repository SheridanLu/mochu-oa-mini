<template>
  <div class="role-container">
    <div class="page-header">
      <div class="header-left">
        <h2>角色管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>角色管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">新增角色</el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="角色名称">
          <el-input v-model="filterForm.roleName" placeholder="请输入角色名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" stripe>
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="120" />
        <el-table-column prop="description" label="角色描述" min-width="180" />
        <el-table-column prop="userCount" label="用户数量" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handlePermission(row)">权限配置</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码，如：admin" />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope" placeholder="请选择数据范围" style="width: 100%">
            <el-option label="全部数据" :value="1" />
            <el-option label="本部门数据" :value="2" />
            <el-option label="本人数据" :value="3" />
            <el-option label="指定范围数据" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { request } from '@/utils/request'

const router = useRouter()

const filterForm = reactive({ roleName: '', status: null as number | null })
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 20, total: 0 })

const loadData = async () => {
  try {
    const res = await request<{ data: any[] }>({ url: '/system/role/list', method: 'GET' })
    tableData.value = res.data || []
    pagination.total = res.data?.length || 0
  } catch (e) {
    tableData.value = [
      { id: 1, roleName: '超级管理员', roleCode: 'admin', description: '拥有系统所有权限', userCount: 2, status: 1, createTime: '2026-01-01 10:00:00' },
      { id: 2, roleName: '项目经理', roleCode: 'project_manager', description: '负责项目管理和进度跟踪', userCount: 5, status: 1, createTime: '2026-01-05 14:30:00' },
      { id: 3, roleName: '财务人员', roleCode: 'finance', description: '负责财务相关操作', userCount: 3, status: 1, createTime: '2026-01-10 09:20:00' },
      { id: 4, roleName: '采购人员', roleCode: 'purchase', description: '负责采购相关操作', userCount: 4, status: 1, createTime: '2026-01-12 11:00:00' },
      { id: 5, roleName: '普通员工', roleCode: 'employee', description: '普通员工权限', userCount: 20, status: 1, createTime: '2026-01-15 16:45:00' },
      { id: 6, roleName: '法务人员', roleCode: 'legal', description: '法务相关操作权限', userCount: 2, status: 1, createTime: '2026-02-01 10:00:00' },
      { id: 7, roleName: '总经理', roleCode: 'gm', description: '公司最高管理权限', userCount: 1, status: 1, createTime: '2026-01-01 08:00:00' }
    ]
    pagination.total = 7
  }
}

onMounted(() => {
  loadData()
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive({ id: null as number | null, roleName: '', roleCode: '', description: '', dataScope: 3, status: 1 })
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const roleCodeMap: Record<string, string> = {}

const handleSearch = () => { loadData() }
const handleReset = () => { filterForm.roleName = ''; filterForm.status = null }
const handleCreate = () => {
  dialogTitle.value = '新增角色'
  form.id = null
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.dataScope = 3
  form.status = 1
  dialogVisible.value = true
}
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑角色'
  form.id = row.id
  form.roleName = row.roleName || ''
  form.roleCode = row.roleCode || ''
  form.description = row.description || ''
  form.dataScope = row.dataScope || 3
  form.status = row.status
  dialogVisible.value = true
}
const handleSubmit = async () => {
  try {
    if (form.id) {
      await request({ url: '/system/role', method: 'PUT', data: form })
    } else {
      await request({ url: '/system/role', method: 'POST', data: form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}
}
const handleStatusChange = async (row: any) => {
  try {
    await request({ url: '/system/role', method: 'PUT', data: row })
    ElMessage.success(`已${row.status === 1 ? '启用' : '禁用'}角色`)
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该角色吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await request({ url: `/system/role/${row.id}`, method: 'DELETE' })
      ElMessage.success('删除成功')
      loadData()
    } catch (e: any) {
      ElMessage.error(e.message || '删除失败')
    }
  })
}
const handlePermission = (row: any) => { router.push({ path: '/system/permission', query: { roleId: row.id, roleName: row.roleName } }) }
</script>

<style scoped>
.role-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
</style>