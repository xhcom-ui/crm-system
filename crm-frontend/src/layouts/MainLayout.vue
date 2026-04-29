<template>
  <el-container class="main-container" :class="{ 'is-mobile': isMobile, 'is-collapsed': isCollapse }">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '68px' : '232px'" class="sidebar">
      <div class="logo">
        <span class="logo-mark">C</span>
        <span v-if="!isCollapse" class="logo-text">CRM 管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="transparent"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
      >
        <template v-for="item in menuList" :key="item.id">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="item.children && item.children.length" :index="resolveMenuIndex(item)">
            <template #title>
              <div class="submenu-title" @click="handleSubMenuTitleClick(item)">
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.menuName }}</span>
              </div>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.id" :index="resolveMenuIndex(child, item)">
              <el-icon><component :is="child.icon" /></el-icon>
              <span>{{ child.menuName }}</span>
            </el-menu-item>
          </el-sub-menu>
          <!-- 无子菜单 -->
          <el-menu-item v-else :index="resolveMenuIndex(item)">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.menuName }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse" :size="22">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span
            class="notif-indicator"
            :title="notifConnected ? 'SSE 通知已连接' : 'SSE 通知未连接'"
            @click="openNotificationList"
          >
            <el-badge :value="unreadCount || ''" :is-dot="!unreadCount && notifConnected" :type="notifConnected ? 'success' : 'info'" :hidden="!unreadCount && !notifConnected">
              <el-icon :size="20" :color="notifConnected ? '#67c23a' : '#c0c4cc'">
                <Bell />
              </el-icon>
            </el-badge>
            <span class="mode-tag">{{ notifMode.toUpperCase() }}</span>
          </span>
          <el-dropdown trigger="click">
            <span class="user-dropdown">
              <el-avatar :size="28" icon="UserFilled" />
              <span class="user-name">{{ userStore.userInfo?.realName || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')" :icon="'User'">
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout" :icon="'SwitchButton'">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <el-drawer v-model="notificationDrawer" title="消息列表" size="420px" append-to-body>
      <div class="message-toolbar">
        <span>{{ notifConnected ? 'SSE 已连接' : 'SSE 未连接' }}</span>
        <div>
          <el-button size="small" :icon="'Refresh'" @click="loadNotifications">刷新</el-button>
          <el-button size="small" type="primary" plain @click="router.push('/system/notif')">通知管理</el-button>
        </div>
      </div>
      <div v-loading="notificationLoading" class="message-list">
        <el-empty v-if="!notificationRows.length" description="暂无消息" />
        <div v-for="item in notificationRows" :key="item.id || item.localId" class="message-card">
          <div class="message-head">
            <strong>{{ item.title || '系统通知' }}</strong>
            <el-tag :type="noticeTagType(item.type)" size="small">{{ noticeTypeLabel(item.type) }}</el-tag>
          </div>
          <p>{{ item.message || '-' }}</p>
          <div class="message-meta">
            <span>{{ item.sender || '系统' }}</span>
            <span>{{ item.createTime || item.timestamp || '刚刚' }}</span>
          </div>
          <div v-if="item.channels || item.pushSummary" class="message-meta">
            <span>渠道：{{ channelNames(item.channels) }}</span>
            <span>{{ item.pushSummary || '' }}</span>
          </div>
        </div>
      </div>
      <el-pagination
        v-model:current-page="notificationPage.current"
        v-model:page-size="notificationPage.size"
        :total="notificationPage.total"
        small
        layout="prev, pager, next"
        class="message-pagination"
        @current-change="loadNotifications"
      />
    </el-drawer>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useNotification } from '@/composables/useNotification'
import { getUserRoutes } from '@/api/auth'
import { getNotificationPage } from '@/api/system'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const isMobile = ref(false)
const menuList = ref([])
const notificationDrawer = ref(false)
const notificationLoading = ref(false)
const notificationRows = ref([])
const unreadCount = ref(0)
const notificationPage = ref({ current: 1, size: 10, total: 0 })

// 统一通知 (SSE)
const { connected: notifConnected, mode: notifMode, connect: notifConnect, disconnect: notifDisconnect } = useNotification()

function channelNames(value) {
  if (!value) return '站内'
  const map = { email: '邮件', dingtalk: '钉钉', feishu: '飞书', sms: '短信', wechat: '微信' }
  return value.split(',').filter(Boolean).map(item => map[item] || item).join('、')
}

function noticeTagType(type) {
  const map = { info: 'info', success: 'success', warning: 'warning', error: 'danger' }
  return map[type] || 'info'
}

function noticeTypeLabel(type) {
  const map = { info: '信息', success: '成功', warning: '警告', error: '错误' }
  return map[type] || '信息'
}

async function loadNotifications() {
  notificationLoading.value = true
  try {
    const res = await getNotificationPage({
      current: notificationPage.value.current,
      size: notificationPage.value.size,
    })
    notificationRows.value = res.data?.records || []
    notificationPage.value.total = res.data?.total || 0
  } catch (e) {
    notificationRows.value = []
    notificationPage.value.total = 0
  } finally {
    notificationLoading.value = false
  }
}

async function openNotificationList() {
  notificationDrawer.value = true
  unreadCount.value = 0
  await loadNotifications()
}

function handleRealtimeNotification(event) {
  const data = event.detail || {}
  const item = {
    ...data,
    localId: `local-${Date.now()}-${Math.random()}`,
    sender: '系统',
    createTime: data.timestamp || new Date().toLocaleString(),
  }
  notificationRows.value = [item, ...notificationRows.value].slice(0, notificationPage.value.size)
  if (!notificationDrawer.value) unreadCount.value += 1
}

function checkMobile() {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) isCollapse.value = true
}

