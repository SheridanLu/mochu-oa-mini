<template>
  <div class="purchase-edit">
    <div class="page-header">
      <div class="header-left">
        <h2>{{ isEdit ? '编辑采购清单' : '新建采购清单' }}</h2>
        <div class="source-tag" v-if="form.sourceType">
          <el-tag :type="form.sourceType === 2 ? 'warning' : 'info'">
            {{ form.sourceType === 2 ? '来源：合同附件导入' : '手工创建' }}
          </el-tag>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="handleBack">取消</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" :disabled="!canSubmit" @click="handleSubmit">提交审批</el-button>
      </div>
    </div>

    <el-alert v-if="!canEdit" type="info" title="当前为只读模式，不可编辑" show-icon :closable="false" style="margin-bottom: 20px" />

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="base-card">
          <template #header><span>基础信息</span></template>
          <el-form :model="form" label-width="100px">
            <el-form-item label="关联项目" required>
              <el-select v-model="form.projectId" placeholder="请选择项目" :disabled="!canEdit" style="width: 100%">
                <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="关联合同" required>
              <el-select v-model="form.contractId" placeholder="请选择合同" :disabled="!canEdit" style="width: 100%" @change="handleContractChange">
                <el-option v-for="c in contractList" :key="c.id" :label="c.contractNo" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="isEdit" label="清单状态">
              <el-tag :type="getStatusType(form.status)">{{ getStatusText(form.status) }}</el-tag>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="items-card" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>物资明细</span>
              <div class="header-actions">
                <el-button type="primary" link @click="showBatchDialog = true" :disabled="!canEdit">批量写入</el-button>
                <el-button type="primary" link @click="handleReverseOrder" :disabled="!canEdit">倒序</el-button>
                <el-button type="primary" link @click="handleImportContract" :disabled="!canEdit">从合同附件导入</el-button>
                <el-button type="primary" link :disabled="!canEdit" @click="triggerExcelImport">Excel导入</el-button>
                <el-button type="primary" link @click="downloadExcelTemplate">下载导入模板</el-button>
                <input ref="excelImportRef" type="file" accept=".xlsx,.xls,.csv" style="display: none" @change="handleExcelImport" />
              </div>
            </div>
          </template>
          <el-table :data="itemList" stripe show-summary :summary-method="getSummary">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="materialName" label="物资名称" width="150">
              <template #default="{ row }">
                <el-input v-model="row.materialName" placeholder="名称" :disabled="!canEdit" />
              </template>
            </el-table-column>
            <el-table-column prop="specModel" label="规格型号" width="120">
              <template #default="{ row }">
                <el-input v-model="row.specModel" placeholder="规格" :disabled="!canEdit" />
              </template>
            </el-table-column>
            <el-table-column prop="brand" label="品牌" width="100">
              <template #default="{ row }">
                <el-input v-model="row.brand" placeholder="品牌" :disabled="!canEdit" />
                <el-tooltip v-if="row.requireBrand" content="设备类物资必须填写品牌" placement="top">
                  <el-icon color="#f56c6c"><WarningFilled /></el-icon>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80">
              <template #default="{ row }">
                <el-input v-model="row.unit" placeholder="单位" :disabled="!canEdit" />
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="计划数量" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="0" :disabled="!canEdit" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column prop="budgetPrice" label="预算单价" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.budgetPrice" :min="0" :precision="2" :disabled="!canEdit" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column prop="budgetTotal" label="预算合价" width="120">
              <template #default="{ row }">
                {{ formatAmount((row.quantity || 0) * (row.budgetPrice || 0)) }}
              </template>
            </el-table-column>
            <el-table-column prop="matchStatus" label="匹配" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.matchStatus === 'base'" type="success" size="small">基准库</el-tag>
                <el-tag v-else-if="row.matchStatus === 'standard'" type="primary" size="small">基础库</el-tag>
                <el-tag v-else type="info" size="small">待询价</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注">
              <template #default="{ row }">
                <el-input v-model="row.remark" placeholder="备注" :disabled="!canEdit" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" v-if="canEdit">
              <template #default="{ row, $index }">
                <el-button type="primary" link @click="openCompareDialog(row)">比价</el-button>
                <el-button type="danger" link @click="removeItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" link @click="addItem" :disabled="!canEdit" style="margin-top: 10px">+添加物资</el-button>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="summary-card">
          <template #header><span>汇总信息</span></template>
          <div class="summary-item">
            <span class="label">物资行数</span>
            <span class="value">{{ itemList.length }}</span>
          </div>
          <div class="summary-item">
            <span class="label">预算总额</span>
            <span class="value primary">{{ formatAmount(totalBudget) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">异常行数</span>
            <span class="value danger">{{ errorCount }}</span>
          </div>
        </el-card>

        <el-card class="validation-card" style="margin-top: 20px">
          <template #header><span>校验提示</span></template>
          <div class="validation-list">
            <div v-for="(v, i) in validationErrors" :key="i" class="validation-item" :class="v.type">
              <el-icon><Close v-if="v.type === 'error'" /><WarningFilled v-else /></el-icon>
              <span>{{ v.message }}</span>
            </div>
            <el-empty v-if="validationErrors.length === 0" description="校验通过" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showBatchDialog" title="批量写入物资" width="600px">
      <el-input v-model="batchText" type="textarea" :rows="10" placeholder="格式：物资名称,规格型号,品牌,单位,计划数量&#10;示例：钢材,Φ12,XX品牌,吨,100" />
      <template #footer>
        <el-button @click="showBatchDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBatchImport">解析并导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showImportDialog" title="从合同附件导入" width="800px">
      <div v-loading="importing" class="import-result">
        <el-alert type="info" title="解析成功，共识别X条物资" show-icon :closable="false" />
        <el-table :data="importItems" stripe max-height="400">
          <el-table-column prop="materialName" label="物资名称" />
          <el-table-column prop="specModel" label="规格型号" />
          <el-table-column prop="brand" label="品牌" />
          <el-table-column prop="quantity" label="数量" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status === 'success' ? 'success' : 'warning'" size="small">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" @click="handleImportConfirm">导入物资</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showExcelReportDialog" title="Excel导入校验报告" width="980px">
      <el-alert
        :type="excelInvalidCount > 0 ? 'warning' : 'success'"
        :title="`共 ${excelEditableRows.length} 行：通过 ${excelValidCount} 行，异常 ${excelInvalidCount} 行`"
        show-icon
        :closable="false"
        style="margin-bottom: 10px"
      />
      <div style="display: flex; justify-content: flex-end; margin-bottom: 8px; gap: 8px">
        <el-button size="small" @click="handleAddExcelRow">新增一行</el-button>
        <el-button size="small" @click="handleRevalidateExcelRows">重新校验</el-button>
        <el-button size="small" @click="handleSelectAllValidRows">全选可导入行</el-button>
        <el-button size="small" @click="handleToggleSelectValidRows">反选可导入行</el-button>
        <el-button size="small" :disabled="excelInvalidCount === 0" @click="handleExportErrorCsv">导出错误清单CSV</el-button>
      </div>
      <el-table
        ref="excelReportTableRef"
        :data="excelEditableRows"
        stripe
        max-height="420"
        :row-class-name="excelReportRowClassName"
        @selection-change="handleExcelSelectionChange"
      >
        <el-table-column type="selection" width="50" :selectable="excelRowSelectable" />
        <el-table-column prop="rowNo" label="行号" width="70" />
        <el-table-column prop="materialName" label="物资名称" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.materialName" size="small" :class="{ 'excel-cell-error': excelCellHasIssue(row, '物资名称') }" @change="handleExcelRowChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="specModel" label="规格型号" min-width="120">
          <template #default="{ row }">
            <el-input v-model="row.specModel" size="small" @change="handleExcelRowChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="brand" label="品牌" min-width="100">
          <template #default="{ row }">
            <el-input v-model="row.brand" size="small" @change="handleExcelRowChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="90">
          <template #default="{ row }">
            <el-input v-model="row.unit" size="small" @change="handleExcelRowChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="rawQuantity" label="数量" width="120">
          <template #default="{ row }">
            <el-input v-model="row.rawQuantity" size="small" :class="{ 'excel-cell-error': excelCellHasIssue(row, '数量') }" @change="handleExcelRowChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="rawBudgetPrice" label="预算单价" width="120">
          <template #default="{ row }">
            <el-input v-model="row.rawBudgetPrice" size="small" :class="{ 'excel-cell-error': excelCellHasIssue(row, '预算单价') }" @change="handleExcelRowChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="校验结果" min-width="200">
          <template #default="{ row }">
            <el-tag v-if="row.valid" type="success" size="small">通过</el-tag>
            <el-tag v-else type="danger" size="small">{{ (row.issues || []).join('；') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button type="danger" link @click="handleRemoveExcelRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showExcelReportDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!excelSelectedValidRows.length" @click="handleImportSelectedExcelRows">
          导入选中行（{{ excelSelectedValidRows.length }}）
        </el-button>
        <el-button type="primary" plain :disabled="!excelValidRows.length" @click="handleImportExcelValidRows">
          导入全部通过行（{{ excelValidRows.length }}）
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCompareDialog" title="自动比价分析" width="800px">
      <div class="compare-header">
        <span>物资：{{ compareItem?.materialName }}</span>
        <el-radio-group v-model="compareSortBy" size="small">
          <el-radio-button label="price">最低含税价</el-radio-button>
          <el-radio-button label="speed">最快到货</el-radio-button>
          <el-radio-button label="score">综合性价比</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="compareList" stripe>
        <el-table-column prop="supplierName" label="供应商" min-width="150" />
        <el-table-column prop="priceWithoutTax" label="不含税单价" width="110" align="right">
          <template #default="{ row }">{{ formatAmount(row.priceWithoutTax) }}</template>
        </el-table-column>
        <el-table-column prop="priceWithTax" label="含税单价" width="110" align="right">
          <template #default="{ row }">{{ formatAmount(row.priceWithTax) }}</template>
        </el-table-column>
        <el-table-column prop="supplyDays" label="备货周期(天)" width="100" align="center" />
        <el-table-column prop="deliveryDate" label="预计到货" width="120" />
        <el-table-column prop="score" label="综合评分" width="100" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.score" disabled text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default>
            <el-button type="primary" link>选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showCompareDialog = false">关闭</el-button>
        <el-button type="primary" @click="exportCompareReport">导出比价报告</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const router = useRouter()
const route = useRoute()
const id = computed(() => Number(route.query.id) || 0)
const isEdit = computed(() => id.value > 0)

const canEdit = computed(() => !isEdit.value || form.status === 1)
const canSubmit = computed(() => canEdit.value && form.projectId > 0 && form.contractId > 0 && itemList.value.length > 0)

const form = reactive({ id: 0, projectId: 0, contractId: 0, status: 1, sourceType: 1 })
const projectList = ref<any[]>([])
const contractList = ref<any[]>([])
const itemList = ref<any[]>([])
const showBatchDialog = ref(false)
const showImportDialog = ref(false)
const importing = ref(false)
const batchText = ref('')
const importItems = ref<any[]>([])
const excelImportRef = ref<HTMLInputElement>()
const showExcelReportDialog = ref(false)
const excelReportRows = ref<any[]>([])
const excelEditableRows = ref<any[]>([])
const excelReportTableRef = ref()
const excelValidRows = ref<any[]>([])
const excelSelectedRows = ref<any[]>([])
const excelSelectedValidRows = computed(() => excelSelectedRows.value.filter((x: any) => !!x?.valid).map((x: any) => normalizeItem(x)))
const excelValidCount = ref(0)
const excelInvalidCount = ref(0)
const validationErrors = ref<any[]>([])
const showCompareDialog = ref(false)
const compareItem = ref<any>(null)
const compareSortBy = ref('price')
const compareList = ref([
  { supplierName: 'XX建材有限公司', priceWithoutTax: 3800, priceWithTax: 4294, supplyDays: 3, deliveryDate: '2026-04-20', score: 5 },
  { supplierName: 'XX钢材贸易', priceWithoutTax: 3950, priceWithTax: 4463.5, supplyDays: 5, deliveryDate: '2026-04-22', score: 4 },
  { supplierName: 'XX物资公司', priceWithoutTax: 4100, priceWithTax: 4633, supplyDays: 2, deliveryDate: '2026-04-19', score: 4 }
])

const totalBudget = computed(() => itemList.value.reduce((sum: number, item: any) => sum + (item.quantity || 0) * (item.budgetPrice || 0), 0))
const errorCount = computed(() => validationErrors.value.filter((v: any) => v.type === 'error').length)

const getStatusType = (status: number) => ['', 'info', 'warning', 'success'][status] || 'info'
const getStatusText = (status: number) => ['', '草稿', '待审批', '已审批'][status] || ''
const formatAmount = (amount: number) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)

const getSummary = ({ columns, data }: any) => {
  const sums: any[] = []
  columns.forEach((col: any, i: number) => {
    if (i === 0) sums.push('合计')
    else if (i === 6) sums.push(itemList.value.length)
    else if (i === 7) sums.push(formatAmount(totalBudget.value))
    else sums.push('')
  })
  return sums
}

const validate = () => {
  validationErrors.value = []
  itemList.value.forEach((item: any, i: number) => {
    if (!item.materialName) validationErrors.value.push({ type: 'error', message: `第${i + 1}行：物资名称不能为空` })
    if (!item.quantity || item.quantity <= 0) validationErrors.value.push({ type: 'error', message: `第${i + 1}行：计划数量必须大于0` })
    if (item.requireBrand && !item.brand) validationErrors.value.push({ type: 'warning', message: `第${i + 1}行：设备类物资品牌必填` })
    if (!item.budgetPrice) validationErrors.value.push({ type: 'info', message: `第${i + 1}行：无基准价` })
  })
  return validationErrors.value.filter((v: any) => v.type === 'error').length === 0
}

const handleBack = () => router.back()
const handleContractChange = () => {}

const addItem = () => itemList.value.push({ materialName: '', specModel: '', brand: '', unit: '', quantity: 0, budgetPrice: 0, matchStatus: '' })
const removeItem = (idx: number) => itemList.value.splice(idx, 1)
const handleReverseOrder = () => itemList.value.reverse()
const normalizeItem = (row: any) => ({
  materialName: row?.materialName || '',
  specModel: row?.specModel || '',
  brand: row?.brand || '',
  unit: row?.unit || '',
  quantity: Number(row?.quantity ?? row?.planQuantity ?? 0),
  budgetPrice: Number(row?.budgetPrice ?? 0),
  remark: row?.remark || '',
  matchStatus: row?.matchStatus || ''
})

const excelReportRowClassName = ({ row }: any) => (row?.valid ? '' : 'excel-report-row-error')
const excelRowSelectable = (row: any) => !!row?.valid
const excelCellHasIssue = (row: any, field: string) => {
  const issues = Array.isArray(row?.issues) ? row.issues : []
  if (!issues.length) return false
  if (field === '物资名称') return issues.some((x: string) => x.includes('物资名称'))
  if (field === '数量') return issues.some((x: string) => x.includes('数量'))
  if (field === '预算单价') return issues.some((x: string) => x.includes('预算单价') || x.includes('单价'))
  return false
}

const parseDecimalStrict = (raw: any): number | null => {
  const txt = String(raw ?? '').trim()
  if (!txt) return null
  if (!/^-?\d+(\.\d+)?$/.test(txt)) return null
  const n = Number(txt)
  if (Number.isNaN(n)) return null
  return n
}

const validateExcelRow = (row: any, idx: number) => {
  const issues: string[] = []
  const materialName = String(row.materialName || '').trim()
  const q = parseDecimalStrict(row.rawQuantity)
  const p = parseDecimalStrict(row.rawBudgetPrice)
  if (!materialName) issues.push('物资名称不能为空')
  if (q == null) issues.push('数量格式错误')
  else if (q <= 0) issues.push('数量必须大于0')
  if (String(row.rawBudgetPrice || '').trim() && p == null) issues.push('预算单价格式错误')
  else if (p != null && p < 0) issues.push('预算单价不能为负数')
  row.rowNo = idx + 1
  row.issues = issues
  row.valid = issues.length === 0
  row.quantity = q ?? 0
  row.budgetPrice = p ?? 0
}

const rebuildExcelValidationResult = () => {
  const selectedKeys = new Set(
    excelSelectedRows.value.map((r: any) => `${String(r?.rowNo)}|${String(r?.materialName || '')}|${String(r?.rawQuantity || '')}|${String(r?.rawBudgetPrice || '')}`)
  )
  excelEditableRows.value.forEach((row, idx) => validateExcelRow(row, idx))
  excelReportRows.value = excelEditableRows.value
  excelValidRows.value = excelEditableRows.value.filter((r: any) => r.valid).map((r: any) => normalizeItem(r))
  excelValidCount.value = excelValidRows.value.length
  excelInvalidCount.value = excelEditableRows.value.length - excelValidCount.value
  excelSelectedRows.value = excelEditableRows.value.filter((r: any) =>
    r.valid && selectedKeys.has(`${String(r?.rowNo)}|${String(r?.materialName || '')}|${String(r?.rawQuantity || '')}|${String(r?.rawBudgetPrice || '')}`)
  )
  // re-apply table selection after data refresh
  setTimeout(() => {
    if (!excelReportTableRef.value) return
    excelReportTableRef.value.clearSelection()
    for (const row of excelSelectedRows.value) {
      excelReportTableRef.value.toggleRowSelection(row, true)
    }
  }, 0)
}

const handleExcelRowChange = () => {
  rebuildExcelValidationResult()
}

const handleAddExcelRow = () => {
  excelEditableRows.value.push({
    rowNo: excelEditableRows.value.length + 1,
    materialName: '',
    specModel: '',
    brand: '',
    unit: '',
    rawQuantity: '',
    rawBudgetPrice: '',
    quantity: 0,
    budgetPrice: 0,
    valid: false,
    issues: ['物资名称不能为空', '数量格式错误']
  })
  rebuildExcelValidationResult()
}

const handleRemoveExcelRow = (idx: number) => {
  excelEditableRows.value.splice(idx, 1)
  rebuildExcelValidationResult()
}

const handleRevalidateExcelRows = () => {
  rebuildExcelValidationResult()
  if (excelInvalidCount.value > 0) {
    ElMessage.warning(`仍有 ${excelInvalidCount.value} 行异常，请修正后再保存`)
    return
  }
  ElMessage.success('校验通过')
}

const handleExcelSelectionChange = (rows: any[]) => {
  excelSelectedRows.value = rows || []
}

const handleSelectAllValidRows = () => {
  if (!excelReportTableRef.value) return
  excelReportTableRef.value.clearSelection()
  for (const row of excelEditableRows.value) {
    if (row?.valid) excelReportTableRef.value.toggleRowSelection(row, true)
  }
}

const handleToggleSelectValidRows = () => {
  if (!excelReportTableRef.value) return
  const selectedSet = new Set(excelSelectedRows.value)
  for (const row of excelEditableRows.value) {
    if (!row?.valid) continue
    excelReportTableRef.value.toggleRowSelection(row, !selectedSet.has(row))
  }
}

const escapeCsvCell = (value: any) => {
  const s = String(value ?? '')
  if (s.includes('"') || s.includes(',') || s.includes('\n')) {
    return `"${s.replace(/"/g, '""')}"`
  }
  return s
}

const handleExportErrorCsv = () => {
  const invalidRows = excelEditableRows.value.filter((r: any) => !r?.valid)
  if (!invalidRows.length) {
    ElMessage.info('当前没有异常行可导出')
    return
  }
  const header = ['行号', '物资名称', '规格型号', '品牌', '单位', '原始数量', '原始预算单价', '错误原因']
  const lines = [header.map(escapeCsvCell).join(',')]
  for (const row of invalidRows) {
    lines.push([
      row.rowNo,
      row.materialName,
      row.specModel,
      row.brand,
      row.unit,
      row.rawQuantity,
      row.rawBudgetPrice,
      Array.isArray(row.issues) ? row.issues.join('；') : ''
    ].map(escapeCsvCell).join(','))
  }
  const csv = '\uFEFF' + lines.join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const href = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = href
  a.download = `purchase-import-errors-${Date.now()}.csv`
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(href)
  ElMessage.success(`已导出 ${invalidRows.length} 条异常记录`)
}

const handleBatchImport = () => {
  if (!batchText.value) return
  const lines = batchText.value.trim().split('\n')
  lines.forEach(line => {
    const parts = line.split(',')
    if (parts[0]) {
      itemList.value.push({ materialName: parts[0] || '', specModel: parts[1] || '', brand: parts[2] || '', unit: parts[3] || '', quantity: parseFloat(parts[4]) || 0, budgetPrice: 0 })
    }
  })
  showBatchDialog.value = false
  ElMessage.success('批量导入成功')
}

const handleImportContract = async () => {
  if (!form.contractId) { ElMessage.warning('请先选择合同'); return }
  importing.value = true
  showImportDialog.value = true
  try {
    importItems.value = [{ materialName: '测试物资', specModel: 'XX', brand: 'XX', quantity: 100, status: 'success', statusText: '成功' }]
  } catch (e: any) { ElMessage.error(e.message) }
  finally { importing.value = false }
}

const handleImportConfirm = () => {
  itemList.value.push(...importItems.value.map((x: any) => normalizeItem(x)))
  showImportDialog.value = false
  ElMessage.success('导入成功')
}

const triggerExcelImport = () => {
  if (!canEdit.value) return
  excelImportRef.value?.click()
}

const downloadExcelTemplate = async () => {
  try {
    const url = api.purchase.importTemplateQuery()
    const token = (localStorage.getItem('token') || '').trim().replace(/^"+|"+$/g, '').replace(/^Bearer\s+/i, '')
    const res = await fetch(url, {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined
    })
    if (!res.ok) throw new Error('下载失败')
    const blob = await res.blob()
    const href = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = href
    a.download = 'purchase-import-template.csv'
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(href)
  } catch (err: any) {
    ElMessage.error(err?.message || '模板下载失败')
  }
}

const handleExcelImport = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res: any = await api.purchase.importExcel(fd)
    if (res.code !== 200) {
      ElMessage.error(res.message || 'Excel导入失败')
      return
    }
    const rows = (res.data?.rows || []).map((x: any) => normalizeItem(x))
    const reportRows = (res.data?.reportRows || []).map((x: any) => ({
      ...x,
      issues: Array.isArray(x?.issues) ? x.issues : []
    }))
    excelEditableRows.value = reportRows
    excelSelectedRows.value = []
    rebuildExcelValidationResult()
    if (!rows.length) {
      showExcelReportDialog.value = reportRows.length > 0
      ElMessage.warning('没有可导入的有效行，请根据校验报告修正后重试')
      return
    }
    if (excelInvalidCount.value > 0) {
      showExcelReportDialog.value = true
      ElMessage.warning(`检测到 ${excelInvalidCount.value} 行异常，请先查看校验报告`)
      return
    }
    itemList.value.push(...rows)
    ElMessage.success(`Excel导入成功，新增 ${rows.length} 条物资`)
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || err?.message || 'Excel导入失败')
  } finally {
    target.value = ''
  }
}

