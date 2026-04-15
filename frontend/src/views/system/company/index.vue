<template>
  <div class="company-container">
    <div class="page-header">
      <div class="header-left">
        <h2>公司信息管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>公司信息管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>新增公司
        </el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="公司名称">
          <el-input v-model="filterForm.companyName" placeholder="搜索公司名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="统一信用代码">
          <el-input v-model="filterForm.creditCode" placeholder="搜索信用代码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="companyName" label="公司名称" min-width="200" />
        <el-table-column prop="creditCode" label="统一社会信用代码" width="180" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="bankName" label="开户银行" width="150" />
        <el-table-column prop="bankAccount" label="银行账号" width="160" />
        <el-table-column prop="taxNo" label="纳税人识别号" width="160" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="formDialogVisible" :title="formTitle" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入公司全称" />
        </el-form-item>
        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="form.creditCode" placeholder="18位信用代码" />
        </el-form-item>
        <el-form-item label="公司地址">
          <el-input v-model="form.address" placeholder="请输入公司地址" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="开户银行">
          <el-input v-model="form.bankName" placeholder="请输入开户银行" />
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="form.bankAccount" placeholder="请输入银行账号" />
        </el-form-item>
        <el-form-item label="纳税人识别号">
          <el-input v-model="form.taxNo" placeholder="请输入纳税人识别号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" title="公司详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="公司名称">{{ currentRow?.companyName }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ currentRow?.creditCode }}</el-descriptions-item>
        <el-descriptions-item label="公司地址">{{ currentRow?.address }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentRow?.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow?.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="开户银行">{{ currentRow?.bankName }}</el-descriptions-item>
        <el-descriptions-item label="银行账号">{{ currentRow?.bankAccount }}</el-descriptions-item>
        <el-descriptions-item label="纳税人识别号">{{ currentRow?.taxNo }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentRow?.email }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)
const tableData = ref<any[]>([])
const formDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentRow = ref<any>(null)

const filterForm = reactive({ companyName: '', creditCode: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })

const form = reactive({
  id: 0,
  companyName: '',
  creditCode: '',
  address: '',
  contactName: '',
  contactPhone: '',
  bankName: '',
  bankAccount: '',
  taxNo: '',
  email: '',
  status: 1
})

const formRules: FormRules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  creditCode: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const formTitle = computed(() => form.id > 0 ? '编辑公司' : '新增公司')

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      companyName: filterForm.companyName,
      creditCode: filterForm.creditCode,
      page: pagination.page,
      size: pagination.size
    }
    const res = await api.company.search(params)
    if (res.code === 200) {
      tableData.value = res.data?.list || []
      pagination.total = res.data?.total || 0
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { filterForm.companyName = ''; filterForm.creditCode = ''; pagination.page = 1; fetchList() }
const handleRefresh = () => { fetchList(); ElMessage.success('刷新成功') }

const handleCreate = () => {
  form.id = 0
  form.companyName = ''
  form.creditCode = ''
  form.address = ''
  form.contactName = ''
  form.contactPhone = ''
  form.bankName = ''
  form.bankAccount = ''
  form.taxNo = ''
  form.email = ''
  form.status = 1
  formDialogVisible.value = true
}

const handleEdit = async (row: any) => {
  try {
    const res = await api.company.get(row.id)
    if (res.code === 200) {
      Object.assign(form, res.data)
      formDialogVisible.value = true
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '获取详情失败')
  }
}

const handleView = (row: any) => {
  currentRow.value = row
  viewDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    if (form.id > 0) {
      await api.company.update(form)
    } else {
      await api.company.create(form)
    }
    ElMessage.success('保存成功')
    formDialogVisible.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

const handleSizeChange = (size: number) => { pagination.size = size; fetchList() }
const handleCurrentChange = (page: number) => { pagination.page = page; fetchList() }

onMounted(() => { fetchList() })
</script>

<style scoped>
.company-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
</style>