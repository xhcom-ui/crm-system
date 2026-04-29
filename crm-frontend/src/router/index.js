import { createRouter, createWebHashHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘', icon: 'DataBoard' },
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('@/views/customer/CustomerList.vue'),
        meta: { title: '客户管理', icon: 'User' },
      },
      {
        path: 'customer/:id',
        name: 'CustomerDetail',
        component: () => import('@/views/customer/CustomerDetail.vue'),
        meta: { title: '客户详情', hidden: true },
      },
      {
        path: 'fat-management',
        name: 'FatManagement',
        component: () => import('@/views/fatManagement/FatManagement.vue'),
        meta: { title: '客户信息管理', icon: 'DataLine' },
      },
      {
        path: 'sales',
        name: 'Sales',
        component: () => import('@/views/sales/SalesList.vue'),
        meta: { title: '商机管理', icon: 'TrendCharts' },
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/product/ProductList.vue'),
        meta: { title: '产品管理', icon: 'Box' },
      },
      {
        path: 'marketing',
        name: 'Marketing',
        component: () => import('@/views/marketing/MarketingList.vue'),
        meta: { title: '营销自动化', icon: 'Promotion' },
      },
      {
        path: 'marketing/report',
        name: 'MarketingReport',
        component: () => import('@/views/marketing/MarketingReport.vue'),
        meta: { title: '营销效果报表', hidden: true },
      },
      {
        path: 'service',
        name: 'Service',
        component: () => import('@/views/service/ServiceList.vue'),
        meta: { title: '客户服务', icon: 'Service' },
      },
      {
        path: 'value-service',
        name: 'ValueService',
        component: () => import('@/views/valueService/ValueServiceList.vue'),
        meta: { title: '增值服务', icon: 'Service' },
      },
      {
        path: 'service/knowledge',
        name: 'ServiceKnowledge',
        component: () => import('@/views/service/KnowledgeBase.vue'),
        meta: { title: 'SLA 与知识库', hidden: true },
      },
      {
        path: 'sales-management',
        name: 'SalesManagement',
        component: () => import('@/views/salesManagement/SalesManagement.vue'),
        meta: { title: '销售管理', icon: 'Money' },
      },
      {
        path: 'payment',
        name: 'PaymentEngine',
        component: () => import('@/views/payment/PaymentEngine.vue'),
        meta: { title: '支付管理', icon: 'Wallet' },
      },
      {
        path: 'inventory',
        name: 'InventoryManagement',
        component: () => import('@/views/inventory/InventoryManagement.vue'),
        meta: { title: '库存进销存', icon: 'TakeawayBox' },
      },
      {
        path: 'followup',
        name: 'Followup',
        component: () => import('@/views/followup/FollowupList.vue'),
        meta: { title: '跟进记录', icon: 'Document' },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/StatsReport.vue'),
        meta: { title: '数据统计', icon: 'Histogram' },
      },
      {
        path: 'leads',
        name: 'Leads',
        component: () => import('@/views/leads/LeadsList.vue'),
        meta: { title: '线索管理', icon: 'Search' },
      },
      {
        path: 'workflow',
        name: 'Workflow',
        component: () => import('@/views/workflow/WorkflowList.vue'),
        meta: { title: '工作流自动化', icon: 'SetUp' },
      },
      {
        path: 'workflow/:id/design',
        name: 'WorkflowDesigner',
        component: () => import('@/views/workflow/WorkflowDesigner.vue'),
        meta: { title: '流程设计', hidden: true },
      },
      {
        path: 'workflow/instances',
        name: 'WorkflowInstances',
        component: () => import('@/views/workflow/WorkflowInstances.vue'),
        meta: { title: '审批实例追踪', hidden: true },
      },
      // ========== 系统管理 ==========
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/RoleList.vue'),
        meta: { title: '角色管理', icon: 'Avatar' },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/MenuList.vue'),
        meta: { title: '菜单管理', icon: 'Menu' },
      },
      {
        path: 'system/dept',
        name: 'SystemDept',
        component: () => import('@/views/system/DeptList.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' },
      },
      {
        path: 'system/oper-log',
        name: 'SystemOperLog',
        component: () => import('@/views/system/OperLog.vue'),
        meta: { title: '操作日志', icon: 'Document' },
      },
      {
        path: 'system/notif',
        name: 'SystemNotif',
        component: () => import('@/views/system/NotifManage.vue'),
        meta: { title: '通知管理', icon: 'Bell' },
      },
      {
        path: 'system/sentinel',
        name: 'SystemSentinel',
        component: () => import('@/views/system/SentinelConsole.vue'),
        meta: { title: 'Sentinel 控制台', icon: 'Monitor' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/system/ProfileView.vue'),
        meta: { title: '个人中心', hidden: true },
      },
      {
        path: '404',
        name: 'NotFound',
        component: () => import('@/views/error/NotFound.vue'),
        meta: { title: '页面未找到', hidden: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFoundRedirect',
    redirect: '/404',
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

/**
 * 全局路由守卫 —— Token 验证
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')

  // 访问登录页：已登录→跳转首页，未登录→放行
  if (to.meta.noAuth) {
    if (token) return next('/dashboard')
    return next()
  }

  // 访问需要认证的页面：无 token→跳转登录
  if (!token) return next('/login')

  next()
})

export default router