const handleImportExcelValidRows = () => {
  rebuildExcelValidationResult()
  if (excelInvalidCount.value > 0) {
    ElMessage.warning(`存在 ${excelInvalidCount.value} 行异常，请修正后再保存导入`)
    return
  }
  if (!excelValidRows.value.length) {
    ElMessage.warning('没有可导入的有效行')
    return
  }
  itemList.value.push(...excelValidRows.value.map((x: any) => normalizeItem(x)))
  showExcelReportDialog.value = false
  excelSelectedRows.value = []
  ElMessage.success(`已导入 ${excelValidRows.value.length} 条通过校验的数据`)
}

const handleImportSelectedExcelRows = () => {
  rebuildExcelValidationResult()
  if (!excelSelectedValidRows.value.length) {
    ElMessage.warning('请先勾选通过校验的行')
    return
  }
  itemList.value.push(...excelSelectedValidRows.value.map((x: any) => normalizeItem(x)))
  showExcelReportDialog.value = false
  excelSelectedRows.value = []
  ElMessage.success(`已导入 ${excelSelectedValidRows.value.length} 条选中数据`)
}

const openCompareDialog = (row: any) => {
  compareItem.value = row
  showCompareDialog.value = true
}

const exportCompareReport = () => {
  ElMessage.success('比价报告导出成功')
}

