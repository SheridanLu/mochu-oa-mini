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
            <el-form-item label="合同模板" prop="templateId">
              <el-select v-model="form.templateId" placeholder="选择模板" style="width: 100%" @change="handleTemplateChange">
                <el-option v-for="item in templateOptions" :key="item.id" :label="item.templateName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6" v-if="form.contractType === 2">
            <el-form-item label="支出合同分类" prop="expenseContractCategory">
              <el-select v-model="form.expenseContractCategory" placeholder="请选择" style="width: 100%">
                <el-option v-for="item in expenseCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
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
          <AttachmentUpload :limit="30" @uploaded="handleUploadSuccess">
            <el-button type="primary">上传合同文件</el-button>
          </AttachmentUpload>
        </el-form-item>
        <el-form-item v-if="form.contractType === 1" label="广联达倒表">
          <div class="import-box">
            <input ref="glodonImportRef" type="file" accept=".xlsx,.xls,.csv,.txt" @change="handleImportGlodon" />
            <div class="import-tip">支持广联达导出 Excel/CSV/TXT，自动回填甲方、金额、税率等字段。</div>
          </div>
        </el-form-item>
        <el-form-item v-if="glodonRows.length && form.contractType === 1" label="广联达预览">
          <div style="width: 100%">
            <div class="glodon-toolbar">
              <div class="import-tip">系统内可直接阅读广联达倒表结果，并可自动生成采购清单草稿。</div>
              <div>
                <el-button size="small" @click="glodonPreviewVisible = true">系统内阅读</el-button>
                <el-button type="primary" size="small" @click="handleCreatePurchaseDraftFromGlodon">生成采购清单草稿并推送预算员待办</el-button>
              </div>
            </div>
            <el-table :data="glodonRows" border size="small" style="width: 100%">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="projectName" label="项目名称" min-width="160" />
              <el-table-column prop="partyA" label="甲方" min-width="140" />
              <el-table-column prop="amountWithTax" label="含税金额" width="120" />
              <el-table-column prop="amountWithoutTax" label="不含税金额" width="120" />
              <el-table-column prop="taxRate" label="税率" width="100" />
            </el-table>
          </div>
        </el-form-item>
        <el-form-item v-if="form.contractType === 2 && (form.expenseContractCategory === 'EQUIPMENT' || form.expenseContractCategory === 'MATERIAL')" label="清单倒表">
          <div class="import-box">
            <input ref="itemImportRef" type="file" accept=".xlsx,.xls,.csv" @change="handleImportItems" />
            <div class="import-tip">支持 Excel/CSV；自动识别名称、规格、单位、数量、单价并回填合同正文。</div>
          </div>
        </el-form-item>
        <el-form-item v-if="importedItems.length" label="导入清单预览">
          <el-table :data="importedItems" border size="small" style="width: 100%">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="name" label="名称" min-width="160" />
            <el-table-column prop="spec" label="规格/型号" min-width="120" />
            <el-table-column prop="brand" label="品牌" min-width="120" />
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="qty" label="数量" width="100" />
            <el-table-column prop="price" label="单价" width="120" />
          </el-table>
        </el-form-item>

        <el-divider content-position="left">智能合同正文（仅可填写白色区域）</el-divider>
        <div v-if="form.contractType === 2" class="print-toolbar">
          <div class="watermark-tip">支出合同打印水印：MOCHU-OA 合同专用水印</div>
          <el-button type="primary" plain @click="handlePrintContract">一键生成打印文件</el-button>
        </div>
        <div class="smart-doc" v-if="smartSegments.length">
          <template v-for="(seg, idx) in smartSegments" :key="idx">
            <span v-if="seg.type === 'text'" class="seg-text">{{ seg.text }}</span>
            <el-input
              v-else
              v-model="templateFieldValues[seg.key]"
              class="seg-input"
              size="small"
              :placeholder="seg.key"
            />
          </template>
        </div>
        <el-empty v-else description="请先选择合同模板，系统将自动解析可编辑区" />
      </el-form>
    </el-card>
    <el-dialog v-model="glodonPreviewVisible" title="广联达文件系统内阅读" width="900px">
      <el-table :data="glodonRows" border height="520">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="projectName" label="项目名称" min-width="180" />
        <el-table-column prop="partyA" label="甲方" min-width="180" />
        <el-table-column prop="amountWithTax" label="含税金额" width="120" />
        <el-table-column prop="amountWithoutTax" label="不含税金额" width="130" />
        <el-table-column prop="taxRate" label="税率" width="100" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
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
  templateId: null as number | null,
  expenseContractCategory: '',
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
  templateId: [{
    validator: (_rule: any, value: any, callback: any) => {
      if (form.contractType === 2 && !value) {
        callback(new Error('支出合同必须选择合同模板'))
        return
      }
      callback()
    },
    trigger: 'change'
  }],
  expenseContractCategory: [{
    validator: (_rule: any, value: any, callback: any) => {
      if (form.contractType === 2 && !value) {
        callback(new Error('请选择支出合同分类'))
        return
      }
      callback()
    },
    trigger: 'change'
  }],
  totalAmount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
  signDate: [{ required: true, message: '请选择签订日期', trigger: 'change' }]
}

