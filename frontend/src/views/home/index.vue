<template>
  <div class="home-container">
    <div class="home-toolbar">
      <div class="toolbar-left">
        <span class="welcome-text">工作台</span>
        <span class="welcome-sub">待办与公告每 5 分钟自动刷新，亦可手动刷新全部模块</span>
      </div>
      <el-button type="primary" :loading="homeRefreshing" @click="refreshAll">刷新全部</el-button>
    </div>

    <el-card v-if="recentList.length > 0" class="recent-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>最近访问</span>
          <el-button type="danger" link @click="clearRecentList">清空记录</el-button>
        </div>
      </template>
      <div class="recent-chips">
        <el-tag
          v-for="item in recentList"
          :key="item.path + '-' + item.at"
          class="recent-chip"
          effect="plain"
          @click="router.push(item.path)"
        >
          {{ item.title }}
        </el-tag>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card" @click="$router.push('/project')">
          <div class="stat-icon" style="background: #409eff">
            <el-icon><Folder /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.projectCount }}</div>
            <div class="stat-label">进行中项目</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" @click="$router.push('/contract')">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.contractCount }}</div>
            <div class="stat-label">待审批合同</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" @click="$router.push('/purchase')">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.purchaseCount }}</div>
            <div class="stat-label">待采购清单</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" @click="$router.push('/finance/reconciliation')">
          <div class="stat-icon" style="background: #f56c6c">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.statementCount }}</div>
            <div class="stat-label">待对账单</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button type="primary" link @click="$router.push('/approval')">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="todoLoading" class="todo-list">
            <template v-if="todoList.length > 0">
              <div v-for="item in todoList" :key="item.id" class="todo-item" @click="handleTodoClick(item)">
                <div class="todo-content">
                  <span class="todo-title">{{ item.title }}</span>
                  <el-tag :type="getPriorityType(item.priority)" size="small">{{ item.priorityText }}</el-tag>
                </div>
                <div class="todo-meta">
                  <span class="todo-type">{{ item.bizTypeText }}</span>
                  <span class="todo-project">{{ item.projectName }}</span>
                </div>
                <div class="todo-time">{{ formatDateTime(item.createdAt) }}</div>
              </div>
            </template>
            <el-empty v-else description="暂无待办事项" :image-size="60" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="notice-card">
          <template #header>
            <div class="card-header">
              <span>系统公告</span>
              <el-button type="primary" link @click="$router.push('/system/announcement')">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="carouselLoading" class="carousel-container" v-if="noticeCarousel.length >= 3">
            <el-carousel height="150px" indicator-position="outside" :interval="5000" autoplay @change="handleCarouselChange">
              <el-carousel-item v-for="item in noticeCarousel" :key="item.id">
                <div class="carousel-item" @click="handleNoticeClick(item)">
                  <img v-if="item.coverImage" :src="item.coverImage" class="carousel-img" @error="handleImageError" />
                  <div v-else class="carousel-placeholder">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="carousel-info">
                    <span class="carousel-title">{{ item.title }}</span>
                    <span class="carousel-summary">{{ item.summary }}</span>
                    <span class="carousel-time">{{ formatDate(item.publishTime) }}</span>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
          <div v-else v-loading="noticeLoading" class="notice-list">
            <template v-if="noticeList.length > 0">
              <div v-for="item in noticeList" :key="item.id" class="notice-item" @click="handleNoticeClick(item)">
                <el-icon v-if="item.isTop" color="#f56c6c"><Top /></el-icon>
                <span class="notice-title">{{ item.title }}</span>
                <span class="notice-time">{{ formatDate(item.publishTime) }}</span>
              </div>
            </template>
            <el-empty v-else description="暂无公告" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="quick-entry-card">
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
              <el-button type="primary" link @click="showCustomizeDialog = true">
                <el-icon><Setting /></el-icon>自定义
              </el-button>
            </div>
          </template>
          <div class="quick-entry-list">
            <div
              class="quick-entry-item"
              v-for="item in quickEntries"
              :key="item.path"
              @click="$router.push(item.path)"
            >
              <div class="quick-entry-icon" :style="{ background: item.color }">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <span class="quick-entry-label">{{ item.label }}</span>
              <span class="quick-entry-desc">{{ item.desc }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showCustomizeDialog" title="自定义快捷入口" width="500px">
      <p class="customize-tip">最多添加12个快捷入口</p>
      <el-checkbox-group v-model="selectedEntries" class="customize-group">
        <el-checkbox v-for="item in allEntries" :key="item.path" :label="item.path">
          {{ item.label }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="showCustomizeDialog = false">取消</el-button>
        <el-button type="primary" @click="saveQuickEntries">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatDate, formatDateTime } from '../../utils/format'
import { api } from '../../api'
import { useUserStore } from '../../stores/user'
import { getRecentVisits, clearRecentVisits } from '@/utils/recentVisits'

const router = useRouter()
const userStore = useUserStore()

const REFRESH_INTERVAL = 5 * 60 * 1000

const todoLoading = ref(false)
const noticeLoading = ref(false)
const carouselLoading = ref(false)
const showCustomizeDialog = ref(false)
const homeRefreshing = ref(false)
const recentList = ref<{ path: string; title: string; at: number }[]>([])

const stats = reactive({
  projectCount: 0,
  contractCount: 0,
  purchaseCount: 0,
  statementCount: 0
})

const todoList = ref<any[]>([])
const noticeCarousel = ref<any[]>([])
const noticeList = ref<any[]>([])

const allEntries = ref([
  { path: '/finance/reconciliation', label: '新建对账单', desc: '创建收入对账单', icon: 'Document', color: '#409eff' },
  { path: '/finance/supervision', label: '生成督办计划', desc: '创建回款督办', icon: 'TrendCharts', color: '#67c23a' },
  { path: '/finance/expense', label: '新建报销单', desc: '日常费用报销', icon: 'Coin', color: '#e6a23c' },
  { path: '/finance/invoice', label: '查询发票认证', desc: '发票状态查询', icon: 'Ticket', color: '#f56c6c' },
  { path: '/finance/payment-plan', label: '月度付款计划', desc: '查看付款计划', icon: 'Calendar', color: '#909399' },
  { path: '/purchase', label: '采购清单', desc: '管理采购清单', icon: 'ShoppingCart', color: '#c71585' },
  { path: '/project', label: '项目管理', desc: '项目列表查询', icon: 'Folder', color: '#409eff' },
  { path: '/contract', label: '合同管理', desc: '合同列表查询', icon: 'Document', color: '#67c23a' },
  { path: '/supplier', label: '供应商', desc: '供应商管理', icon: 'Shop', color: '#e6a23c' },
  { path: '/material', label: '物资管理', desc: '物资库存管理', icon: 'Box', color: '#f56c6c' },
  { path: '/construction/gantt', label: '施工管理', desc: '施工进度管理', icon: 'PieChart', color: '#909399' },
  { path: '/system/user', label: '用户管理', desc: '系统用户管理', icon: 'User', color: '#c71585' }
])

const selectedEntries = ref<string[]>([])
const quickEntries = computed(() => {
  if (selectedEntries.value.length === 0) {
    return allEntries.value.slice(0, 6)
  }
  return allEntries.value.filter(e => selectedEntries.value.includes(e.path))
})

const BIZ_TYPE_MAP: Record<string, string> = {
  project: '项目立项',
  contract: '合同审批',
  purchase: '采购审批',
  payment: '付款审批',
  expense: '报销审批',
  statement: '对账确认'
}

const bizTypeLabel = (bizType: string) => BIZ_TYPE_MAP[(bizType || '').toLowerCase()] || bizType || '-'

let refreshTimer: ReturnType<typeof setInterval> | null = null

const fetchStats = async () => {
  try {
    const res = await api.home.getStats()
    if (res.code === 200) {
      Object.assign(stats, res.data || {})
    }
  } catch (e) {
    console.error('获取统计失败', e)
  }
}

const fetchTodoList = async () => {
  const uid = userStore.userInfo?.id
  if (!uid) return
  todoLoading.value = true
  try {
    const res = await api.todo.list({ userId: uid, category: 'TODO', page: 1, size: 10 })
    if (res.code === 200) {
      todoList.value = (res.data?.list || []).map((item: any) => ({
        ...item,
        bizTypeText: bizTypeLabel(item.bizType),
        priorityText: item.priority === 1 ? '紧急' : item.priority === 2 ? '重要' : '一般',
        priorityTextVal: item.priority
      }))
    }
  } catch (e: any) {
    console.error('获取待办失败', e)
  } finally {
    todoLoading.value = false
  }
}

const fetchCarousel = async () => {
  carouselLoading.value = true
  try {
    const res = await api.announcement.carousel({ limit: 5 })
    if (res.code === 200) {
      noticeCarousel.value = res.data || []
    }
  } catch (e) {
    console.error('获取轮播失败', e)
  } finally {
    carouselLoading.value = false
  }
}

const fetchNoticeList = async () => {
  noticeLoading.value = true
  try {
    const res = await api.announcement.list({ status: 'published', page: 1, size: 5 })
    if (res.code === 200) {
      noticeList.value = res.data?.list || []
    }
  } catch (e) {
    console.error('获取公告失败', e)
  } finally {
    noticeLoading.value = false
  }
}

const getPriorityType = (priority: number) => {
  const types = ['danger', 'warning', 'info']
  return types[priority - 1] || 'info'
}

const handleTodoClick = (item: any) => {
  if (item.instanceId) {
    router.push({ path: '/approval', query: { instanceId: String(item.instanceId) } })
  } else {
    router.push('/approval')
  }
}

const handleNoticeClick = (item: any) => {
  router.push(`/system/announcement/detail?id=${item.id}`)
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
}

const handleCarouselChange = (index: number) => {
  console.log('轮播切换', index)
}

const loadQuickEntries = () => {
  const saved = localStorage.getItem('quickEntries')
  if (saved) {
    try {
      selectedEntries.value = JSON.parse(saved)
    } catch {
      selectedEntries.value = []
    }
  }
}

const saveQuickEntries = () => {
  localStorage.setItem('quickEntries', JSON.stringify(selectedEntries.value))
  showCustomizeDialog.value = false
  ElMessage.success('保存成功')
}

const loadRecentList = () => {
  recentList.value = getRecentVisits()
}

const clearRecentList = () => {
  clearRecentVisits()
  loadRecentList()
  ElMessage.success('已清空最近访问')
}

const refreshAll = async () => {
  homeRefreshing.value = true
  try {
    await Promise.all([fetchStats(), fetchTodoList(), fetchCarousel(), fetchNoticeList()])
    loadRecentList()
    ElMessage.success('已刷新')
  } finally {
    homeRefreshing.value = false
  }
}

const initRefreshTimer = () => {
  if (refreshTimer) clearInterval(refreshTimer)
  refreshTimer = setInterval(() => {
    fetchTodoList()
    fetchCarousel()
    fetchNoticeList()
    fetchStats()
  }, REFRESH_INTERVAL)
}

onMounted(() => {
  fetchStats()
  fetchTodoList()
  fetchCarousel()
  fetchNoticeList()
  loadQuickEntries()
  loadRecentList()
  initRefreshTimer()
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.home-container { padding: 20px; }
.home-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.toolbar-left { display: flex; flex-direction: column; gap: 4px; }
.welcome-text { font-size: 18px; font-weight: 600; color: #303133; }
.welcome-sub { font-size: 12px; color: #909399; }
.recent-card { margin-bottom: 16px; border-radius: 8px; }
.recent-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.recent-chip { cursor: pointer; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; display: flex; align-items: center; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); cursor: pointer; transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-2px); }
.stat-icon { width: 60px; height: 60px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 28px; color: #fff; margin-right: 16px; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 500; }
.todo-list, .notice-list { max-height: 320px; overflow-y: auto; }
.todo-item { padding: 12px; border-bottom: 1px solid #f0f0f0; cursor: pointer; transition: background 0.2s; }
.todo-item:hover { background: #f5f7fa; }
.todo-content { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.todo-title { font-size: 14px; color: #303133; font-weight: 500; }
.todo-meta { display: flex; gap: 12px; font-size: 12px; color: #909399; margin-bottom: 4px; }
.todo-time { font-size: 12px; color: #c0c4cc; }
.notice-item { padding: 12px; border-bottom: 1px solid #f0f0f0; display: flex; align-items: center; gap: 8px; cursor: pointer; transition: background 0.2s; }
.notice-item:hover { background: #f5f7fa; }
.notice-title { flex: 1; font-size: 14px; color: #303133; }
.notice-time { font-size: 12px; color: #909399; }
.carousel-container { padding: 10px; }
.carousel-item { height: 100%; display: flex; align-items: center; padding: 15px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 8px; cursor: pointer; }
.carousel-img { width: 100px; height: 100px; object-fit: cover; border-radius: 4px; margin-right: 16px; flex-shrink: 0; }
.carousel-placeholder { width: 100px; height: 100px; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.2); border-radius: 4px; margin-right: 16px; flex-shrink: 0; font-size: 40px; color: #fff; }
.carousel-info { display: flex; flex-direction: column; gap: 8px; overflow: hidden; }
.carousel-title { font-size: 16px; color: #fff; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.carousel-summary { font-size: 14px; color: rgba(255,255,255,0.8); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.carousel-time { font-size: 12px; color: rgba(255,255,255,0.6); }
.quick-entry-list { display: flex; gap: 16px; flex-wrap: wrap; }
.quick-entry-item { width: 160px; padding: 16px; background: #f8f9fa; border-radius: 8px; text-align: center; cursor: pointer; transition: all 0.2s; }
.quick-entry-item:hover { background: #ecf5ff; transform: translateY(-2px); }
.quick-entry-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin: 0 auto 12px; font-size: 24px; color: #fff; }
.quick-entry-label { display: block; font-size: 14px; color: #303133; font-weight: 500; margin-bottom: 4px; }
.quick-entry-desc { display: block; font-size: 12px; color: #909399; }
.customize-tip { color: #909399; margin-bottom: 16px; }
.customize-group { display: flex; flex-wrap: wrap; gap: 10px; }
.customize-group .el-checkbox { width: 120px; margin-right: 0; }
</style>