const handleSaveDraft = async () => {
  if (!form.projectId || !form.contractId) { ElMessage.warning('请选择项目和合同'); return }
  try {
    const data = { ...form, status: 1, items: itemList.value }
    if (isEdit.value) {
      await api.purchase.update(data)
    } else {
      await api.purchase.create(data)
    }
    ElMessage.success('保存草稿成功')
    router.back()
  } catch (e: any) { ElMessage.error(e.message) }
}

const handleSubmit = async () => {
  if (!validate()) { ElMessage.warning('请修正校验错误'); return }
  try {
    if (isEdit.value && form.id) {
      await api.purchase.update({ ...form, status: 2, items: itemList.value })
      await api.purchase.submit(form.id)
    } else {
      const createRes: any = await api.purchase.create({ ...form, status: 2, items: itemList.value })
      const newId = Number(createRes?.data?.id)
      if (Number.isFinite(newId) && newId > 0) {
        await api.purchase.submit(newId)
      }
    }
    ElMessage.success('提交成功')
    router.back()
  } catch (e: any) { ElMessage.error(e.message) }
}

onMounted(async () => {
  if (!id.value) return
  try {
    const res: any = await api.purchase.get(id.value)
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data)
      form.id = id.value
      itemList.value = (res.data.items || []).map((x: any) => normalizeItem(x))
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载采购清单失败')
  }
})
</script>

<style scoped>
.purchase-edit { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; }
.source-tag { margin-top: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
.summary-card .summary-item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.summary-card .label { color: #909399; }
.summary-card .value { font-weight: 600; font-size: 16px; }
.summary-card .value.primary { color: #409eff; }
.summary-card .value.danger { color: #f56c6c; }
.validation-card .validation-item { display: flex; align-items: center; gap: 8px; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.validation-card .validation-item.error { color: #f56c6c; }
.validation-card .validation-item.warning { color: #e6a23c; }
.validation-card .validation-item.info { color: #909399; }
.import-result { padding: 10px 0; }
:deep(.excel-report-row-error) {
  --el-table-tr-bg-color: #fff2f0;
}
:deep(.excel-cell-error .el-input__wrapper) {
  box-shadow: 0 0 0 1px #f56c6c inset !important;
}
</style>