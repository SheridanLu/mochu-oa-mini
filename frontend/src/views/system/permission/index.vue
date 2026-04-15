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
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存权限</el-button>
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
            :default-expanded-keys="expandedMenuIds"
            @check="handleMenuCheck"
          >
            <template #default="{ data }">
              <span class="custom-tree-node">
                <el-icon v-if="data.icon"><component :is="data.icon" /></el-icon>
                <span>{{ data.permissionName || data.menuName || data.permissionCode || `权限-${data.id}` }}</span>
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
                    <el-checkbox v-for="action in module.actions" :key="action.code" :label="action.code">
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
                <el-radio :value="2">本部门数据</el-radio>
                <el-radio :value="3">本人数据</el-radio>
                <el-radio :value="4">指定范围数据</el-radio>
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
            <el-form-item label="说明">
              <div class="scope-desc">
                <p v-if="dataScope === 1">全部数据：可查看和管理所有部门的数据</p>
                <p v-else-if="dataScope === 2">本部门数据：只能查看和管理所属部门的数据</p>
                <p v-else-if="dataScope === 3">本人数据：只能查看和管理自己创建的数据</p>
                <p v-else>指定范围数据：可查看和管理指定部门的数据</p>
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

const route = useRoute()
const router = useRouter()

const activeTab = ref('menu')
const dataScope = ref(1)
const menuTreeRef = ref()
const deptTreeRef = ref()
const saveLoading = ref(false)

/** 与后端 SysPermission 一致；兼容历史 mock 的 menuName */
const treeProps = { label: 'permissionName', children: 'children' }

const menuTree = ref<any[]>([])
const checkedMenuIds = ref<number[]>([])
const expandedMenuIds = ref<number[]>([])

const actionModules = ref([
  {
    id: 1, name: '项目管理', actions: [
      { code: 'project:create', name: '发起项目立项' },
      { code: 'project:approve', name: '审批项目立项' },
      { code: 'project:convert', name: '虚拟项目转实体' },
      { code: 'project:terminate', name: '虚拟项目中止' },
      { code: 'project:view-all', name: '查看所有项目' },
      { code: 'project:view-own', name: '查看本人相关项目' }
    ]
  },
  {
    id: 2, name: '合同管理', actions: [
      { code: 'contract:template-manage', name: '合同模板管理' },
      { code: 'contract:sign-income', name: '签订收入合同' },
      { code: 'contract:sign-expense', name: '签订支出合同' },
      { code: 'contract:approve-finance', name: '合同财务审批' },
      { code: 'contract:approve-legal', name: '合同法务审批' },
      { code: 'contract:approve-gm', name: '合同总经理审批' },
      { code: 'contract:link', name: '合同关联操作' }
    ]
  },
  {
    id: 3, name: '采购管理', actions: [
      { code: 'purchase:list-manage', name: '采购清单管理' },
      { code: 'purchase:check-overbuy', name: '超量采购校验' }
    ]
  },
  {
    id: 4, name: '物资管理', actions: [
      { code: 'material:inbound', name: '物资入库操作' },
      { code: 'material:inbound-approve', name: '物资入库审批' },
      { code: 'material:outbound', name: '物资出库操作' },
      { code: 'material:outbound-approve', name: '物资出库审批' },
      { code: 'material:return', name: '物资退库操作' }
    ]
  },
  {
    id: 5, name: '施工管理', actions: [
      { code: 'progress:report', name: '进度填报' },
      { code: 'progress:view', name: '进度查看' },
      { code: 'progress:correct', name: '进度纠偏' },
      { code: 'change:apply', name: '变更申请' },
      { code: 'change:approve', name: '变更审批' },
      { code: 'statement:apply', name: '对账单申请' },
      { code: 'statement:approve', name: '对账单审批' }
    ]
  },
  {
    id: 6, name: '财务管理', actions: [
      { code: 'finance:reimburse-approve', name: '报销审批' },
      { code: 'finance:payment', name: '付款操作' },
      { code: 'finance:report-view', name: '查看财务报表' }
    ]
  },
  {
    id: 7, name: '人事管理', actions: [
      { code: 'hr:entry-process', name: '入职流程' },
      { code: 'hr:resign-process', name: '离职流程' },
      { code: 'hr:certificate-manage', name: '资质管理' },
      { code: 'hr:salary-adjust', name: '工资调整' },
      { code: 'hr:salary-approve', name: '工资审批' },
      { code: 'hr:contract-manage', name: '合同管理' },
      { code: 'hr:contract-view-own', name: '查看本人合同' }
    ]
  },
  {
    id: 8, name: '文档管理', actions: [
      { code: 'doc:upload', name: '上传文档' },
      { code: 'doc:download', name: '下载文档' },
      { code: 'doc:manage', name: '文档管理' }
    ]
  },
  {
    id: 9, name: '系统管理', actions: [
      { code: 'system:user-manage', name: '用户管理' },
      { code: 'system:role-manage', name: '角色管理' },
      { code: 'system:dept-manage', name: '部门管理' },
      { code: 'system:log-view', name: '查看日志' }
    ]
  }
])

const checkedActions = ref<string[]>([])

const deptTree = ref<any[]>([])
const checkedDeptIds = ref<number[]>([])

const normalizeMenuNodes = (nodes: any[]): any[] =>
  (nodes || []).map((n) => ({
    ...n,
    permissionName: n.permissionName || n.menuName || n.permissionCode || `权限-${n.id}`,
    children: n.children?.length ? normalizeMenuNodes(n.children) : undefined
  }))

