<template>
  <div class="contract-edit-container">
    <div class="page-header">
      <div class="header-left">
        <h2>{{ isEdit ? '编辑合同' : (form.contractType === 1 ? '收入合同签订' : '支出合同签订') }}</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>合同管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEdit ? '编辑' : '新建' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleBack">返回</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </div>
    </div>

    <el-card class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-divider content-position="left">合同基本信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="合同类型" prop="contractType">
              <el-radio-group v-model="form.contractType" :disabled="isEdit">
                <el-radio :value="1">收入合同</el-radio>
                <el-radio :value="2">支出合同</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="合同编号">
              <el-input v-model="form.contractNo" disabled placeholder="保存后自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" filterable :disabled="isEdit">
                <el-option v-for="p in projectList" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同名称" prop="contractName">
              <el-input v-model="form.contractName" placeholder="请输入合同名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="合同模板">
              <el-select v-model="form.templateId" placeholder="选择模板" style="width: 100%">
                <el-option label="施工合同模板" value="1" />
                <el-option label="设计合同模板" value="2" />
                <el-option label="采购合同模板" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="合同类型" prop="bizType">
              <el-select v-model="form.bizType" placeholder="请选择" style="width: 100%">
                <el-option label="施工合同" value="1" />
                <el-option label="设计合同" value="2" />
                <el-option label="咨询合同" value="3" />
                <el-option label="其他" value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">金额信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="合同含税金额" prop="totalAmount">
              <el-input-number v-model="form.totalAmount" :min="0" :precision="2" :step="10000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="不含税金额">
              <el-input-number v-model="form.amountWithoutTax" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="税率">
              <el-select v-model="form.taxRate" placeholder="请选择" style="width: 100%">
                <el-option label="0%" :value="0" />
                <el-option label="1%" :value="0.01" />
                <el-option label="3%" :value="0.03" />
                <el-option label="6%" :value="0.06" />
                <el-option label="9%" :value="0.09" />
                <el-option label="13%" :value="0.13" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">甲方/乙方信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="甲方(委托方)">
              <el-input v-model="form.partyA" placeholder="请输入甲方名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="乙方(承包方)">
              <el-input v-model="form.partyB" placeholder="请输入乙方名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商/分包商" v-if="form.contractType === 2">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%" filterable>
                <el-option v-for="s in supplierList" :key="s.id" :label="s.supplierName" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="签订日期" prop="signDate">
              <el-date-picker v-model="form.signDate" type="date" placeholder="请选择" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="付款方式">
              <el-select v-model="form.paymentType" placeholder="请选择" style="width: 100%">
                <el-option label="按进度" value="1" />
                <el-option label="按节点" value="2" />
                <el-option label="一次性" value="3" />
                <el-option label="其他" value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">时间信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="合同开始日期">
              <el-date-picker v-model="form.startDate" type="date" placeholder="请选择" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="合同结束日期">
              <el-date-picker v-model="form.endDate" type="date" placeholder="请选择" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="保修期(月)">
              <el-input-number v-model="form.warrantyPeriod" :min="0" :max="120" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">其他信息</el-divider>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>

        <el-form-item label="合同文件">
          <el-upload action="/api/common/upload" list-type="file" :limit="5" :on-success="handleUploadSuccess">
            <el-button type="primary">上传合同文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const route = useRoute()
const router = useRouter()

const query = route.query
const isEdit = computed(() => !!query.id)
const isExpenseContract = computed(() => route.query.expense === '1')

const formRef = ref()
const form = reactive({
  contractType: query.type === 'income' ? 1 : 2,
  contractNo: '',
  projectId: null as number | null,
  projectName: '',
  contractName: '',
  templateId: '',
  bizType: '',
  totalAmount: 0,
  amountWithoutTax: 0,
  taxRate: 0.06,
  partyA: '',
  partyB: '',
  supplierId: null as number | null,
  signDate: '',
  paymentType: '',
  startDate: '',
  endDate: '',
  warrantyPeriod: 0,
  remark: '',
  attachments: [] as string[]
})

const rules = {
  contractType: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  totalAmount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
  signDate: [{ required: true, message: '请选择签订日期', trigger: 'change' }]
}

const projectList = ref<any[]>([])

const supplierList = ref<any[]>([
  { id: 1, supplierName: 'XXX供应商' },
  { id: 2, supplierName: 'YYY供应商' }
])

const loadProjects = async () => {
  try {
    const res: any = await api.project.list()
    if (res.code === 200) projectList.value = res.data || []
  } catch {
    projectList.value = []
  }
}