const projectList = ref<any[]>([])
const templateOptions = ref<any[]>([])
const templateFieldValues = reactive<Record<string, string>>({})
const smartSegments = ref<Array<{ type: 'text' | 'field'; text?: string; key?: string }>>([])
const templateMeta = reactive({
  version: 0,
  contentHash: ''
})

const supplierList = ref<any[]>([])
const expenseCategoryOptions = ref<Array<{ value: string; label: string }>>([])
const importedItems = ref<Array<{ name: string; spec: string; unit: string; qty: string; price: string }>>([])
const itemImportRef = ref<HTMLInputElement>()
const glodonRows = ref<any[]>([])
const glodonImportRef = ref<HTMLInputElement>()
const glodonPreviewVisible = ref(false)

const loadProjects = async () => {
  try {
    const res: any = await api.project.list()
    if (res.code === 200) projectList.value = res.data || []
  } catch {
    projectList.value = []
  }
}

const loadSuppliers = async () => {
  try {
    const res: any = await api.supplier.list()
    supplierList.value = res.code === 200 ? (res.data || []) : []
  } catch {
    supplierList.value = []
  }
}

const loadExpenseCategories = async () => {
  try {
    const res: any = await api.contract.expense.categories()
    expenseCategoryOptions.value = res.code === 200 ? (res.data || []) : []
  } catch {
    expenseCategoryOptions.value = []
  }
}

const loadTemplates = async () => {
  try {
    const res: any = await api.contract.template.list({
      contractType: form.contractType,
      onlyEnabled: 1
    })
    templateOptions.value = res.code === 200 ? (res.data || []) : []
  } catch {
    templateOptions.value = []
  }
}

const parseSmartSegments = (content: string) => {
  const segments: Array<{ type: 'text' | 'field'; text?: string; key?: string }> = []
  const placeholderRegex = /\{\{\s*([^{}\s]+)\s*}}|_{3,}/g
  let lastIndex = 0
  let autoIndex = 1
  let match: RegExpExecArray | null
  while ((match = placeholderRegex.exec(content)) !== null) {
    if (match.index > lastIndex) {
      segments.push({ type: 'text', text: content.slice(lastIndex, match.index) })
    }
    const key = match[1] ? match[1].trim() : `FIELD_${autoIndex++}`
    if (!(key in templateFieldValues)) templateFieldValues[key] = ''
    segments.push({ type: 'field', key })
    lastIndex = match.index + match[0].length
  }
  if (lastIndex < content.length) {
    segments.push({ type: 'text', text: content.slice(lastIndex) })
  }
  smartSegments.value = segments
}