/**
 * 加载动态菜单
 */
async function loadMenus() {
  try {
    const res = await getUserRoutes()
    menuList.value = normalizeMenus(res.data || [])
  } catch (e) {
    console.warn('加载菜单失败，使用默认菜单')
    menuList.value = getDefaultMenus()
  }
}

/**
 * 默认菜单（API 失败时的兜底）
 */
function getDefaultMenus() {
  return [
    { id: 300, menuName: '工作台', path: 'workspace', icon: 'DataBoard', children: [
      { id: 18, menuName: '仪表盘', path: '/dashboard', icon: 'DataBoard', children: [] },
      { id: 104, menuName: '数据统计', path: '/statistics', icon: 'Histogram', children: [] },
    ]},
    { id: 301, menuName: '客户运营', path: 'customer-ops', icon: 'User', children: [
      { id: 19, menuName: '客户管理', path: '/customer', icon: 'User', children: [] },
      { id: 103, menuName: '跟进记录', path: '/followup', icon: 'Document', children: [] },
      { id: 201, menuName: '客户信息管理', path: '/fat-management', icon: 'DataLine', children: [] },
    ]},
    { id: 302, menuName: '销售交易', path: 'sales-ops', icon: 'TrendCharts', children: [
      { id: 23, menuName: '线索管理', path: '/leads', icon: 'Search', children: [] },
      { id: 20, menuName: '商机管理', path: '/sales', icon: 'TrendCharts', children: [] },
      { id: 102, menuName: '销售管理', path: '/sales-management', icon: 'Money', children: [] },
      { id: 202, menuName: '支付管理', path: '/payment', icon: 'Wallet', children: [] },
    ]},
    { id: 303, menuName: '产品服务', path: 'product-service', icon: 'Box', children: [
      { id: 100, menuName: '产品管理', path: '/product', icon: 'Box', children: [] },
      { id: 101, menuName: '增值服务', path: '/value-service', icon: 'Service', children: [] },
      { id: 22, menuName: '客户服务', path: '/service', icon: 'Headset', children: [] },
      { id: 203, menuName: '库存进销存', path: '/inventory', icon: 'TakeawayBox', children: [] },
    ]},
    { id: 304, menuName: '营销增长', path: 'marketing-growth', icon: 'Promotion', children: [
      { id: 21, menuName: '营销活动', path: '/marketing', icon: 'Promotion', children: [] },
      { id: 115, menuName: '营销效果报表', path: '/marketing/report', icon: 'Histogram', children: [] },
    ]},
    { id: 305, menuName: '自动化', path: 'automation', icon: 'SetUp', children: [
      { id: 24, menuName: '工作流自动化', path: '/workflow', icon: 'SetUp', children: [] },
      { id: 116, menuName: '审批实例追踪', path: '/workflow/instances', icon: 'Connection', children: [] },
    ]},
    { id: 1, menuName: '系统管理', path: 'system', icon: 'Setting', children: [
      { id: 81, menuName: '用户管理', path: 'user', icon: 'User', children: [] },
      { id: 82, menuName: '角色管理', path: 'role', icon: 'Avatar', children: [] },
      { id: 83, menuName: '菜单管理', path: 'menu', icon: 'Menu', children: [] },
      { id: 84, menuName: '部门管理', path: 'dept', icon: 'OfficeBuilding', children: [] },
      { id: 85, menuName: '操作日志', path: 'oper-log', icon: 'Document', children: [] },
      { id: 86, menuName: '通知管理', path: 'notif', icon: 'Bell', children: [] },
      { id: 117, menuName: 'Sentinel 控制台', path: 'sentinel', icon: 'Monitor', children: [] },
    ]},
  ]
}

