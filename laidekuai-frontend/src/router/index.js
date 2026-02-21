import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

/**
 * 路由配置
 */
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/index.vue'),
    meta: { title: '首页 - 来得快' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: { title: '登录 - 来得快', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: { title: '注册 - 来得快', requiresAuth: false }
  },
  {
    path: '/goods',
    name: 'GoodsList',
    component: () => import('@/views/goods/GoodsList.vue'),
    meta: { title: '商品列表 - 来得快' }
  },
  {
    path: '/goods/:id',
    name: 'GoodsDetail',
    component: () => import('@/views/goods/GoodsDetail.vue'),
    meta: { title: '商品详情 - 来得快' }
  },
  {
    path: '/notices/:id',
    name: 'NoticeDetail',
    component: () => import('@/views/notice/NoticeDetail.vue'),
    meta: { title: '公告详情 - 来得快' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/cart/CartList.vue'),
    meta: { title: '购物车 - 来得快', requiresAuth: true }
  },
  {
    path: '/order/confirm',
    name: 'OrderConfirm',
    component: () => import('@/views/order/OrderConfirm.vue'),
    meta: { title: '确认订单 - 来得快', requiresAuth: true }
  },
  {
    path: '/order/pay/:id',
    name: 'OrderPay',
    component: () => import('@/views/order/OrderPay.vue'),
    meta: { title: '订单支付 - 来得快', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('@/views/order/OrderList.vue'),
    meta: { title: '我的订单 - 来得快', requiresAuth: true }
  },
  {
    path: '/reviews/my',
    name: 'MyReviews',
    component: () => import('@/views/review/ReviewList.vue'),
    meta: { title: '我的评价 - 来得快', requiresAuth: true }
  },
  {
    path: '/reviews/new',
    name: 'ReviewForm',
    component: () => import('@/views/review/ReviewForm.vue'),
    meta: { title: '创建评价 - 来得快', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/user/Profile.vue'),
    meta: { title: '个人中心 - 来得快', requiresAuth: true }
  },
  {
    path: '/addresses',
    name: 'AddressManage',
    component: () => import('@/views/user/AddressManage.vue'),
    meta: { title: '地址管理 - 来得快', requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'MessageCenter',
    component: () => import('@/views/user/MessageCenter.vue'),
    meta: { title: '消息中心 - 来得快', requiresAuth: true }
  },
  {
    path: '/my-goods',
    name: 'MyGoods',
    component: () => import('@/views/user/MyGoods.vue'),
    meta: { title: '我发布的商品 - 来得快', requiresAuth: true }
  },
  {
    path: '/goods/edit/:id',
    name: 'GoodsEdit',
    component: () => import('@/views/goods/GoodsPublish.vue'), // 复用发布页面
    meta: { title: '编辑商品 - 来得快', requiresAuth: true }
  },
  {
    path: '/goods/publish',
    name: 'GoodsPublish',
    component: () => import('@/views/goods/GoodsPublish.vue'),
    meta: { title: '发布商品 - 来得快', requiresAuth: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/UserList.vue'),
    meta: { title: '用户管理 - 管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/goods',
    name: 'AdminGoods',
    component: () => import('@/views/admin/GoodsAudit.vue'),
    meta: { title: '商品审核 - 管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/reviews',
    name: 'AdminReviews',
    component: () => import('@/views/admin/ReviewManage.vue'),
    meta: { title: '评价管理 - 管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/categories',
    name: 'AdminCategories',
    component: () => import('@/views/admin/CategoryManage.vue'),
    meta: { title: '分类管理 - 管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: () => import('@/views/admin/OrderManage.vue'),
    meta: { title: '订单管理 - 管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/orders/:id',
    name: 'AdminOrderDetail',
    component: () => import('@/views/admin/OrderDetail.vue'),
    meta: { title: '管理员订单详情 - 管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/refunds',
    name: 'AdminRefunds',
    component: () => import('@/views/admin/RefundAudit.vue'),
    meta: { title: '退款审核 - 管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/audit-logs',
    name: 'AdminAuditLogs',
    component: () => import('@/views/admin/AuditLog.vue'),
    meta: { title: '审计日志 - 管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/refunds/:id',
    name: 'AdminRefundDetail',
    component: () => import('@/views/admin/RefundAuditDetail.vue'),
    meta: { title: '退款审核详情 - 管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/seller/orders',
    name: 'SellerOrders',
    component: () => import('@/views/seller/SalesOrders.vue'),
    meta: { title: '卖家订单 - 来得快', requiresAuth: true, requiresSeller: true }
  },
  {
    path: '/seller/orders/:id',
    name: 'SellerOrderDetail',
    component: () => import('@/views/seller/OrderDetail.vue'),
    meta: { title: '卖家订单详情 - 来得快', requiresAuth: true, requiresSeller: true }
  }
]

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

/**
 * 路由守卫
 */
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '来得快'

  // 检查是否需要登录
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    // 需要登录但未登录，跳转到登录页
    next({
      name: 'Login',
      query: { redirect: to.fullPath }
    })
  } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    // 非管理员访问管理页，回首页
    next({ path: '/' })
  } else if (to.meta.requiresSeller && !authStore.isSeller && !authStore.isAdmin) {
    // 非卖家访问卖家页，回首页
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
