<template>
  <div class="price-container">
    <div class="page-header">
      <div class="header-left">
        <h2>询价与产品价格双库</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>价格管理</el-breadcrumb-item>
          <el-breadcrumb-item>询价与产品价格双库</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>
    <el-tabs v-model="activeTab" class="price-tabs">
      <el-tab-pane label="询价记录库" name="inquiry">
        <el-card class="filter-card">
          <el-form :inline="true" :model="inquiryFilter">
            <el-form-item label="产品名称">
              <el-input v-model="inquiryFilter.productName" placeholder="请输入产品名称" clearable style="width: 160px" />
            </el-form-item>
            <el-form-item label="供应商">
              <el-input v-model="inquiryFilter.supplierName" placeholder="请输入供应商" clearable style="width: 140px" />
            </el-form-item>
            <el-form-item label="询价日期">
              <el-date-picker v-model="inquiryFilter.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 240px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleInquirySearch">搜索</el-button>
              <el-button @click="handleInquiryReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card class="table-card">
          <template #header>
            <div class="table-header">
              <span>询价记录列表</span>
              <el-button type="primary" size="small" @click="handleAddInquiry">新增询价</el-button>
            </div>
          </template>
          <el-table :data="inquiryList" stripe>
            <el-table-column prop="inquiryNo" label="询价单号" width="140" />
            <el-table-column prop="productName" label="产品名称" min-width="160" />
            <el-table-column prop="specification" label="规格型号" width="120" />
            <el-table-column prop="supplierName" label="供应商" width="150" />
            <el-table-column prop="inquiryPrice" label="询价单价" width="100" align="right">
              <template #default="{ row }">{{ formatAmount(row.inquiryPrice) }}</template>
            </el-table-column>
            <el-table-column prop="inquiryDate" label="询价日期" width="120" />
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'info'">{{ ['','已采纳','待比较','已过期'][row.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default>
                <el-button type="primary" link>查看</el-button>
                <el-button type="primary" link>比价</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="inquiryPagination.page" v-model:page-size="inquiryPagination.size" :total="inquiryPagination.total" layout="total, prev, pager, next" />
          </div>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="基准价库" name="baseline">
        <el-card class="filter-card">
          <el-form :inline="true" :model="baselineFilter">
            <el-form-item label="产品分类">
              <el-select v-model="baselineFilter.categoryId" placeholder="请选择" clearable style="width: 140px">
                <el-option label="材料类" value="1" />
                <el-option label="设备类" value="2" />
                <el-option label="劳务类" value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="产品名称">
              <el-input v-model="baselineFilter.productName" placeholder="请输入产品名称" clearable style="width: 160px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleBaselineSearch">搜索</el-button>
              <el-button @click="handleBaselineReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card class="table-card">
          <template #header>
            <div class="table-header">
              <span>基准价列表</span>
              <el-button type="primary" size="small" @click="handleAddBaseline">新增基准价</el-button>
            </div>
          </template>
          <el-table :data="baselineList" stripe>
            <el-table-column prop="productCode" label="产品编码" width="120" />
            <el-table-column prop="productName" label="产品名称" min-width="160" />
            <el-table-column prop="categoryName" label="产品分类" width="100" />
            <el-table-column prop="specification" label="规格型号" width="120" />
            <el-table-column prop="unit" label="单位" width="60" />
            <el-table-column prop="baselinePrice" label="基准单价" width="100" align="right">
              <template #default="{ row }">{{ formatAmount(row.baselinePrice) }}</template>
            </el-table-column>
            <el-table-column prop="validDate" label="有效期" width="120" />
            <el-table-column prop="source" label="来源" width="100">
              <template #default="{ row }">{{ ['','历史均值','市场询价','招标价'][row.source] }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default>
                <el-button type="primary" link>查看</el-button>
                <el-button type="primary" link>编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="baselinePagination.page" v-model:page-size="baselinePagination.size" :total="baselinePagination.total" layout="total, prev, pager, next" />
          </div>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="基础价库" name="base">
        <el-card class="filter-card">
          <el-form :inline="true" :model="baseFilter">
            <el-form-item label="产品分类">
              <el-select v-model="baseFilter.categoryId" placeholder="请选择" clearable style="width: 140px">
                <el-option label="材料类" value="1" />
                <el-option label="设备类" value="2" />
                <el-option label="劳务类" value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="产品名称">
              <el-input v-model="baseFilter.productName" placeholder="请输入产品名称" clearable style="width: 160px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleBaseSearch">搜索</el-button>
              <el-button @click="handleBaseReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card class="table-card">
          <template #header>
            <div class="table-header">
              <span>基础价列表</span>
              <el-button type="primary" size="small" @click="handleAddBase">新增基础价</el-button>
            </div>
          </template>
          <el-table :data="baseList" stripe>
            <el-table-column prop="productCode" label="产品编码" width="120" />
            <el-table-column prop="productName" label="产品名称" min-width="160" />
            <el-table-column prop="categoryName" label="产品分类" width="100" />
            <el-table-column prop="specification" label="规格型号" width="120" />
            <el-table-column prop="unit" label="单位" width="60" />
            <el-table-column prop="basePrice" label="基础单价" width="100" align="right">
              <template #default="{ row }">{{ formatAmount(row.basePrice) }}</template>
            </el-table-column>
            <el-table-column prop="adjustRate" label="调价系数" width="90" align="center">
              <template #default="{ row }">{{ row.adjustRate }}%</template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="120" />
            <el-table-column label="操作" width="120">
              <template #default>
                <el-button type="primary" link>查看</el-button>
                <el-button type="primary" link>编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="basePagination.page" v-model:page-size="basePagination.size" :total="basePagination.total" layout="total, prev, pager, next" />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="inquiryDialogVisible" title="新增询价" width="700px" :close-on-click-modal="false">
      <el-form ref="inquiryFormRef" :model="inquiryForm" :rules="inquiryRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="inquiryForm.productName" placeholder="请输入产品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号" prop="specification">
              <el-input v-model="inquiryForm.specification" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="inquiryForm.brand" placeholder="请输入品牌" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierName">
              <el-input v-model="inquiryForm.supplierName" placeholder="请输入供应商名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单价" prop="inquiryPrice">
              <el-input-number v-model="inquiryForm.inquiryPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="税率%" prop="taxRate">
              <el-input-number v-model="inquiryForm.taxRate" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备货周期(天)" prop="supplyDays">
              <el-input-number v-model="inquiryForm.supplyDays" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="询价日期" prop="inquiryDate">
              <el-date-picker v-model="inquiryForm.inquiryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计到货">
              <el-date-picker v-model="inquiryForm.deliveryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="inquiryForm.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="inquiryDialogVisible = false">取消</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" @click="handleSubmitInquiry">提交入库</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="批量导入询价" width="600px">
      <div class="import-tip">
        <el-alert type="info" :closable="false">
          <template #title>
            <span>请下载模板，按模板格式填写后上传。支持 .xlsx/.xls 格式</span>
          </template>
        </el-alert>
        <el-button type="primary" link style="margin-top: 10px" @click="downloadTemplate">下载模板</el-button>
      </div>
      <el-upload action="/api/common/upload" :auto-upload="false" :on-change="handleFileChange" :file-list="importFileList" accept=".xlsx,.xls">
        <el-button type="primary">选择文件</el-button>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImportConfirm">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'

