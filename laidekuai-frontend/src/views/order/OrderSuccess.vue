<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const orders = ref([])

const loadOrders = () => {
  const raw = sessionStorage.getItem('orderSuccess')
  if (!raw) {
    orders.value = []
    return
  }
  try {
    const parsed = JSON.parse(raw)
    orders.value = Array.isArray(parsed) ? parsed : []
  } catch (error) {
    orders.value = []
  }
}

const hasOrders = computed(() => orders.value.length > 0)

const formatStatus = (status) => {
  const map = {
    PENDING_PAY: '待支付',
    PAID: '待发货',
    SHIPPED: '待收货',
    COMPLETED: '已完成',
    CANCELED: '已取消',
    REFUNDING: '退款中',
    REFUNDED: '已退款',
    DISPUTED: '争议中'
  }
  return map[status] || status
}

const goPay = (orderId) => {
  router.push(`/order/pay/${orderId}`)
}

const goDetail = (orderId) => {
  router.push(`/orders/${orderId}`)
}

const goOrders = () => {
  router.push('/orders')
}

onMounted(() => {
  loadOrders()
})
</script>

<template>
  <div class="order-success-page page-shell">
    <div class="page-header">
      <div>
        <h2>下单成功</h2>
        <p class="page-desc">已拆分为 {{ orders.length }} 个订单，请分别支付。</p>
      </div>
      <el-button @click="goOrders">查看我的订单</el-button>
    </div>

    <el-empty v-if="!hasOrders" description="暂无可展示的订单">
      <el-button type="primary" @click="goOrders">返回订单列表</el-button>
    </el-empty>

    <div v-else class="success-list">
      <el-card v-for="order in orders" :key="order.id" class="panel-card success-card" shadow="never">
        <div class="card-head">
          <div class="order-no">订单号 {{ order.orderNo }}</div>
          <el-tag type="info">{{ formatStatus(order.status || 'PENDING_PAY') }}</el-tag>
        </div>
        <div class="card-body">
          <div class="seller">卖家：{{ order.sellerName || `卖家${order.sellerId || ''}` }}</div>
          <div class="amount">金额：￥{{ order.totalAmount }}</div>
        </div>
        <div class="card-actions">
          <el-button type="primary" @click="goPay(order.id)">去支付</el-button>
          <el-button @click="goDetail(order.id)">订单详情</el-button>
        </div>
      </el-card>
    </div>

    <div class="tips">提示：15 分钟未支付自动取消并释放库存。</div>
  </div>
</template>

<style scoped>
.order-success-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.success-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.success-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.order-no {
  font-weight: 600;
  color: var(--ldk-text-primary);
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: var(--ldk-text-regular);
}

.amount {
  font-weight: 600;
  color: var(--ldk-danger);
}

.card-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tips {
  font-size: 12px;
  color: #95a0b3;
}
</style>