const fillTemplateDefaults = (defaults: Record<string, string>) => {
  const currentSupplier = supplierList.value.find((s: any) => Number(s.id) === Number(form.supplierId))
  const supplierName = String(currentSupplier?.supplierName || form.partyB || '')
  const aliasMap: Record<string, string[]> = {
    合同编号: ['合同编号'],
    合同名称: ['合同名称'],
    甲方: ['甲方'],
    乙方: ['乙方', '供应商'],
    品牌: ['品牌', '品牌名称', 'brand'],
    规格型号: ['规格', '型号', '规格型号', '产品规格', 'spec', 'model'],
    项目名称: ['项目名称'],
    项目编号: ['项目编号'],
    签订日期: ['签订日期', '合同签订日期']
  }
  Object.entries(aliasMap).forEach(([field, aliases]) => {
    const v = field === '合同编号' ? form.contractNo
      : field === '合同名称' ? form.contractName
      : field === '甲方' ? form.partyA
      : field === '乙方' ? form.partyB
      : field === '品牌' ? supplierName
      : field === '规格型号' ? String(defaults['规格型号'] || defaults['产品规格'] || '')
      : field === '项目名称' ? form.projectName
      : field === '项目编号' ? String(projectList.value.find((p: any) => p.id === form.projectId)?.projectNo || '')
      : field === '签订日期' ? form.signDate
      : ''
    aliases.forEach((a) => {
      if (a in templateFieldValues) templateFieldValues[a] = v || defaults[a] || ''
    })
  })
  Object.keys(templateFieldValues).forEach((k) => {
    if (!templateFieldValues[k] && defaults[k]) templateFieldValues[k] = defaults[k]
  })
}

const hasTemplatePlaceholder = (aliases: string[]) => {
  const keys = Object.keys(templateFieldValues)
  const normalized = aliases.map((x) => x.toLowerCase())
  return keys.some((k) => {
    const kk = k.toLowerCase()
    return normalized.some((a) => kk.includes(a))
  })
}

const validateTemplateStructure = () => {
  if (form.contractType !== 2 || !form.expenseContractCategory) return
  const required: Array<{ aliases: string[]; tip: string }> = [
    { aliases: ['物资名称', '名称', 'material', 'item'], tip: '模板建议包含“物资名称”占位字段' },
    { aliases: ['单位', 'unit'], tip: '模板建议包含“单位”占位字段' },
    { aliases: ['数量', 'qty', 'quantity'], tip: '模板建议包含“数量”占位字段' }
  ]
  if (['EQUIPMENT', 'MATERIAL'].includes(form.expenseContractCategory)) {
    required.push({ aliases: ['品牌', 'brand'], tip: '设备/材料合同模板建议包含“品牌”占位字段' })
  }
  if (['EQUIPMENT', 'MATERIAL'].includes(form.expenseContractCategory)) {
    required.push({ aliases: ['规格', '型号', 'spec', 'model'], tip: '设备/材料合同模板建议包含“规格/型号”占位字段' })
  }
  if (form.expenseContractCategory === 'LABOR') {
    required.push({ aliases: ['工种', '班组', 'labor', 'team'], tip: '劳务合同模板建议包含“工种/班组”占位字段' })
  }
  if (form.expenseContractCategory === 'SOFTWARE') {
    required.push({ aliases: ['授权', 'license', '许可'], tip: '软件合同模板建议包含“授权/许可”占位字段' })
  }
  if (form.expenseContractCategory === 'PROFESSIONAL_SUBCONTRACT') {
    required.push({ aliases: ['分包范围', 'subcontract', '专业'], tip: '专业分包模板建议包含“分包范围”占位字段' })
  }
  const missed = required.filter((r) => !hasTemplatePlaceholder(r.aliases)).map((r) => r.tip)
  if (missed.length) {
    ElMessage.warning(`当前模板字段可能不完整：${missed.join('；')}`)
  }
}

const handleTemplateChange = async () => {
  Object.keys(templateFieldValues).forEach((k) => delete templateFieldValues[k])
  smartSegments.value = []
  templateMeta.version = 0
  templateMeta.contentHash = ''
  if (!form.templateId) return
  try {
    const res: any = await api.contract.template.draft(form.templateId, {
      projectId: form.projectId || undefined,
      supplierId: form.supplierId || undefined
    })
    if (res.code !== 200 || !res.data?.templateContent) {
      ElMessage.error(res.message || '加载模板失败')
      return
    }
    templateMeta.version = Number(res.data.templateVersion || 0)
    templateMeta.contentHash = String(res.data.templateContentHash || '')
    parseSmartSegments(String(res.data.templateContent))
    fillTemplateDefaults(res.data.defaults || {})
    validateTemplateStructure()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载模板失败')
  }
}