const activeTab = ref('inquiry')

const formatAmount = (amount: number) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)

const inquiryDialogVisible = ref(false)
const importDialogVisible = ref(false)
const inquiryFormRef = ref<FormInstance>()
const importFileList = ref<UploadFile[]>([])

const inquiryForm = reactive({
  productName: '',
  specification: '',
  brand: '',
  supplierName: '',
  inquiryPrice: 0,
  taxRate: 13,
  supplyDays: 0,
  inquiryDate: '',
  deliveryDate: '',
  remark: ''
})

const inquiryRules: FormRules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  specification: [{ required: true, message: '请输入规格型号', trigger: 'blur' }],
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  inquiryPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  taxRate: [{ required: true, message: '请输入税率', trigger: 'blur' }],
  supplyDays: [{ required: true, message: '请输入备货周期', trigger: 'blur' }],
  inquiryDate: [{ required: true, message: '请选择询价日期', trigger: 'change' }]
}

const handleAddInquiry = () => {
  inquiryForm.productName = ''
  inquiryForm.specification = ''
  inquiryForm.brand = ''
  inquiryForm.supplierName = ''
  inquiryForm.inquiryPrice = 0
  inquiryForm.taxRate = 13
  inquiryForm.supplyDays = 0
  inquiryForm.inquiryDate = ''
  inquiryForm.deliveryDate = ''
  inquiryForm.remark = ''
  inquiryDialogVisible.value = true
}

