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
      <el-table v-loading="tableLoading" :data="tableData" stripe>
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="120" />
        <el-table-column label="角色描述" min-width="180">
          <template #default="{ row }">{{ row.remark || row.description || '—' }}</template>
        </el-table-column>
        <el-table-column prop="userCount" label="用户数量" width="100" align="center" />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ row.createdAt || row.createTime || '—' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handlePermission(row)">权限配置</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" :validate-on-rule-change="false" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" :validate-event="false" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="form.roleCode"
            :validate-event="false"
            maxlength="32"
            show-word-limit
            placeholder="请输入角色编码，如：admin"
          />
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
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { request } from '@/utils/request'

const route = useRoute()
const router = useRouter()

const filterForm = reactive({ roleName: '', status: null as number | null })
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, size: 20, total: 0 })
const tableLoading = ref(false)

const loadData = async () => {
  tableLoading.value = true
  try {
    const res: any = await request({
      url: '/system/role/list',
      method: 'GET',
      params: {
        roleName: filterForm.roleName || undefined,
        status: filterForm.status ?? undefined,
        page: pagination.page,
        size: pagination.size
      }
    })
    const pageData = res.data
    tableData.value = pageData?.list || []
    pagination.total = Number(pageData?.total) || 0
  } catch {
    tableData.value = []
    pagination.total = 0
  } finally {
    tableLoading.value = false
  }
}

const handleSizeChange = () => {
  pagination.page = 1
  loadData()
}

const handlePageChange = () => {
  loadData()
}

onMounted(() => {
  const roleName = typeof route.query.roleName === 'string' ? route.query.roleName : ''
  if (roleName) {
    filterForm.roleName = roleName
    pagination.page = 1
  }
  loadData()
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const submitLoading = ref(false)
interface RoleForm {
  id: number | null
  roleName: string
  roleCode: string
  description: string
  dataScope: number
  status: number
}

const createEmptyRoleForm = (): RoleForm => ({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  dataScope: 3,
  status: 1
})

const form = reactive<RoleForm>(createEmptyRoleForm())
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'change' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'change' },
    {
      pattern: /^[a-z][a-z0-9:_-]{1,31}$/,
      message: '角色编码需以小写字母开头，仅支持小写字母/数字/:/_/-，长度2-32位',
      trigger: 'change'
    }
  ]
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  filterForm.roleName = ''
  filterForm.status = null
  pagination.page = 1
  loadData()
}
const handleCreate = () => {
  dialogTitle.value = '新增角色'
  Object.assign(form, createEmptyRoleForm())
  dialogVisible.value = true
}
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑角色'
  Object.assign(form, {
    id: row.id,
    roleName: row.roleName || '',
    roleCode: row.roleCode || '',
    description: row.remark || row.description || '',
    dataScope: Number(row.dataScope ?? 3),
    status: Number(row.status ?? 1)
  } satisfies RoleForm)
  dialogVisible.value = true
}
const buildRolePayload = () => ({
  id: form.id ?? undefined,
  roleName: form.roleName,
  roleCode: form.roleCode,
  remark: form.description || null,
  dataScope: form.dataScope,
  status: form.status,
  roleType: 1
})

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const payload = buildRolePayload()
    if (form.id) {
      await request({ url: '/system/role', method: 'PUT', data: payload })
      ElMessage.success('保存成功')
      dialogVisible.value = false
      await loadData()
    } else {
      const res = await request<{ data: number }>({ url: '/system/role', method: 'POST', data: payload })
      ElMessage.success('保存成功')
      dialogVisible.value = false
      await loadData()
      const newId = res?.data
      if (newId != null && Number(newId) > 0) {
        await router.push({
          path: '/system/permission',
          query: { roleId: String(newId), roleName: form.roleName }
        })
      }
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

const rowToRolePayload = (row: any) => ({
  id: row.id,
  roleName: row.roleName,
  roleCode: row.roleCode,
  remark: row.remark ?? row.description ?? null,
  dataScope: row.dataScope ?? 3,
  status: row.status,
  roleType: row.roleType ?? 1
})

const handleStatusChange = async (row: any) => {
  try {
    await request({ url: '/system/role', method: 'PUT', data: rowToRolePayload(row) })
    ElMessage.success(`已${row.status === 1 ? '启用' : '禁用'}角色`)
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
    row.status = row.status === 1 ? 0 : 1
  }
}
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该角色吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await request({ url: `/system/role/${row.id}`, method: 'DELETE' })
      ElMessage.success('删除成功')
      loadData()
    } catch (e: any) {
      ElMessage.error(e?.message || '删除失败')
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