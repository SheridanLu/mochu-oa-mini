<template>
  <div class='expense-edit-container'>
    <div class='page-header'>
      <div class='header-left'>
        <h2>{{ isEdit ? '编辑报销单' : '新建报销单' }}</h2>
        <el-breadcrumb separator='/'>
          <el-breadcrumb-item>财务管理</el-breadcrumb-item>
          <el-breadcrumb-item>日常报销</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEdit ? '编辑' : '新建' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class='header-right'>
        <el-button @click='handleBack'>返回</el-button>
        <el-button type='primary' @click='handleSubmit'>保存</el-button>
      </div>
    </div>
    <el-card class='form-card'>
      <el-form ref='formRef' :model='form' :rules='rules' label-width='120px'>
        <el-row :gutter='20'>
          <el-col :span='8'>
            <el-form-item label='报销人' prop='reporterName'>
              <el-input v-model='form.reporterName' placeholder='请输入报销人' />
            </el-form-item>
          </el-col>
          <el-col :span='8'>
            <el-form-item label='部门' prop='departmentName'>
              <el-input v-model='form.departmentName' placeholder='请输入部门' />
            </el-form-item>
          </el-col>
          <el-col :span='8'>
            <el-form-item label='关联项目' prop='projectName'>
              <el-input v-model='form.projectName' placeholder='请输入项目名称' />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter='20'>
          <el-col :span='8'>
            <el-form-item label='报销类型' prop='expenseType'>
              <el-select v-model='form.expenseType' placeholder='请选择' style='width: 100%'>
                <el-option label='差旅费' value='1' />
                <el-option label='餐饮费' value='2' />
                <el-option label='办公费' value='3' />
                <el-option label='其他' value='4' />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span='8'>
            <el-form-item label='报销金额' prop='totalAmount'>
              <el-input-number v-model='form.totalAmount' :min='0' :precision='2' style='width: 100%' />
            </el-form-item>
          </el-col>
          <el-col :span='8'>
            <el-form-item label='费用日期'>
              <el-date-picker v-model='form.expenseDate' type='date' placeholder='请选择' style='width: 100%' value-format='YYYY-MM-DD' />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label='事由说明'>
          <el-input v-model='form.remark' type='textarea' :rows='3' placeholder='请输入事由说明' />
        </el-form-item>
        <el-form-item label='附件'>
          <AttachmentUpload :limit='30' @uploaded='handleUploadSuccess'>
            <el-button type='primary'>上传附件</el-button>
          </AttachmentUpload>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang='ts'>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.query.id)
const formRef = ref()

const form = reactive({
  id: undefined as number | undefined,
  reporterName: '',
  departmentName: '',
  projectName: '',
  expenseType: '',
  totalAmount: 0,
  expenseDate: '',
  remark: '',
  attachments: [] as string[]
})

const rules = {
  reporterName: [{ required: true, message: '请输入报销人', trigger: 'blur' }],
  expenseType: [{ required: true, message: '请选择报销类型', trigger: 'change' }],
  totalAmount: [{ required: true, message: '请输入报销金额', trigger: 'blur' }]
}

const handleBack = () => router.push('/finance/expense')

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value && form.id) {
      await api.expense.report.update(form)
    } else {
      await api.expense.report.create(form)
    }
    ElMessage.success('保存成功')
    router.push('/finance/expense')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

const loadDetail = async () => {
  if (!isEdit.value) return
  try {
    const id = Number(route.query.id)
    if (!Number.isFinite(id) || id <= 0) return
    const res: any = await api.expense.report.get(id)
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data)
      form.id = id
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载报销单失败')
  }
}

const handleUploadSuccess = (url: string, res: any) => {
  if (res?.code === 200 && url) form.attachments.push(url)
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.expense-edit-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; }
.form-card { background: #fff; border-radius: 8px; }
</style>