const handleSaveDraft = async () => {
  await inquiryFormRef.value?.validateField(['productName', 'supplierName'])
  ElMessage.success('草稿保存成功')
  inquiryDialogVisible.value = false
}

const handleSubmitInquiry = async () => {
  await inquiryFormRef.value?.validate()
  if (inquiryForm.deliveryDate && inquiryForm.inquiryDate && inquiryForm.deliveryDate <= inquiryForm.inquiryDate) {
    ElMessage.warning('预计到货时间必须晚于询价日期')
    return
  }
  ElMessage.success('提交入库成功')
  inquiryDialogVisible.value = false
}

const downloadTemplate = () => {
  ElMessage.info('模板下载功能')
}

const handleFileChange = (file: UploadFile) => {
  importFileList.value = [file]
}

const handleImportConfirm = () => {
  if (importFileList.value.length === 0) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  ElMessage.success('导入成功')
  importDialogVisible.value = false
}

const inquiryFilter = reactive({ productName: '', supplierName: '', dateRange: [] as string[] })
const inquiryList = ref([
  { id: 1, inquiryNo: 'XJ202604001', productName: '螺纹钢 HRB400', specification: 'Φ12', supplierName: 'XX钢材公司', inquiryPrice: 4200, inquiryDate: '2026-04-10', status: 1 },
  { id: 2, inquiryNo: 'XJ202604002', productName: '水泥 P.O42.5', specification: '散装', supplierName: 'XX水泥厂', inquiryPrice: 380, inquiryDate: '2026-04-11', status: 2 },
  { id: 3, inquiryNo: 'XJ202604003', productName: '挖掘机 卡特', specification: '320D', supplierName: 'XX机械设备', inquiryPrice: 180000, inquiryDate: '2026-04-09', status: 1 }
])
const inquiryPagination = reactive({ page: 1, size: 20, total: 45 })
const handleInquirySearch = () => console.log('搜索询价', inquiryFilter)
const handleInquiryReset = () => { inquiryFilter.productName = ''; inquiryFilter.supplierName = ''; inquiryFilter.dateRange = [] }

const baselineFilter = reactive({ categoryId: '', productName: '' })
const baselineList = ref([
  { id: 1, productCode: 'CL001', productName: '螺纹钢 HRB400', categoryName: '材料类', specification: 'Φ12', unit: '吨', baselinePrice: 4150, validDate: '2026-06-30', source: 1 },
  { id: 2, productCode: 'CL002', productName: '水泥 P.O42.5', categoryName: '材料类', specification: '散装', unit: '吨', baselinePrice: 375, validDate: '2026-06-30', source: 2 },
  { id: 3, productCode: 'SB001', productName: '挖掘机 卡特320D', categoryName: '设备类', specification: '320D', unit: '台', baselinePrice: 175000, validDate: '2026-12-31', source: 3 }
])
const baselinePagination = reactive({ page: 1, size: 20, total: 120 })
const handleBaselineSearch = () => console.log('搜索基准价', baselineFilter)
const handleBaselineReset = () => { baselineFilter.categoryId = ''; baselineFilter.productName = '' }
const handleAddBaseline = () => console.log('新增基准价')

const baseFilter = reactive({ categoryId: '', productName: '' })
const baseList = ref([
  { id: 1, productCode: 'CL001', productName: '螺纹钢 HRB400', categoryName: '材料类', specification: 'Φ12', unit: '吨', basePrice: 4000, adjustRate: 5, updateTime: '2026-04-01' },
  { id: 2, productCode: 'CL002', productName: '水泥 P.O42.5', categoryName: '材料类', specification: '散装', unit: '吨', basePrice: 350, adjustRate: 8, updateTime: '2026-04-01' },
  { id: 3, productCode: 'SB001', productName: '挖掘机 卡特320D', categoryName: '设备类', specification: '320D', unit: '台', basePrice: 160000, adjustRate: 10, updateTime: '2026-03-15' }
])
const basePagination = reactive({ page: 1, size: 20, total: 200 })
const handleBaseSearch = () => console.log('搜索基础价', baseFilter)
const handleBaseReset = () => { baseFilter.categoryId = ''; baseFilter.productName = '' }
const handleAddBase = () => console.log('新增基础价')
</script>

<style scoped>
.price-container { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.price-tabs { margin-top: 20px; }
.filter-card { margin-bottom: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { display: flex; justify-content: flex-end; padding: 20px 0; }
.import-tip { margin-bottom: 20px; }
</style>