function resolveMenuIndex(item, parent) {
  if (!item?.path) return parent ? resolveMenuIndex(parent) : '/'
  if (item.path.startsWith('/')) return item.path
  if (!parent) return `/${item.path}`
  return `${resolveMenuIndex(parent)}/${item.path}`
}

function isRouteAvailable(path) {
  const matched = router.resolve(path).matched
  return matched.length > 0 && path !== '/404'
}

function normalizeMenus(items, parent) {
  return (items || [])
    .map(item => {
      const next = { ...item }
      next.children = normalizeMenus(item.children || [], item)
      if (next.children.length) return next
      const path = resolveMenuIndex(item, parent)
      return isRouteAvailable(path) ? next : null
    })
    .filter(Boolean)
}

function handleSubMenuTitleClick(item) {
  const firstChild = item?.children?.find(child => child.path)
  if (firstChild) router.push(resolveMenuIndex(firstChild, item))
}

onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  // 恢复用户会话
  if (userStore.token && !userStore.userInfo) {
    try { await userStore.fetchUserInfo() } catch (e) { /* ignore */ }
  }
  // 加载动态菜单 + 建立通知连接
  await loadMenus()
  if (userStore.token) notifConnect()
  window.addEventListener('crm-notification', handleRealtimeNotification)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  window.removeEventListener('crm-notification', handleRealtimeNotification)
})

const activeMenu = computed(() => route.path)

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleLogout() {
  notifDisconnect()
  userStore.logout()
  router.push('/login')
}

</script>

<style scoped>
.main-container {
  height: 100vh;
  background: #f3f6fb;
}

.sidebar {
  background: #0f172a;
  overflow: hidden;
  transition: width 0.25s;
  box-shadow: 6px 0 18px rgba(15, 23, 42, 0.12);
  z-index: 10;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 17px;
  font-weight: bold;
  background: #111827;
  overflow: hidden;
  white-space: nowrap;
}

.logo-mark {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: 8px;
  background: #2563eb;
  font-size: 18px;
}

.logo-text {
  letter-spacing: 0;
}

.sidebar :deep(.el-menu) {
  border-right: none;
  padding: 10px 8px;
}

.sidebar :deep(.el-menu-item),
.sidebar :deep(.el-sub-menu__title) {
  height: 44px;
  margin: 3px 0;
  border-radius: 8px;
}

.sidebar :deep(.el-menu-item:hover),
.sidebar :deep(.el-sub-menu__title:hover) {
  background: rgba(148, 163, 184, 0.14);
}

.sidebar :deep(.el-menu-item.is-active) {
  background: #2563eb;
  color: #fff;
}

.sidebar :deep(.el-sub-menu .el-menu-item) {
  min-width: 0;
}

.submenu-title {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-width: 0;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid #e5e7eb;
  padding: 0 20px;
  height: 60px;
  backdrop-filter: blur(10px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #475569;
  transition: color 0.2s;
}
.collapse-btn:hover { color: #2563eb; }

.breadcrumb {
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}
.user-dropdown:hover { background: #f5f7fa; }

.user-name {
  font-size: 14px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  background: #f3f6fb;
  padding: 20px;
  overflow-y: auto;
}

.notif-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 8px;
  transition: background 0.2s;
}
.notif-indicator:hover { background: #f5f7fa; }

.mode-tag {
  font-size: 10px;
  font-weight: 600;
  color: #909399;
  background: #f4f4f5;
  padding: 1px 5px;
  border-radius: 3px;
  letter-spacing: 0;
}

.message-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
}

.message-list {
  min-height: 280px;
}

.message-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #fff;
  margin-bottom: 10px;
}

.message-head,
.message-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.message-head strong {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-card p {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}

.message-meta {
  color: #909399;
  font-size: 12px;
}

.message-pagination {
  margin-top: 12px;
  justify-content: flex-end;
}

.is-active .check-icon {
  margin-left: auto;
  color: #409eff;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-container.is-mobile .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
  }

  .main-container.is-mobile.is-collapsed .sidebar {
    width: 0 !important;
    box-shadow: none;
  }

  .breadcrumb { display: none; }
  .user-name { display: none; }
  .mode-tag { display: none; }
  .main-content { padding: 12px; }
  .header { padding: 0 12px; }
}
</style>
