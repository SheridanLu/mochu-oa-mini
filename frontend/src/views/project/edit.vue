<template>
  <div class="project-edit-container">
    <div class="page-header">
      <div class="header-left">
        <h2>{{ isEdit ? '编辑项目' : (query.type === 'virtual' ? '虚拟项目立项' : '实体项目立项') }}</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>项目管理</el-breadcrumb-item>
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
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目类型" prop="projectType">
              <el-radio-group v-model="form.projectType" :disabled="isEdit">
                <el-radio :value="1">虚拟项目</el-radio>
                <el-radio :value="2">实体项目</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目编号">
              <el-input v-model="form.projectNo" disabled placeholder="保存后自动生成" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="form.projectType === 1 ? '虚拟合同名称' : '项目名称'" prop="projectName">
              <el-input v-model="form.projectName" :placeholder="form.projectType === 1 ? '请输入虚拟合同名称' : '请输入项目名称'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目别名">
              <el-input v-model="form.projectAlias" placeholder="请输入项目别名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目地点" prop="province">
              <el-cascader v-model="form.location" :options="regionOptions" placeholder="请选择省/市" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="详细地址">
              <el-input v-model="form.detailedAddress" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">合同信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="合同含税金额" prop="contractAmount">
              <el-input-number v-model="form.contractAmount" :min="0" :precision="2" :step="10000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="不含税金额">
              <el-input-number v-model="form.amountWithoutTax" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="税率">
              <el-select v-model="form.taxRate" placeholder="请选择税率" style="width: 100%">
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

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="甲方信息" prop="ownerName">
              <el-input v-model="form.ownerName" placeholder="请输入甲方/业主名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="甲方联系人">
              <el-input v-model="form.ownerContact" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="甲方联系电话">
              <el-input v-model="form.ownerPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" placeholder="请选择计划开工日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" placeholder="请选择计划完工日期" style="width: 100%" value-format="YYYY-MM-DD" :disabled="!form.startDate" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">项目经理</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目经理" prop="projectManagerId">
              <el-select v-model="form.projectManagerId" placeholder="请选择项目经理" style="width: 100%" filterable>
                <el-option v-for="user in userList" :key="user.id" :label="user.realName" :value="user.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门">
              <el-select v-model="form.departmentId" placeholder="请选择部门" style="width: 100%">
                <el-option v-for="dept in deptList" :key="dept.id" :label="dept.deptName" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">其他信息</el-divider>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>

        <el-form-item label="附件">
          <el-upload action="/api/common/upload" list-type="file" :limit="5" :on-success="handleUploadSuccess">
            <el-button type="primary">上传附件</el-button>
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
import { request } from '@/utils/request'
import { api } from '@/api'

const route = useRoute()
const router = useRouter()

const query = route.query
const isEdit = computed(() => !!query.id)

const formRef = ref()
const form = reactive({
  projectType: query.type === 'virtual' ? 1 : 2,
  projectNo: '',
  projectName: '',
  projectAlias: '',
  location: [] as string[],
  province: '',
  city: '',
  detailedAddress: '',
  contractAmount: 0,
  amountWithoutTax: 0,
  taxRate: 0.06,
  ownerName: '',
  ownerContact: '',
  ownerPhone: '',
  startDate: '',
  endDate: '',
  projectManagerId: null as number | null,
  projectManagerName: '',
  departmentId: null as number | null,
  departmentName: '',
  remark: '',
  attachments: [] as string[]
})

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  province: [{ required: true, message: '请选择项目地点', trigger: 'change' }],
  contractAmount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
  ownerName: [{ required: true, message: '请输入甲方信息', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择计划开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择计划结束日期', trigger: 'change' }],
  projectManagerId: [{ required: true, message: '请选择项目经理', trigger: 'change' }]
}

const userList = ref<any[]>([
  { id: 1, realName: '张三' },
  { id: 2, realName: '李四' },
  { id: 3, realName: '王五' }
])

const deptList = ref<any[]>([
  { id: 1, deptName: '基础业务部' },
  { id: 2, deptName: '软件业务部' }
])

const regionOptions = [
  { value: '北京市', label: '北京市', children: [{ value: '北京市', label: '北京市' }] },
  { value: '上海市', label: '上海市', children: [{ value: '上海市', label: '上海市' }] },
  { value: '广东省', label: '广东省', children: [{ value: '广州市', label: '广州市' }, { value: '深圳市', label: '深圳市' }] },
  { value: '浙江省', label: '浙江省', children: [{ value: '杭州市', label: '杭州市' }, { value: '宁波市', label: '宁波市' }] }
]

const loadData = async () => {
  if (query.id) {
    try {
      const res = await request<{ data: any }>({ url: `/project/${query.id}`, method: 'GET' })
      if (res.data) {
        Object.assign(form, res.data)
      }
    } catch (e) {
      console.error('加载项目失败', e)
    }
  }
}

onMounted(() => {
  loadData()
})

const handleBack = () => router.push('/project')

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    form.province = form.location[0] || ''
    form.city = form.location[1] || ''
    
    if (isEdit.value) {
      await api.project.update(form)
    } else {
      await api.project.create(form)
    }
    ElMessage.success('保存成功')
    router.push('/project')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleUploadSuccess = (res: any) => {
  if (res.code === 200) {
    form.attachments.push(res.data)
  }
}
</script>

<style scoped>
.project-edit-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.form-card { background: #fff; border-radius: 8px; }
</style>