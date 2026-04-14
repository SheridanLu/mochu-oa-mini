<template>
  <div class="permission-container">
    <div class="page-header">
      <div class="header-left">
        <h2>权限配置 - {{ route.query.roleName }}</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>角色管理</el-breadcrumb-item>
          <el-breadcrumb-item>权限配置</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="handleBack">返回</el-button>
        <el-button type="primary" @click="handleSave">保存权限</el-button>
      </div>
    </div>

    <el-card class="permission-card">
      <div class="permission-tip">
        <el-icon color="#409eff"><InfoFilled /></el-icon>
        <span>请勾选角色需要分配的权限，权限将影响菜单和功能的访问</span>
      </div>
      
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="菜单权限" name="menu">
          <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            :props="treeProps"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedMenuIds"
            @check="handleMenuCheck"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <el-icon v-if="data.icon"><component :is="data.icon" /></el-icon>
                <span>{{ node.label }}</span>
              </span>
            </template>
          </el-tree>
        </el-tab-pane>
        
        <el-tab-pane label="功能权限" name="action">
          <div class="action-permission">
            <el-row :gutter="20">
              <el-col :span="8" v-for="module in actionModules" :key="module.id">
                <el-card class="action-module">
                  <template #header>
                    <div class="module-header">
                      <span>{{ module.name }}</span>
                      <el-checkbox 
                        :model-value="moduleChecked(module)" 
                        :indeterminate="moduleIndeterminate(module)"
                        @change="handleModuleCheck(module, $event)"
                      >全选</el-checkbox>
                    </div>
                  </template>
                  <el-checkbox-group v-model="checkedActions" @change="handleActionChange">
                    <el-checkbox v-for="action in module.actions" :key="action.code" :label="action.code" :value="action.code">
                      {{ action.name }}
                    </el-checkbox>
                  </el-checkbox-group>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="数据权限" name="data">
          <el-form label-width="120px">
            <el-form-item label="数据范围">
              <el-radio-group v-model="dataScope">
                <el-radio :value="1">全部数据</el-radio>
                <el-radio :value="2">部门数据</el-radio>
                <el-radio :value="3">本人数据</el-radio>
                <el-radio :value="4">指定部门数据</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="dataScope === 4" label="指定部门">
              <el-tree
                ref="deptTreeRef"
                :data="deptTree"
                show-checkbox
                node-key="id"
                :props="{ label: 'deptName', children: 'children' }"
                :default-checked-keys="checkedDeptIds"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeTab = ref('menu')
const dataScope = ref(1)
const menuTreeRef = ref()
const deptTreeRef = ref()

const treeProps = { label: 'menuName', children: 'children' }

const menuTree = ref([
  {
    id: 1, menuName: '首页', icon: 'House', children: [
      { id: 101, menuName: '首页数据' }
    ]
  },
  {
    id: 2, menuName: '项目管理', icon: 'FolderOpened', children: [
      { id: 201, menuName: '项目列表' },
      { id: 202, menuName: '项目新增' },
      { id: 203, menuName: '项目编辑' }
    ]
  },
  {
    id: 3, menuName: '合同管理', icon: 'Document', children: [
      { id: 301, menuName: '收入合同' },
      { id: 302, menuName: '支出合同' }
    ]
  },
  {
    id: 4, menuName: '供应商管理', icon: 'Shop', children: [
      { id: 401, menuName: '供应商列表' }
    ]
  },
  {
    id: 5, menuName: '采购管理', icon: 'ShoppingCart', children: [
      { id: 501, menuName: '采购清单' },
      { id: 502, menuName: '采购编辑' }
    ]
  },
  {
    id: 6, menuName: '财务管理', icon: 'Money', children: [
      { id: 601, menuName: '收入对账' },
      { id: 602, menuName: '回款督办' },
      { id: 603, menuName: '付款计划' },
      { id: 604, menuName: '日常报销' },
      { id: 605, menuName: '发票管理' },
      { id: 606, menuName: '成本归集' }
    ]
  },
  {
    id: 7, menuName: '系统设置', icon: 'Setting', children: [
      { id: 701, menuName: '角色管理' },
      { id: 702, menuName: '用户管理' },
      { id: 703, menuName: '部门管理' }
    ]
  }
])

const checkedMenuIds = ref([1, 101, 2, 201, 3, 301, 4, 401, 5, 501, 6, 601, 602, 603, 604, 7, 701])

const actionModules = ref([
  {
    id: 1, name: '项目管理', actions: [
      { code: 'project:add', name: '新增项目' },
      { code: 'project:edit', name: '编辑项目' },
      { code: 'project:delete', name: '删除项目' },
      { code: 'project:export', name: '导出项目' }
    ]
  },
  {
    id: 2, name: '合同管理', actions: [
      { code: 'contract:add', name: '新增合同' },
      { code: 'contract:edit', name: '编辑合同' },
      { code: 'contract:delete', name: '删除合同' },
      { code: 'contract:sign', name: '合同签订' }
    ]
  },
  {
    id: 3, name: '财务管理', actions: [
      { code: 'finance:approve', name: '审批' },
      { code: 'finance:pay', name: '付款' },
      { code: 'finance:refund', name: '退款' },
      { code: 'finance:export', name: '导出财务报表' }
    ]
  }
])

const checkedActions = ref(['project:add', 'project:edit', 'contract:add', 'contract:edit', 'finance:approve'])

const deptTree = ref([
  { id: 1, deptName: '总公司', children: [
    { id: 11, deptName: '基础业务部' },
    { id: 12, deptName: '软件业务部' }
  ]}
])
const checkedDeptIds = ref([11])

const handleMenuCheck = () => { console.log('菜单勾选变化') }
const handleBack = () => { router.push('/system/role') }
const handleSave = () => { 
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
  console.log('保存权限', { menuIds: [...checkedKeys, ...halfCheckedKeys], actions: checkedActions.value, dataScope: dataScope.value })
  ElMessage.success('权限保存成功') 
}

const moduleChecked = (module: any) => module.actions.every((a: any) => checkedActions.value.includes(a.code))
const moduleIndeterminate = (module: any) => {
  const checked = module.actions.filter((a: any) => checkedActions.value.includes(a.code)).length
  return checked > 0 && checked < module.actions.length
}
const handleModuleCheck = (module: any, checked: boolean) => {
  if (checked) {
    checkedActions.value = [...new Set([...checkedActions.value, ...module.actions.map((a: any) => a.code)])]
  } else {
    checkedActions.value = checkedActions.value.filter((c: string) => !module.actions.some((a: any) => a.code === c))
  }
}
const handleActionChange = () => {}
</script>

<style scoped>
.permission-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; font-weight: 600; }
.permission-card { background: #fff; border-radius: 8px; }
.permission-tip { display: flex; align-items: center; gap: 8px; padding: 12px 16px; background: #ecf5ff; border-radius: 4px; margin-bottom: 20px; color: #409eff; }
.custom-tree-node { display: flex; align-items: center; gap: 6px; }
.action-module { margin-bottom: 20px; }
.module-header { display: flex; justify-content: space-between; align-items: center; }
.action-permission .el-checkbox-group { display: flex; flex-direction: column; gap: 10px; }
</style>