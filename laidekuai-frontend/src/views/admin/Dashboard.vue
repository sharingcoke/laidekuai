<script setup>
import { ref, onMounted } from 'vue'
import orderApi from '@/api/order'
import goodsApi from '@/api/goods'
import adminUserApi from '@/api/adminUser'

const loading = ref(false)
const summary = ref({
  orders: '--',
  goods: '--',
  users: '--'
})

const fetchSummary = async () => {
  loading.value = true
  try {
    const [ordersRes, goodsRes, usersRes] = await Promise.all([
      orderApi.listAdmin({ page: 1, size: 1 }),
      goodsApi.listAdmin({ page: 1, size: 1 }),
      adminUserApi.list({ page: 1, size: 1 })
    ])

    if (ordersRes.code === 0) {
      summary.value.orders = ordersRes.data?.total ?? 0
    }
    if (goodsRes.code === 0) {
      summary.value.goods = goodsRes.data?.total ?? 0
    }
    if (usersRes.code === 0) {
      summary.value.users = usersRes.data?.total ?? 0
    }
  } catch (error) {
    console.error('获取管理员概览失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSummary()
})
</script>

<template>
  <div class="admin-dashboard page-shell" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>管理员首页</h2>
        <p class="page-desc">平台运营概览与快捷入口。</p>
      </div>
    </div>

    <div class="summary-grid">
      <el-card class="panel-card summary-card" shadow="never">
        <div class="label">订单总数</div>
        <div class="value">{{ summary.orders }}</div>
        <div class="hint">最近更新以订单列表为准</div>
      </el-card>
      <el-card class="panel-card summary-card" shadow="never">
        <div class="label">商品总数</div>
        <div class="value">{{ summary.goods }}</div>
        <div class="hint">统计包含已下架商品</div>
      </el-card>
      <el-card class="panel-card summary-card" shadow="never">
        <div class="label">用户总数</div>
        <div class="value">{{ summary.users }}</div>
        <div class="hint">包括管理员与普通用户</div>
      </el-card>
    </div>

    <el-card class="panel-card quick-card" shadow="never">
      <div class="section-title">快捷入口</div>
      <div class="quick-links">
        <router-link to="/admin/orders">订单管理</router-link>
        <router-link to="/admin/goods">商品审核</router-link>
        <router-link to="/admin/users">用户管理</router-link>
        <router-link to="/admin/refunds">退款审核</router-link>
        <router-link to="/admin/disputes">争议处理</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.summary-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-card .label {
  color: var(--ldk-text-secondary);
  font-size: 13px;
}

.summary-card .value {
  font-size: 28px;
  font-weight: 700;
  color: var(--ldk-text-primary);
}

.summary-card .hint {
  font-size: 12px;
  color: #95a0b3;
}

.quick-card .quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-card a {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid var(--ldk-border);
  color: var(--ldk-text-regular);
  transition: all 0.2s ease;
}

.quick-card a:hover {
  color: var(--ldk-primary);
  border-color: var(--ldk-primary);
}
</style>