const loadSignedPayload = (raw: string | undefined) => {
  if (!raw) return
  try {
    const parsed = JSON.parse(raw)
    const fields = parsed?.fields && typeof parsed.fields === 'object' ? parsed.fields : parsed
    Object.keys(templateFieldValues).forEach((k) => delete templateFieldValues[k])
    Object.entries(fields || {}).forEach(([k, v]) => {
      templateFieldValues[k] = String(v ?? '')
    })
    templateMeta.version = Number(parsed?.templateVersion || 0)
    templateMeta.contentHash = String(parsed?.templateContentHash || '')
  } catch {
    // ignore legacy non-json payload
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
      form.expenseContractCategory = d.contractType || ''
      form.supplierId = d.supplierId ?? null
      form.signDate = d.signDate || ''
      form.startDate = d.startDate || ''
      form.endDate = d.endDate || ''
      form.paymentType = d.paymentType != null ? String(d.paymentType) : ''
      form.remark = d.remark || ''
      if (d.templateId) form.templateId = Number(d.templateId)
      if (form.templateId) {
        await handleTemplateChange()
        loadSignedPayload(d.signedFilePath)
      }
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
    if (d.templateId) form.templateId = Number(d.templateId)
    if (form.templateId) {
      await handleTemplateChange()
      loadSignedPayload(d.signedFilePath)
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  }
}

onMounted(() => {
  loadData()
  loadSuppliers()
  loadExpenseCategories()
  loadTemplates()
})

watch(() => form.contractType, () => {
  form.templateId = null
  Object.keys(templateFieldValues).forEach((k) => delete templateFieldValues[k])
  smartSegments.value = []
  loadTemplates()
})

watch(() => form.projectId, () => {
  const p = projectList.value.find((x: any) => Number(x.id) === Number(form.projectId))
  if (p) {
    form.projectName = p.projectName || p.name || ''
    if (!form.partyA) form.partyA = p.ownerName || ''
  }
})

watch(() => form.supplierId, () => {
  const s = supplierList.value.find((x: any) => Number(x.id) === Number(form.supplierId))
  if (s && form.contractType === 2) {
    form.partyB = s.supplierName || ''
  }
})

watch(() => form.expenseContractCategory, () => {
  importedItems.value = []
  if (itemImportRef.value) itemImportRef.value.value = ''
})

watch(() => form.contractType, () => {
  if (form.contractType !== 1) {
    glodonRows.value = []
    if (glodonImportRef.value) glodonImportRef.value.value = ''
  }
})

const handleBack = () => router.push('/contract')

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.contractType === 2) {
    const ruleErr = validateExpenseCategoryRules()
    if (ruleErr) {
      ElMessage.warning(ruleErr)
      return
    }
  }
  if (form.contractType === 2 && !Object.keys(templateFieldValues).length) {
    ElMessage.warning('请填写合同正文中的下划线字段')
    return
  }
  if (form.contractType === 1 && !form.templateId && !form.attachments.length) {
    ElMessage.warning('收入合同不使用模板时，请至少上传一个合同附件')
    return
  }
  const signedPayload = form.templateId
    ? JSON.stringify({
      mode: 'template',
      templateId: form.templateId,
      templateVersion: templateMeta.version,
      templateContentHash: templateMeta.contentHash,
      fields: templateFieldValues
    })
    : JSON.stringify({
      mode: 'attachment',
      attachments: form.attachments
    })

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
        contractType: form.expenseContractCategory,
        taxRate: form.taxRate,
        templateId: form.templateId,
        templateVersion: templateMeta.version,
        templateContentHash: templateMeta.contentHash,
        signedFilePath: signedPayload,
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
        templateId: form.templateId,
        templateVersion: templateMeta.version,
        templateContentHash: templateMeta.contentHash,
        signedFilePath: signedPayload,
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
        contractType: form.expenseContractCategory,
        taxRate: form.taxRate,
        templateId: form.templateId,
        templateVersion: templateMeta.version,
        templateContentHash: templateMeta.contentHash,
        signedFilePath: signedPayload,
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
        templateId: form.templateId,
        templateVersion: templateMeta.version,
        templateContentHash: templateMeta.contentHash,
        signedFilePath: signedPayload,
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

