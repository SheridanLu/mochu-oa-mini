<template>
  <div class="material-container">
    <div class="page-header">
      <div class="header-left">
        <h2>物资管理</h2>
        <el-breadcrumb separator="/"><el-breadcrumb-item>物资管理</el-breadcrumb-item></el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">新增物资</el-button>
      </div>
    </div>
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="物资分类">
          <el-select placeholder="请选择" style="width: 140px">
            <el-option label="钢材" value="1" />
            <el-option label="水泥" value="2" />
            <el-option label="木材" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select placeholder="请选择" style="width: 140px">
            <el-option label="主仓库" value="1" />
            <el-option label="辅料仓库" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary">搜索</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="物资库存" name="stock">
          <el-table :data="stockData" stripe>
            <el-table-column prop="materialName" label="物资名称" min-width="150" />
            <el-table-column prop="specModel" label="规格型号" width="120" />
            <el-table-column prop="warehouseName" label="仓库" width="100" />
            <el-table-column prop="quantity" label="库存数量" width="100" align="right" />
            <el-table-column prop="availableQuantity" label="可用数量" width="100" align="right" />
            <el-table-column prop="unitPrice" label="单价" width="100" align="right">
              <template #default="{ row }">{{ formatAmount(row.unitPrice) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default>
                <el-button type="primary" link @click="handleOutbound">出库</el-button>
                <el-button type="primary" link>入库</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="入库记录" name="entry">
          <el-table :data="entryData" stripe>
            <el-table-column prop="entryNo" label="入库单号" width="140" />
            <el-table-column prop="projectName" label="项目" min-width="120" />
            <el-table-column prop="entryType" label="入库类型" width="100" align="center">
              <template #default="{ row }">{{ ['采购入库','退货入库','调拨入库'][row.entryType-1] }}</template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="入库金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column prop="entryDate" label="入库日期" width="110" />
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 4 ? 'success' : 'warning'">{{ ['','草稿','待审批','审批中','已完成'][row.status] }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="出库记录" name="out">
          <el-table :data="outData" stripe>
            <el-table-column prop="outNo" label="出库单号" width="140" />
            <el-table-column prop="projectName" label="项目" min-width="120" />
            <el-table-column prop="contractNo" label="合同编号" width="130" />
            <el-table-column prop="entryNo" label="入库单号" width="130" />
            <el-table-column prop="outType" label="出库类型" width="100" align="center">
              <template #default="{ row }">{{ ['施工领用','跨项目调拨','退货'][row.outType-1] }}</template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="出库金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column prop="outDate" label="出库日期" width="110" />
            <el-table-column prop="receiverName" label="领料人" width="100" />
            <el-table-column label="操作" width="80">
              <template #default>
                <el-button type="primary" link>查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="outboundDialogVisible" title="物资出库" width="600px" :close-on-click-modal="false">
      <el-form ref="outboundFormRef" :model="outboundForm" :rules="outboundRules" label-width="120px">
        <el-form-item label="出库类型" required>
          <el-radio-group v-model="outboundForm.outType">
            <el-radio :value="1">施工领用</el-radio>
            <el-radio :value="2">跨项目调拨</el-radio>
            <el-radio :value="3">退货</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联项目" required>
          <el-select v-model="outboundForm.projectId" placeholder="请选择项目" style="width: 100%">
            <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目合同编号" prop="contractNo">
          <el-input v-model="outboundForm.contractNo" placeholder="请输入关联合同编号" />
        </el-form-item>
        <el-form-item label="入库单号" prop="entryNo">
          <el-select v-model="outboundForm.entryNo" placeholder="请选择入库单号" style="width: 100%" clearable>
            <el-option v-for="e in entryList" :key="e.id" :label="e.entryNo" :value="e.entryNo" />
          </el-select>
        </el-form-item>
        <el-divider>出库物资明细</el-divider>
        <el-table :data="outboundForm.items" stripe>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="materialName" label="物资名称" />
          <el-table-column prop="specModel" label="规格型号" />
          <el-table-column prop="quantity" label="出库数量">
            <template #default="{ row, $index }">
              <el-input-number v-model="row.quantity" :min="0" :max="row.maxQuantity" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template #default="{ $index }">
              <el-button type="danger" link @click="outboundForm.items.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" link @click="addOutboundItem" style="margin-top: 10px">+添加物资</el-button>
      </el-form>
      <template #footer>
        <el-button @click="outboundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleOutboundSubmit">确认出库</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="transferBlockVisible" title="跨项目调拨拦截" width="450px" :close-on-click-modal="false">
      <div class="transfer-block">
        <el-icon color="#f56c6c" :size="48"><WarningFilled /></el-icon>
        <p>当前操作涉及物资跨项目调拨，未完成调拨单审批流程，禁止出库</p>
      </div>
      <template #footer>
        <el-button @click="transferBlockVisible = false">取消</el-button>
        <el-button type="primary" @click="goToTransfer">去创建调拨单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'

