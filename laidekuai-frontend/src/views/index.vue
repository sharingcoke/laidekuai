<script setup>
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>

<template>
  <div class="home-page">
    <!-- 头部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <h1>来得快</h1>
          <span class="slogan">二手交易平台</span>
        </div>

        <nav class="nav">
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/goods">商品列表</RouterLink>
          <el-dropdown v-if="authStore.isAdmin" class="admin-dropdown" trigger="hover">
            <span class="nav-link admin-link">
              管理端
              <span class="caret">▾</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <RouterLink to="/admin/orders">订单管理</RouterLink>
                </el-dropdown-item>
                <el-dropdown-item>
                  <RouterLink to="/admin/goods">商品审核</RouterLink>
                </el-dropdown-item>
                <el-dropdown-item>
                  <RouterLink to="/admin/users">用户管理</RouterLink>
                </el-dropdown-item>
                <el-dropdown-item>
                  <RouterLink to="/admin/reviews">评价管理</RouterLink>
                </el-dropdown-item>
                <el-dropdown-item>
                  <RouterLink to="/admin/categories">分类管理</RouterLink>
                </el-dropdown-item>
                <el-dropdown-item>
                  <RouterLink to="/admin/refunds">退款审核</RouterLink>
                </el-dropdown-item>
                <el-dropdown-item>
                  <RouterLink to="/admin/audit-logs">审计日志</RouterLink>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>

        <div class="user-actions">
          <template v-if="authStore.isLoggedIn">
            <RouterLink to="/cart" class="btn">购物车</RouterLink>
            <RouterLink to="/orders" class="btn">我的订单</RouterLink>
            <RouterLink to="/profile" class="btn">
              {{ authStore.user?.nickName || '个人中心' }}
            </RouterLink>
          </template>
          <template v-else>
            <RouterLink to="/login" class="btn btn-primary">登录</RouterLink>
            <RouterLink to="/register" class="btn btn-outline">注册</RouterLink>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main">
      <div class="hero">
        <h2>欢迎来到来得快</h2>
        <p>安全、便捷的二手交易平台</p>
        <RouterLink to="/goods" class="btn btn-large">开始购物</RouterLink>
      </div>

      <div class="features stagger-enter">
        <div class="feature">
          <h3>买卖一体</h3>
          <p>所有登录用户均可发布商品</p>
        </div>
        <div class="feature">
          <h3>安全交易</h3>
          <p>平台担保，资金安全</p>
        </div>
        <div class="feature">
          <h3>快速发货</h3>
          <p>卖家直接发货，快速到达</p>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <p>&copy; 2026 来得快 - 二手交易平台</p>
    </footer>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  border-bottom: 1px solid var(--ldk-border);
  box-shadow: none;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  width: min(1240px, 100%);
  margin: 0 auto;
  padding: 0 24px;
  height: 70px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.logo h1 {
  font-size: 25px;
  color: var(--ldk-primary);
  margin: 0;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.logo .slogan {
  font-size: 12px;
  color: var(--ldk-text-secondary);
  margin-left: 8px;
}

.nav {
  flex: 1;
  display: flex;
  gap: 8px;
  align-items: center;
}

.nav a,
.nav-link {
  color: var(--ldk-text-regular);
  font-size: 15px;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.25s;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.nav a:hover,
.nav a.router-link-active,
.nav-link:hover {
  color: var(--ldk-primary);
  background: var(--ldk-primary-soft);
}

.admin-dropdown {
  display: inline-flex;
  align-items: center;
}

.admin-link {
  cursor: pointer;
}

.caret {
  font-size: 12px;
  color: var(--ldk-text-secondary);
}

.admin-dropdown :deep(.el-dropdown-menu__item a) {
  color: inherit;
  display: block;
  width: 100%;
}

.user-actions {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.25s;
  border: 1px solid var(--ldk-border-strong);
  color: var(--ldk-text-regular);
  background: #fff;
  font-weight: 500;
}

.btn:hover {
  border-color: var(--ldk-primary-border);
  background: #f5f8ff;
}

.btn-primary {
  background: var(--ldk-primary);
  color: white;
  border-color: var(--ldk-primary);
}

.btn-primary:hover {
  background: #335dc4;
  border-color: #335dc4;
}

.btn-outline {
  color: var(--ldk-primary);
  border-color: var(--ldk-primary-border);
  background: #f5f8ff;
}

.btn-large {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 26px;
  font-size: 15px;
  background: var(--ldk-primary);
  color: white;
  border: 1px solid var(--ldk-primary);
  border-radius: 10px;
}

.btn-large:hover {
  background: #335dc4;
  border-color: #335dc4;
}

.main {
  flex: 1;
  width: min(1240px, 100%);
  margin: 0 auto;
  padding: 40px 24px;
}

.hero {
  text-align: center;
  padding: 52px 20px;
  background: #fff;
  border: 1px solid var(--ldk-border);
  border-radius: 14px;
  margin-bottom: 24px;
  box-shadow: var(--ldk-shadow-sm);
}

.hero h2 {
  font-size: 36px;
  color: var(--ldk-text-primary);
  margin-bottom: 12px;
}

.hero p {
  font-size: 16px;
  color: var(--ldk-text-secondary);
  margin-bottom: 22px;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.feature {
  background: #fff;
  border: 1px solid var(--ldk-border);
  box-shadow: none;
  padding: 24px 22px;
  border-radius: 12px;
  text-align: center;
}

.feature h3 {
  font-size: 20px;
  color: var(--ldk-text-primary);
  margin-bottom: 8px;
}

.feature p {
  color: var(--ldk-text-secondary);
  font-size: 14px;
}

.footer {
  border-top: 1px solid var(--ldk-border);
  background: transparent;
  color: var(--ldk-text-secondary);
  text-align: center;
  padding: 16px;
  margin-top: auto;
}

@media (max-width: 900px) {
  .header-content {
    height: auto;
    padding: 12px 14px;
    flex-wrap: wrap;
    gap: 12px;
  }

  .nav,
  .user-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .main {
    padding: 20px 14px 28px;
  }

  .hero {
    padding: 36px 18px;
  }

  .hero h2 {
    font-size: 32px;
  }
}
</style>