const handleUploadSuccess = (url: string, res: any) => {
  if (res?.code === 200 && url) form.attachments.push(url)
}

const handleImportItems = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  if (!form.expenseContractCategory || !['EQUIPMENT', 'MATERIAL'].includes(form.expenseContractCategory)) {
    ElMessage.warning('请先选择设备合同或材料合同分类')
    target.value = ''
    return
  }
  try {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('category', form.expenseContractCategory)
    const res: any = await api.contract.expense.importItems(fd)
    if (res.code !== 200) {
      ElMessage.error(res.message || '清单导入失败')
      return
    }
    importedItems.value = (res.data?.rows || []) as any[]
    templateFieldValues['清单明细'] = String(res.data?.mergedText || '')
    templateFieldValues['物资名称'] = importedItems.value.map(x => x.name).filter(Boolean).join('、')
    templateFieldValues['品牌'] = importedItems.value.map(x => x.brand).filter(Boolean).join('、')
    templateFieldValues['数量'] = importedItems.value.map(x => x.qty).filter(Boolean).join('、')
    if (form.expenseContractCategory === 'MATERIAL') {
      templateFieldValues['规格型号'] = importedItems.value.map(x => x.spec).filter(Boolean).join('、')
    }
    ElMessage.success(`倒表成功，已导入 ${res.data?.count || importedItems.value.length} 条`)
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || err?.message || '清单导入失败')
  } finally {
    target.value = ''
  }
}

const handleImportGlodon = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res: any = await api.contract.income.importGlodon(fd)
    if (res.code !== 200) {
      ElMessage.error(res.message || '广联达倒表失败')
      return
    }
    const data = res.data || {}
    glodonRows.value = data.rows || []
    if (data.projectName && !form.projectName) form.projectName = String(data.projectName)
    if (data.partyA) form.partyA = String(data.partyA)
    if (data.totalAmountWithTax != null) form.totalAmount = Number(data.totalAmountWithTax) || 0
    if (data.amountWithoutTax != null) form.amountWithoutTax = Number(data.amountWithoutTax) || 0
    if (data.taxRate != null && Number(data.taxRate) >= 0) form.taxRate = Number(data.taxRate)
    templateFieldValues['广联达倒表摘要'] = String(data.summary || '')
    ElMessage.success(`广联达倒表成功，共 ${glodonRows.value.length} 行`)
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || err?.message || '广联达倒表失败')
  } finally {
    target.value = ''
  }
}

const handleCreatePurchaseDraftFromGlodon = async () => {
  if (!glodonRows.value.length) {
    ElMessage.warning('请先导入广联达文件')
    return
  }
  if (!form.projectId) {
    ElMessage.warning('请先选择项目后再生成采购清单草稿')
    return
  }
  try {
    const res: any = await api.contract.income.createPurchaseDraft({
      projectId: form.projectId,
      projectName: form.projectName,
      contractNo: form.contractNo,
      contractName: form.contractName,
      rows: glodonRows.value
    })
    if (res.code !== 200) {
      ElMessage.error(res.message || '生成采购清单草稿失败')
      return
    }
    const info = res.data || {}
    ElMessage.success(`已生成采购草稿${info.purchaseNo ? `(${info.purchaseNo})` : ''}，并${info.todoCreated ? '' : '未'}推送预算员待办`)
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '生成采购清单草稿失败')
  }
}