const loadData = async () => {
  await loadProjects()
  if (!query.id) return
  const idNum = Number(query.id)
  if (isExpenseContract.value) {
    try {
      const res: any = await api.contract.expense.get(idNum)
      if (res.code !== 200 || !res.data) {
        ElMessage.error(res.message || '加载支出合同失败')
        return
      }
      const d = res.data
      form.contractType = 2
      form.contractNo = d.contractNo || ''
      form.contractName = d.contractName || ''
      form.projectId = d.projectId ?? null
      form.projectName = d.projectName || ''
      form.totalAmount = Number(d.totalAmountWithTax ?? d.totalAmount ?? 0)
      form.taxRate = d.taxRate != null ? Number(d.taxRate) : 0.06
      form.supplierId = d.supplierId ?? null
      form.signDate = d.signDate || ''
      form.startDate = d.startDate || ''
      form.endDate = d.endDate || ''
      form.paymentType = d.paymentType != null ? String(d.paymentType) : ''
      form.remark = d.remark || ''
    } catch (e: any) {
      ElMessage.error(e.message || '加载失败')
    }
    return
  }
  try {
    const res: any = await api.contract.income.get(idNum)
    if (res.code !== 200 || !res.data) {
      ElMessage.error(res.message || '加载收入合同失败')
      return
    }
    const d = res.data
    form.contractType = 1
    form.contractNo = d.contractNo || ''
    form.contractName = d.contractName || ''
    form.projectId = d.projectId ?? null
    form.projectName = d.projectName || ''
    form.totalAmount = Number(d.totalAmountWithTax ?? d.totalAmount ?? 0)
    form.taxRate = d.taxRate != null ? Number(d.taxRate) : 0.06
    form.partyA = d.partyA || ''
    form.partyB = d.partyB || ''
    form.signDate = d.signDate || ''
    form.startDate = d.startDate || ''
    form.endDate = d.endDate || ''
    form.paymentType = d.paymentType != null ? String(d.paymentType) : ''
    form.remark = d.remark || ''
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  }
}

onMounted(() => {
  loadData()
})

const handleBack = () => router.push('/contract')

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    if (isExpenseContract.value && query.id) {
      const res: any = await api.contract.expense.update({
        id: Number(query.id),
        contractNo: form.contractNo,
        contractName: form.contractName,
        projectId: form.projectId,
        projectName: form.projectName,
        totalAmount: form.totalAmount,
        totalAmountWithTax: form.totalAmount,
        taxRate: form.taxRate,
        supplierId: form.supplierId,
        signDate: form.signDate,
        startDate: form.startDate,
        endDate: form.endDate,
        paymentType: form.paymentType ? Number(form.paymentType) : undefined,
        remark: form.remark
      })
      if (res.code !== 200) {
        ElMessage.error(res.message || '保存失败')
        return
      }
      ElMessage.success('保存成功')
      router.push('/contract')
      return
    }
    if (query.id && !isExpenseContract.value) {
      const res: any = await api.contract.income.update({
        id: Number(query.id),
        contractType: 1,
        contractNo: form.contractNo,
        contractName: form.contractName,
        projectId: form.projectId,
        projectName: form.projectName,
        totalAmount: form.totalAmount,
        totalAmountWithTax: form.totalAmount,
        taxRate: form.taxRate,
        partyA: form.partyA,
        partyB: form.partyB,
        signDate: form.signDate,
        startDate: form.startDate,
        endDate: form.endDate,
        paymentType: form.paymentType ? Number(form.paymentType) : undefined,
        remark: form.remark
      })
      if (res.code !== 200) {
        ElMessage.error(res.message || '保存失败')
        return
      }
      ElMessage.success('保存成功')
      router.push('/contract')
      return
    }
    if (!query.id && form.contractType === 2) {
      const res: any = await api.contract.expense.create({
        contractNo: form.contractNo,
        contractName: form.contractName,
        projectId: form.projectId,
        projectName: form.projectName,
        totalAmount: form.totalAmount,
        totalAmountWithTax: form.totalAmount,
        taxRate: form.taxRate,
        supplierId: form.supplierId,
        signDate: form.signDate,
        startDate: form.startDate,
        endDate: form.endDate,
        paymentType: form.paymentType ? Number(form.paymentType) : undefined,
        remark: form.remark
      })
      if (res.code !== 200) {
        ElMessage.error(res.message || '保存失败')
        return
      }
      ElMessage.success('保存成功')
      router.push('/contract')
      return
    }
    if (!query.id && form.contractType === 1) {
      const res: any = await api.contract.income.create({
        contractType: 1,
        contractNo: form.contractNo,
        contractName: form.contractName,
        projectId: form.projectId,
        projectName: form.projectName,
        totalAmount: form.totalAmount,
        totalAmountWithTax: form.totalAmount,
        taxRate: form.taxRate,
        partyA: form.partyA,
        partyB: form.partyB,
        signDate: form.signDate,
        startDate: form.startDate,
        endDate: form.endDate,
        paymentType: form.paymentType ? Number(form.paymentType) : undefined,
        remark: form.remark
      })
      if (res.code !== 200) {
        ElMessage.error(res.message || '保存失败')
        return
      }
      ElMessage.success('保存成功')
      router.push('/contract')
      return
    }
    ElMessage.warning('请选择合同类型并完善表单')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleUploadSuccess = (res: any) => {
  if (res.code === 200) form.attachments.push(res.data)
}
</script>

<style scoped>
.contract-edit-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.form-card { background: #fff; border-radius: 8px; }
</style>