<template>
  <div class="material-edit-container">
    <div class="page-header">
      <div class="header-left">
        <h2>{{ isEdit ? '编辑物资' : '新增物资' }}</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>物资管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEdit ? '编辑' : '新增' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleBack">返回</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </div>
    </div>

    <el-card class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="物资名称" prop="materialName">
              <el-input v-model="form.materialName" placeholder="请输入物资名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规格型号">
              <el-input v-model="form.specModel" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="如：吨、平方米" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="物资分类">
              <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
                <el-option label="钢材" value="钢材" />
                <el-option label="水泥" value="水泥" />
                <el-option label="木材" value="木材" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="仓库">
              <el-select v-model="form.warehouseId" placeholder="请选择" style="width: 100%">
                <el-option label="主仓库" :value="1" />
                <el-option label="辅料仓库" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单价">
              <el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="安全库存">
              <el-input-number v-model="form.safeStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.query.id)
const formRef = ref()

const form = reactive({
  materialName: '',
  specModel: '',
  unit: '',
  category: '',
  warehouseId: null,
  unitPrice: 0,
  safeStock: 0,
  remark: ''
})

const rules = {
  materialName: [{ required: true, message: '请输入物资名称', trigger: 'blur' }]
}

const handleBack = () => router.push('/material')

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    const payload: any = { ...form }
    if (isEdit.value) {
      payload.id = Number(route.query.id)
      await api.material.update(payload)
    } else {
      await api.material.create(payload)
    }
    ElMessage.success('保存成功')
    router.push('/material')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

const loadData = async () => {
  if (!isEdit.value) return
  try {
    const id = Number(route.query.id)
    if (!Number.isFinite(id) || id <= 0) return
    const res: any = await api.material.get(id)
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data)
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载物资失败')
  }
}

loadData()
</script>

<style scoped>
.material-edit-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; }
.form-card { background: #fff; border-radius: 8px; }
</style>