const loadMenuTree = async () => {
  try {
    const res = await request<{ data: any[] }>({ url: '/system/menu/tree', method: 'GET' })
    menuTree.value = normalizeMenuNodes(res.data || [])
    const getAllIds = (nodes: any[]): number[] => {
      return nodes.reduce((acc: number[], node) => {
        acc.push(node.id)
        if (node.children) {
          acc.push(...getAllIds(node.children))
        }
        return acc
      }, [])
    }
    expandedMenuIds.value = getAllIds(menuTree.value)
  } catch (e) {
    menuTree.value = [
      { id: 1, menuName: '首页', icon: 'House', children: [{ id: 101, menuName: '首页数据' }] },
      { id: 2, menuName: '项目管理', icon: 'FolderOpened', children: [
        { id: 201, menuName: '项目列表' }, { id: 202, menuName: '项目新增' }, { id: 203, menuName: '项目编辑' }
      ]},
      { id: 3, menuName: '合同管理', icon: 'Document', children: [
        { id: 301, menuName: '收入合同' }, { id: 302, menuName: '支出合同' }
      ]},
      { id: 4, menuName: '供应商管理', icon: 'Shop', children: [{ id: 401, menuName: '供应商列表' }] },
      { id: 5, menuName: '采购管理', icon: 'ShoppingCart', children: [
        { id: 501, menuName: '采购清单' }, { id: 502, menuName: '采购编辑' }
      ]},
      { id: 6, menuName: '财务管理', icon: 'Money', children: [
        { id: 601, menuName: '收入对账' }, { id: 602, menuName: '回款督办' }, { id: 603, menuName: '付款计划' },
        { id: 604, menuName: '日常报销' }, { id: 605, menuName: '发票管理' }, { id: 606, menuName: '成本归集' }
      ]},
      { id: 7, menuName: '系统设置', icon: 'Setting', children: [
        { id: 701, menuName: '角色管理' }, { id: 702, menuName: '用户管理' }, { id: 703, menuName: '部门管理' }
      ]}
    ]
  }
}

const loadDeptTree = async () => {
  try {
    const res = await request<{ data: any[] }>({ url: '/system/department/tree', method: 'GET' })
    deptTree.value = res.data || []
  } catch (e) {
    deptTree.value = [{ id: 1, deptName: '总公司', children: [
      { id: 2, deptName: '工程项目管理部', children: [
        { id: 21, deptName: '项目团队A' }, { id: 22, deptName: '项目团队B' }, { id: 23, deptName: '项目团队C' }
      ]},
      { id: 3, deptName: '基础业务部' },
      { id: 4, deptName: '软件业务部' },
      { id: 5, deptName: '财务/综合部', children: [
        { id: 51, deptName: '财务组' }, { id: 52, deptName: '综合组' }
      ]},
      { id: 6, deptName: '技术支撑部', children: [
        { id: 61, deptName: '预算组' }, { id: 62, deptName: '采购组' }, { id: 63, deptName: '资料组' }
      ]}
    ]}]
  }
}

const loadRolePermission = async () => {
  const roleId = route.query.roleId
  if (!roleId) return
  try {
    const res = await request<{ data: number[] }>({ url: `/system/role/${roleId}/permissions`, method: 'GET' })
    checkedMenuIds.value = (res.data || []).map((id: number) => Number(id)).filter((id) => Number.isFinite(id))
  } catch {
    checkedMenuIds.value = []
  }
}

const syncMenuTreeChecked = async () => {
  await nextTick()
  const tree = menuTreeRef.value as { setCheckedKeys?: (keys: number[]) => void } | undefined
  tree?.setCheckedKeys?.(checkedMenuIds.value)
}

onMounted(async () => {
  const roleIdRaw = route.query.roleId
  const roleIdStr = typeof roleIdRaw === 'string' ? roleIdRaw : Array.isArray(roleIdRaw) ? roleIdRaw[0] : ''
  if (!roleIdStr) {
    ElMessage.warning('请从「角色管理」中点击「权限配置」进入此页')
    await router.replace('/system/role')
    return
  }
  await loadMenuTree()
  await loadDeptTree()
  await loadRolePermission()
  await syncMenuTreeChecked()
})

const handleMenuCheck = () => {}
const handleBack = () => { router.push('/system/role') }

const handleSave = async () => {
  const roleIdRaw = route.query.roleId
  const roleId = typeof roleIdRaw === 'string' ? Number(roleIdRaw) : Array.isArray(roleIdRaw) ? Number(roleIdRaw[0]) : NaN
  if (!Number.isFinite(roleId) || roleId <= 0) {
    ElMessage.warning('缺少角色信息，请从角色管理重新进入权限配置')
    return
  }

  activeTab.value = 'menu'
  await nextTick()

  const tree = menuTreeRef.value as
    | {
        getCheckedKeys?: (leafOnly?: boolean) => unknown[]
        getHalfCheckedKeys?: () => unknown[]
      }
    | undefined
  if (!tree?.getCheckedKeys) {
    ElMessage.error('菜单树未就绪，请稍后重试')
    return
  }

  const checkedKeys = (tree.getCheckedKeys?.() || []).map((k) => Number(k)).filter((k) => Number.isFinite(k))
  const halfCheckedKeys = (tree.getHalfCheckedKeys?.() || []).map((k) => Number(k)).filter((k) => Number.isFinite(k))
  const merged = [...new Set([...checkedKeys, ...halfCheckedKeys])]

  saveLoading.value = true
  try {
    await request({
      url: `/system/role/${roleId}/permissions`,
      method: 'POST',
      data: merged
    })
    ElMessage.success('权限保存成功')
    await router.push({
      path: '/system/role',
      query: {
        roleName: String(route.query.roleName || '')
      }
    })
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
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
.scope-desc p { margin: 4px 0; color: #666; font-size: 13px; }
</style>