const handlePrintContract = async () => {
  if (form.contractType !== 2) {
    ElMessage.info('当前仅支出合同启用模板水印打印')
    return
  }
  if (!smartSegments.value.length) {
    ElMessage.warning('请先选择并加载支出合同模板')
    return
  }
  if (!Object.keys(templateFieldValues).length) {
    ElMessage.warning('请先填写模板字段后再生成打印文件')
    return
  }
  try {
    const payload = {
      id: query.id ? Number(query.id) : undefined,
      contractName: form.contractName,
      templateId: form.templateId,
      templateVersion: templateMeta.version,
      templateContentHash: templateMeta.contentHash,
      signedFilePath: JSON.stringify({
        mode: 'template',
        templateId: form.templateId,
        templateVersion: templateMeta.version,
        templateContentHash: templateMeta.contentHash,
        fields: templateFieldValues
      })
    }
    const res: any = await api.contract.expense.generatePrintFile(payload)
    if (res.code !== 200 || !res.data?.url) {
      ElMessage.error(res.message || '生成打印文件失败')
      return
    }
    window.open(res.data.url, '_blank')
    ElMessage.success('已生成打印文件')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '生成打印文件失败')
  }
}

const hasAnyFilledField = (aliases: string[]) => {
  const keys = Object.keys(templateFieldValues)
  const normalized = aliases.map((x) => x.toLowerCase())
  for (const key of keys) {
    const val = (templateFieldValues[key] || '').trim()
    if (!val) continue
    const k = key.toLowerCase()
    if (normalized.some((a) => k.includes(a))) return true
  }
  return false
}

const validateExpenseCategoryRules = (): string => {
  if (!form.expenseContractCategory) return '请选择支出合同分类'
  const commonRequired = [
    { aliases: ['物资名称', '名称', 'material', 'item'], tip: '请填写物资名称' },
    { aliases: ['单位', 'unit'], tip: '请填写单位' },
    { aliases: ['数量', 'qty', 'quantity'], tip: '请填写数量' }
  ]
  const materialRequired = [
    { aliases: ['品牌', 'brand'], tip: '设备/材料合同需填写品牌' },
    { aliases: ['规格', '型号', 'spec', 'model'], tip: '材料合同需填写规格/型号' }
  ]
  const equipmentRequired = [
    { aliases: ['品牌', 'brand'], tip: '设备合同需填写品牌' },
    { aliases: ['规格', '型号', 'spec', 'model'], tip: '设备合同需填写产品规格/型号' }
  ]
  const laborRequired = [
    { aliases: ['工种', '班组', 'labor', 'team'], tip: '劳务合同需填写工种/班组信息' }
  ]
  const softwareRequired = [
    { aliases: ['授权', 'license', '许可'], tip: '软件合同需填写授权/许可信息' }
  ]
  const subcontractRequired = [
    { aliases: ['分包范围', 'subcontract', '专业'], tip: '专业分包合同需填写分包范围' }
  ]
  for (const r of commonRequired) {
    if (!hasAnyFilledField(r.aliases)) return r.tip
  }
  if (form.expenseContractCategory === 'MATERIAL') {
    for (const r of materialRequired) if (!hasAnyFilledField(r.aliases)) return r.tip
  }
  if (form.expenseContractCategory === 'EQUIPMENT') {
    for (const r of equipmentRequired) if (!hasAnyFilledField(r.aliases)) return r.tip
  }
  if (form.expenseContractCategory === 'LABOR') {
    for (const r of laborRequired) if (!hasAnyFilledField(r.aliases)) return r.tip
  }
  if (form.expenseContractCategory === 'SOFTWARE') {
    for (const r of softwareRequired) if (!hasAnyFilledField(r.aliases)) return r.tip
  }
  if (form.expenseContractCategory === 'PROFESSIONAL_SUBCONTRACT') {
    for (const r of subcontractRequired) if (!hasAnyFilledField(r.aliases)) return r.tip
  }
  return ''
}
</script>

<style scoped>
.contract-edit-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.form-card { background: #fff; border-radius: 8px; }
.smart-doc {
  border: 1px solid #ebeef5;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  line-height: 1.9;
  white-space: pre-wrap;
}
.print-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.watermark-tip {
  color: #909399;
  font-size: 12px;
}
.seg-text {
  background: #f5f7fa;
  color: #606266;
  user-select: none;
}
.seg-input {
  display: inline-flex;
  width: 180px;
  margin: 0 6px;
  vertical-align: middle;
}
.import-box {
  width: 100%;
}
.import-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
.glodon-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
</style>