const activeTab = ref('stock')
const outboundDialogVisible = ref(false)
const transferBlockVisible = ref(false)
const outboundFormRef = ref<FormInstance>()

const stockData = ref([
  { id: 1, materialName: '螺纹钢', specModel: 'HRB400φ12', warehouseName: '主仓库', quantity: 5000, availableQuantity: 4500, unitPrice: 4500 },
  { id: 2, materialName: '水泥', specModel: 'P.O42.5散装', warehouseName: '主仓库', quantity: 200, availableQuantity: 180, unitPrice: 380 }
])
const entryData = ref([{ id: 1, entryNo: 'RK202604001', projectName: 'XX项目', entryType: 1, totalAmount: 50000, entryDate: '2026-04-10', status: 4 }])
const outData = ref([
  { id: 1, outNo: 'CK202604001', projectName: 'XX项目', contractNo: 'HT2026001', entryNo: 'RK202604001', outType: 1, totalAmount: 20000, outDate: '2026-04-12', receiverName: '张三' }
])

const projectList = ref([{ id: 1, name: 'XX项目一' }, { id: 2, name: 'XX项目二' }])
const entryList = ref([{ id: 1, entryNo: 'RK202604001' }, { id: 2, entryNo: 'RK202604002' }])

const outboundForm = reactive({
  outType: 1,
  projectId: null as number | null,
  contractNo: '',
  entryNo: '',
  items: [{ materialId: 1, materialName: '螺纹钢', specModel: 'HRB400φ12', quantity: 0, maxQuantity: 100 }]
})

const outboundRules: FormRules = {
  contractNo: [{ required: true, message: '现场出库必须关联项目合同编号或入库单号', trigger: 'blur' }],
  entryNo: [{ required: true, message: '现场出库必须关联项目合同编号或入库单号', trigger: 'change' }]
}

const formatAmount = (v: number) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(v)
const handleCreate = () => router.push('/material/create')

const handleOutbound = () => {
  outboundForm.items = [{ materialId: 1, materialName: '螺纹钢', specModel: 'HRB400φ12', quantity: 0, maxQuantity: 100 }]
  outboundDialogVisible.value = true
}

const addOutboundItem = () => {
  outboundForm.items.push({ materialId: 2, materialName: '水泥', specModel: 'P.O42.5', quantity: 0, maxQuantity: 50 })
}

const handleOutboundSubmit = async () => {
  if (!outboundFormRef.value) return
  await outboundFormRef.value.validate((valid) => {
    if (!valid) return
    
    if (outboundForm.outType === 2) {
      transferBlockVisible.value = true
      return
    }
    
    const hasContractNo = outboundForm.contractNo.trim() !== ''
    const hasEntryNo = outboundForm.entryNo.trim() !== ''
    if (!hasContractNo && !hasEntryNo) {
      ElMessage.error('现场出库必须关联项目合同编号或入库单号')
      return
    }
    
    outboundDialogVisible.value = false
    ElMessage.success('出库成功')
  })
}

const goToTransfer = () => { transferBlockVisible.value = false; console.log('跳转调拨单页面') }
</script>

<style scoped>
.material-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; }
.table-card { background: #fff; border-radius: 8px; }
.transfer-block { display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 20px; }
.transfer-block p { font-size: 16px; color: #303133; text-align: center; margin: 0; }
</style>