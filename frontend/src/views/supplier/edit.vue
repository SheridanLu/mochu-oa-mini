<template>
  <div class="supplier-edit-container">
    <div class="page-header">
      <div class="header-left">
        <h2>{{ isView ? '供应商详情' : (isEdit ? '编辑供应商' : '新增供应商') }}</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>供应商管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isView ? '详情' : (isEdit ? '编辑' : '新增') }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleBack">返回</el-button>
        <el-button v-if="!isView" type="primary" @click="handleSubmit">保存</el-button>
      </div>
    </div>

    <el-card class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" :disabled="isView">
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="供应商编号">
              <el-input v-model="form.supplierNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商名称" prop="supplierName">
              <el-input v-model="form.supplierName" placeholder="请输入供应商名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商类型" prop="supplierType">
              <el-select v-model="form.supplierType" placeholder="请选择" style="width: 100%">
                <el-option label="材料供应商" :value="1" />
                <el-option label="设备供应商" :value="2" />
                <el-option label="劳务供应商" :value="3" />
                <el-option label="专业分包商" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="统一社会信用码">
              <el-input v-model="form.creditCode" placeholder="请输入统一社会信用码" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="成立日期">
              <el-date-picker v-model="form.establishedDate" type="date" placeholder="请选择" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="注册资本">
              <el-input-number v-model="form.registeredCapital" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">联系方式</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="联系人">
              <el-input v-model="form.contactName" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="电子邮箱">
              <el-input v-model="form.contactEmail" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="联系地址">
              <el-input v-model="form.address" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">资质信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="资质等级">
              <el-select v-model="form.qualificationLevel" placeholder="请选择" style="width: 100%">
                <el-option label="甲级" value="甲级" />
                <el-option label="乙级" value="乙级" />
                <el-option label="丙级" value="丙级" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="营业执照">
              <AttachmentUpload :limit="1" @uploaded="handleUploadSuccess">
                <el-button>上传营业执照</el-button>
              </AttachmentUpload>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="资质证书">
              <AttachmentUpload :limit="30">
                <el-button>上传资质证书</el-button>
              </AttachmentUpload>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">其他信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开户银行">
              <el-input v-model="form.bankName" placeholder="请输入开户银行" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行账号">
              <el-input v-model="form.bankAccount" placeholder="请输入银行账号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'
import { api } from '@/api'

const route = useRoute()
const router = useRouter()

const query = route.query
const isEdit = computed(() => !!query.id && !query.view)
const isView = computed(() => query.view === 'true')

const formRef = ref()
const form = reactive({
  supplierNo: '',
  supplierName: '',
  supplierType: 1,
  creditCode: '',
  establishedDate: '',
  registeredCapital: 0,
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  qualificationLevel: '',
  businessLicense: '',
  qualificationCert: '',
  bankName: '',
  bankAccount: '',
  remark: ''
})

const rules = {
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  supplierType: [{ required: true, message: '请选择供应商类型', trigger: 'change' }]
}

const loadData = async () => {
  if (query.id) {
    try {
      const res = await request<{ data: any }>({ url: `/supplier/${query.id}`, method: 'GET' })
      if (res.data) Object.assign(form, res.data)
    } catch (e) { console.error('加载失败', e) }
  }
}

onMounted(() => { loadData() })

const handleBack = () => router.push('/supplier')

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEdit.value) {
      await api.supplier.update(form)
    } else {
      await api.supplier.create(form)
    }
    ElMessage.success('保存成功')
    router.push('/supplier')
  } catch (e: any) { ElMessage.error(e.message || '保存失败') }
}

const handleUploadSuccess = (url: string, res: any) => {
  if (res?.code === 200 && url) form.businessLicense = url
}
</script>

<style scoped>
.supplier-edit-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.form-card { background: #fff; border-radius: 8px; }
</style>