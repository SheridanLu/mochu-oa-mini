<template>
  <div class="template-page">
    <div class="page-header">
      <div>
        <h2>合同模板管理</h2>
        <el-breadcrumb separator="/"><el-breadcrumb-item>合同管理</el-breadcrumb-item><el-breadcrumb-item>合同模板管理</el-breadcrumb-item></el-breadcrumb>
      </div>
      <div class="header-actions">
        <el-button @click="loadList">刷新</el-button>
        <el-button type="primary" @click="openImportDialog">导入模板</el-button>
      </div>
    </div>

    <el-card>
      <el-form :inline="true">
        <el-form-item label="合同类型">
          <el-select v-model="filter.contractType" clearable placeholder="全部" style="width: 160px" @change="loadList">
            <el-option label="收入合同" :value="1" />
            <el-option label="支出合同" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="templateName" label="模板名称" min-width="220" />
        <el-table-column prop="contractType" label="合同类型" width="120">
          <template #default="{ row }">{{ row.contractType === 2 ? '支出合同' : '收入合同' }}</template>
        </el-table-column>
        <el-table-column prop="sourceFileName" label="来源文件" min-width="180" />
        <el-table-column prop="templateVersion" label="版本" width="80" align="center" />
        <el-table-column prop="fieldKeys" label="可填字段" min-width="260" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
            <el-button link type="danger" @click="removeTemplate(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="importDialogVisible" title="导入合同模板" width="520px" destroy-on-close>
      <el-form :model="importForm" label-width="110px">
        <el-form-item label="模板名称" required>
          <el-input v-model="importForm.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="合同类型" required>
          <el-radio-group v-model="importForm.contractType">
            <el-radio :value="1">收入合同</el-radio>
            <el-radio :value="2">支出合同</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="模板文件" required>
          <input ref="fileInputRef" type="file" accept=".doc,.docx,.txt" @change="onFileChange" />
          <div class="tip">支持 doc/docx/txt。导入后将自动识别下划线或 `{{变量}}` 区域。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="submitImport">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)
const importing = ref(false)
const list = ref<any[]>([])
const filter = reactive<{ contractType?: number }>({})
const importDialogVisible = ref(false)
const selectedFile = ref<File | null>(null)
const fileInputRef = ref<HTMLInputElement>()
const importForm = reactive({
  templateName: '',
  contractType: 1
})

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await api.contract.template.list({ contractType: filter.contractType, onlyEnabled: 0 })
    list.value = res.code === 200 ? (res.data || []) : []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

const openImportDialog = () => {
  importDialogVisible.value = true
  importForm.templateName = ''
  importForm.contractType = 1
  selectedFile.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
}

const onFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  selectedFile.value = target.files?.[0] || null
}

const submitImport = async () => {
  if (!importForm.templateName.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('请选择模板文件')
    return
  }
  importing.value = true
  try {
    const fd = new FormData()
    fd.append('file', selectedFile.value)
    fd.append('templateName', importForm.templateName.trim())
    fd.append('contractType', String(importForm.contractType))
    const res: any = await api.contract.template.importDocx(fd)
    if (res.code !== 200) {
      ElMessage.error(res.message || '导入失败')
      return
    }
    ElMessage.success('模板导入成功')
    importDialogVisible.value = false
    await loadList()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '导入失败')
  } finally {
    importing.value = false
  }
}

const toggleStatus = async (row: any) => {
  const toStatus = row.status === 1 ? 0 : 1
  try {
    const res: any = await api.contract.template.update({ id: row.id, status: toStatus })
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      return
    }
    ElMessage.success('状态已更新')
    await loadList()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
  }
}

const removeTemplate = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除模板“${row.templateName}”？`, '提示', { type: 'warning' })
    const res: any = await api.contract.template.delete(row.id)
    if (res.code !== 200) {
      ElMessage.error(res.message || '删除失败')
      return
    }
    ElMessage.success('删除成功')
    await loadList()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data?.message || e?.message || '删除失败')
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.template-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px; }
.header-actions { display: flex; gap: 12px; }
.tip { color: #909399; margin-top: 6px; font-size: 12px; }
</style>
