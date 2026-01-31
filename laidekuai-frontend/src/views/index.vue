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

      <div class="features">
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
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo h1 {
  font-size: 24px;
  color: #409eff;
  margin: 0;
}

.logo .slogan {
  font-size: 14px;
  color: #999;
  margin-left: 10px;
}

.nav {
  flex: 1;
  display: flex;
  gap: 20px;
}

.nav a {
  color: #333;
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav a:hover,
.nav a.router-link-active {
  color: #409eff;
  background: #ecf5ff;
}

.user-actions {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 8px 20px;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
  border: 1px solid #409eff;
  color: #409eff;
}

.btn:hover {
  background: #ecf5ff;
}

.btn-primary {
  background: #409eff;
  color: white;
}

.btn-primary:hover {
  background: #66b1ff;
}

.btn-outline {
  background: white;
  border: 1px solid #409eff;
  color: #409eff;
}

.btn-large {
  padding: 12px 32px;
  font-size: 18px;
  background: #409eff;
  color: white;
  border: none;
}

.btn-large:hover {
  background: #66b1ff;
}

.main {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  width: 100%;
}

.hero {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  margin-bottom: 40px;
}

.hero h2 {
  font-size: 36px;
  color: #333;
  margin-bottom: 16px;
}

.hero p {
  font-size: 18px;
  color: #666;
  margin-bottom: 32px;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.feature {
  background: white;
  padding: 30px;
  border-radius: 8px;
  text-align: center;
}

.feature h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 10px;
}

.feature p {
  color: #666;
  font-size: 14px;
}

.footer {
  background: #333;
  color: white;
  text-align: center;
  padding: 20px;
  margin-top: auto;
}
</style>
