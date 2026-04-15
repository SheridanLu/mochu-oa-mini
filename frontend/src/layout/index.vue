<template>
  <el-container class="layout-container">
    <el-aside width="220px" :class="{ collapsed: isCollapse }">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="logo" />
        <span v-if="!isCollapse">墨初OA</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="#1a1a2e"
        text-color="#a0aec0"
        active-text-color="#409eff"
      >
        <el-menu-item index="/home">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-sub-menu index="/project">
          <template #title>
            <el-icon><FolderOpened /></el-icon>
            <span>项目管理</span>
          </template>
          <el-menu-item index="/project">项目列表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/contract">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>合同管理</span>
          </template>
          <el-menu-item index="/contract">收入合同</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/supplier">
          <template #title>
            <el-icon><Shop /></el-icon>
            <span>供应商</span>
          </template>
          <el-menu-item index="/supplier">供应商列表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/purchase">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>采购管理</span>
          </template>
          <el-menu-item index="/purchase">采购清单</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/material">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>物资管理</span>
          </template>
          <el-menu-item index="/material">物资库存</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/construction">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>施工管理</span>
          </template>
          <el-menu-item index="/construction/gantt">甘特图</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/finance">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>财务管理</span>
          </template>
          <el-menu-item index="/finance/reconciliation">收入对账</el-menu-item>
          <el-menu-item index="/finance/supervision">回款督办</el-menu-item>
          <el-menu-item index="/finance/payment-plan">付款计划</el-menu-item>
          <el-menu-item index="/finance/payment-apply">付款申请</el-menu-item>
          <el-menu-item index="/finance/expense">日常报销</el-menu-item>
          <el-menu-item index="/finance/invoice">发票管理</el-menu-item>
          <el-menu-item index="/finance/budget">部门预算</el-menu-item>
          <el-menu-item index="/finance/cost">成本归集</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/report">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>报表中心</span>
          </template>
          <el-menu-item index="/report">报表概览</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/approval">
          <template #title>
            <el-icon><Checked /></el-icon>
            <span>审批中心</span>
          </template>
          <el-menu-item index="/approval">我的审批</el-menu-item>
          <el-menu-item index="/approval/flow">流程管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/system/announcement">公告管理</el-menu-item>
          <el-menu-item index="/system/company">公司信息</el-menu-item>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/dept">部门管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute">{{ currentRoute }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-badge :value="todoBadge" :hidden="todoBadge === 0" :max="99" class="badge" @click="goApproval">
            <el-icon><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-icon><User /></el-icon>
              <span>{{ displayName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <el-dialog
    v-model="passwordDialogVisible"
    title="修改登录密码"
    width="480px"
    destroy-on-close
    :close-on-click-modal="!mustForceChangePassword"
    :close-on-press-escape="!mustForceChangePassword"
    :show-close="!mustForceChangePassword"
  >
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="110px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
      </el-form-item>
      <div class="pwd-tip">密码要求：8-20位，且必须包含大写字母、小写字母、数字和特殊字符。</div>
    </el-form>
    <template #footer>
      <el-button v-if="!mustForceChangePassword" @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="passwordSubmitting" @click="handleChangePassword">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import { api } from '@/api'
import { pushRecentVisit } from '@/utils/recentVisits'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()

const isCollapse = ref(false)
const todoBadge = ref(0)
const passwordDialogVisible = ref(false)
const passwordSubmitting = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+\-={}\[\]|:;"'<>,.?/]).{8,20}$/
const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (!passwordPattern.test(value || '')) {
          callback(new Error('密码必须8-20位，且包含大小写字母、数字和特殊字符'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}
const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route.meta.title as string)
const mustForceChangePassword = computed(() => !!userStore.userInfo?.mustChangePassword)
const displayName = computed(() => {
  const realName = userStore.userInfo?.realName || ''
  const username = userStore.userInfo?.username || ''
  // realName 出现替换字符时，优先展示 username，避免界面显示乱码
  if (realName.includes('�')) return username || '未登录'
  return realName || username || '未登录'
})

let todoPollTimer: ReturnType<typeof setInterval> | null = null

const refreshTodoBadge = async () => {
  const uid = userStore.userInfo?.id
  if (!uid) {
    todoBadge.value = 0
    return
  }
  try {
    const res: any = await api.todo.count({ userId: uid })
    if (res.code === 200) {
      todoBadge.value = Number(res.data?.todoCount) || 0
    }
  } catch {
    /* 忽略角标拉取失败 */
  }
}

const goApproval = () => {
  router.push('/approval')
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    menuStore.clearMenus()
    router.push('/login')
  } else if (command === 'settings') {
    router.push('/system/announcement')
  } else if (command === 'profile') {
    router.push('/home')
  } else if (command === 'changePassword') {
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    passwordDialogVisible.value = true
  }
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return
  passwordSubmitting.value = true
  try {
    await api.auth.changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
      confirmPassword: passwordForm.value.confirmPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordDialogVisible.value = false
    userStore.logout()
    menuStore.clearMenus()
    router.replace('/login')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '修改密码失败')
  } finally {
    passwordSubmitting.value = false
  }
}

const loadMenus = async () => {
  if (!menuStore.isLoaded) {
    try {
      const res: any = await api.system.menu.tree()
      if (res.code === 200) {
        menuStore.setMenus(res.data || [])
      }
    } catch (e) {
      console.error('加载菜单失败', e)
    }
  }
}

const ensureUserInfo = async () => {
  if (!userStore.token) return
  try {
    const res: any = await api.auth.getUserInfo()
    if (res.code === 200 && res.data) {
      userStore.setUserInfo({
        id: Number(res.data.userId) || 0,
        username: res.data.username || '',
        realName: res.data.realName || '',
        avatar: res.data.avatar || '',
        department: res.data.departmentName || '',
        roles: [],
        mustChangePassword: userStore.userInfo?.mustChangePassword || false
      })
      if (userStore.userInfo?.mustChangePassword) {
        passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
        passwordDialogVisible.value = true
      }
      return
    }
  } catch {
    // token 无效时统一走登出回登录
  }
  userStore.logout()
  menuStore.clearMenus()
  router.replace('/login')
}

watch(
  () => route.fullPath,
  () => {
    const title = (route.meta?.title as string) || ''
    pushRecentVisit(route.path, title)
  },
  { immediate: true }
)

onMounted(() => {
  void ensureUserInfo().then(() => {
    loadMenus()
    void refreshTodoBadge()
  })
  todoPollTimer = setInterval(() => void refreshTodoBadge(), 120000)
})

onUnmounted(() => {
  if (todoPollTimer) clearInterval(todoPollTimer)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #1a1a2e;
  transition: width 0.3s;
}

.el-aside.collapsed {
  width: 64px;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 1px solid #2d3748;
}

.logo img {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  margin-right: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.badge {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.el-main {
  background: #f5f7fa;
  padding: 20px;
}

.pwd-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
  margin-left: 110px;
}
</style>