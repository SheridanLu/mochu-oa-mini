import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: '/project',
        name: 'Project',
        component: () => import('@/views/project/index.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: '/project/create',
        name: 'ProjectCreate',
        component: () => import('@/views/project/edit.vue'),
        meta: { title: '新建项目' }
      },
      {
        path: '/project/edit',
        name: 'ProjectEdit',
        component: () => import('@/views/project/edit.vue'),
        meta: { title: '编辑项目' }
      },
      {
        path: '/contract',
        name: 'Contract',
        component: () => import('@/views/contract/index.vue'),
        meta: { title: '合同管理' }
      },
      {
        path: '/contract/create',
        name: 'ContractCreate',
        component: () => import('@/views/contract/edit.vue'),
        meta: { title: '新建合同' }
      },
      {
        path: '/contract/edit',
        name: 'ContractEdit',
        component: () => import('@/views/contract/edit.vue'),
        meta: { title: '编辑合同' }
      },
      {
        path: '/supplier',
        name: 'Supplier',
        component: () => import('@/views/supplier/index.vue'),
        meta: { title: '供应商管理' }
      },
      {
        path: '/supplier/create',
        name: 'SupplierCreate',
        component: () => import('@/views/supplier/edit.vue'),
        meta: { title: '新增供应商' }
      },
      {
        path: '/supplier/edit',
        name: 'SupplierEdit',
        component: () => import('@/views/supplier/edit.vue'),
        meta: { title: '编辑供应商' }
      },
      {
        path: '/price',
        name: 'Price',
        component: () => import('@/views/price/index.vue'),
        meta: { title: '询价与产品价格双库' }
      },
      {
        path: '/purchase',
        name: 'Purchase',
        component: () => import('@/views/purchase/index.vue'),
        meta: { title: '采购管理' }
      },
      {
        path: '/purchase/edit',
        name: 'PurchaseEdit',
        component: () => import('@/views/purchase/edit.vue'),
        meta: { title: '采购清单编辑' }
      },
      {
        path: '/material',
        name: 'Material',
        component: () => import('@/views/material/index.vue'),
        meta: { title: '物资管理' }
      },
      {
        path: '/material/edit',
        name: 'MaterialEdit',
        component: () => import('@/views/material/edit.vue'),
        meta: { title: '编辑物资' }
      },
      {
        path: '/material/create',
        name: 'MaterialCreate',
        component: () => import('@/views/material/edit.vue'),
        meta: { title: '新增物资' }
      },
      {
        path: '/construction',
        name: 'Construction',
        redirect: '/construction/gantt',
        meta: { title: '施工管理' },
        children: [
          {
            path: 'gantt',
            name: 'Gantt',
            component: () => import('@/views/construction/gantt/index.vue'),
            meta: { title: '甘特图' }
          },
          {
            path: 'gantt/edit',
            name: 'GanttEdit',
            component: () => import('@/views/construction/gantt/edit.vue'),
            meta: { title: '甘特图编辑' }
          },
          {
            path: 'monitor',
            name: 'GanttMonitor',
            component: () => import('@/views/construction/monitor/index.vue'),
            meta: { title: '进度监控' }
          }
        ]
      },
      {
        path: '/finance',
        name: 'Finance',
        redirect: '/finance/reconciliation',
        meta: { title: '财务管理' },
        children: [
          {
            path: 'reconciliation',
            name: 'FinanceReconciliation',
            component: () => import('@/views/finance/reconciliation/index.vue'),
            meta: { title: '收入对账' }
          },
          {
            path: 'reconciliation/detail',
            name: 'ReconciliationDetail',
            component: () => import('@/views/finance/reconciliation/detail/index.vue'),
            meta: { title: '对账详情' }
          },
          {
            path: 'supervision',
            name: 'FinanceSupervision',
            component: () => import('@/views/finance/supervision/index.vue'),
            meta: { title: '回款督办' }
          },
          {
            path: 'supervision/detail',
            name: 'SupervisionDetail',
            component: () => import('@/views/finance/supervision/detail.vue'),
            meta: { title: '督办详情' }
          },
          {
            path: 'payment-plan',
            name: 'FinancePaymentPlan',
            component: () => import('@/views/finance/payment-plan/index.vue'),
            meta: { title: '付款计划' }
          },
          {
            path: 'payment-apply',
            name: 'FinancePaymentApply',
            component: () => import('@/views/finance/payment-apply/index.vue'),
            meta: { title: '付款申请' }
          },
          {
            path: 'expense',
            name: 'FinanceExpense',
            component: () => import('@/views/finance/expense/index.vue'),
            meta: { title: '日常报销' }
          },
          {
            path: 'expense/edit',
            name: 'FinanceExpenseEdit',
            component: () => import('@/views/finance/expense/edit.vue'),
            meta: { title: '报销单编辑' }
          },
          {
            path: 'invoice',
            name: 'FinanceInvoice',
            component: () => import('@/views/finance/invoice/index.vue'),
            meta: { title: '发票管理' }
          },
          {
            path: 'budget',
            name: 'FinanceBudget',
            component: () => import('@/views/finance/budget/index.vue'),
            meta: { title: '部门预算' }
          },
          {
            path: 'cost',
            name: 'FinanceCost',
            component: () => import('@/views/finance/cost/index.vue'),
            meta: { title: '成本归集' }
          }
        ]
      },
      {
        path: '/report',
        name: 'Report',
        component: () => import('@/views/report/index.vue'),
        meta: { title: '报表中心' }
      },
      {
        path: '/report/project',
        name: 'ReportProject',
        component: () => import('@/views/report/project.vue'),
        meta: { title: '项目进度报表' }
      },
      {
        path: '/report/purchase',
        name: 'ReportPurchase',
        component: () => import('@/views/report/purchase.vue'),
        meta: { title: '采购分析报表' }
      },
      {
        path: '/report/finance',
        name: 'ReportFinance',
        component: () => import('@/views/report/finance.vue'),
        meta: { title: '财务分析报表' }
      },
      {
        path: '/report/custom',
        name: 'ReportCustom',
        component: () => import('@/views/report/custom.vue'),
        meta: { title: '自定义报表' }
      },
      {
        path: '/approval',
        name: 'Approval',
        component: () => import('@/views/approval/index.vue'),
        meta: { title: '审批中心' }
      },
      {
        path: '/system',
        name: 'System',
        redirect: '/system/announcement',
        meta: { title: '系统设置' },
        children: [
          {
            path: 'announcement',
            name: 'Announcement',
            component: () => import('@/views/system/announcement/index.vue'),
            meta: { title: '公告管理' }
          },
          {
            path: 'company',
            name: 'Company',
            component: () => import('@/views/system/company/index.vue'),
            meta: { title: '公司信息' }
          },
          {
            path: 'role',
            name: 'Role',
            component: () => import('@/views/system/role/index.vue'),
            meta: { title: '角色管理' }
          },
          {
            path: 'user',
            name: 'User',
            component: () => import('@/views/system/user/index.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'permission',
            name: 'Permission',
            component: () => import('@/views/system/permission/index.vue'),
            meta: { title: '权限配置' }
          },
          {
            path: 'dept',
            name: 'Dept',
            component: () => import('@/views/system/dept/index.vue'),
            meta: { title: '部门管理' }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    const userInfo = localStorage.getItem('userInfo')
    // 仅在存在有效用户信息缓存时才跳过登录页，避免无效 token 直接进首页
    next(userInfo ? '/home' : '/login')
  } else if (!to.path || to.path === '/') {
    next('/home')
  } else {
    next()
  }
})

export default router