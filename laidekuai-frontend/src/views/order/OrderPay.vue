<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import orderApi from '@/api/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref(null)
const payMethod = ref('alipay')

const fetchOrder = async () => {
  loading.value = true
  try {
    const res = await orderApi.detail(route.params.id)
    if (res.code === 0) {
      order.value = res.data
    }
  } catch (error) {
    console.error('获取订单失败:', error)
  } finally {
    loading.value = false
  }
}

const confirmPay = async () => {
  if (!order.value) return
  const res = await orderApi.pay(order.value.id)
  if (res.code === 0) {
    ElMessage.success('支付成功')
    router.push('/orders')
  }
}

const cancelPay = () => {
  router.push('/orders')
}

onMounted(() => {
  fetchOrder()
})
</script>

<template>
  <div class="pay-page" v-loading="loading">
    <div v-if="order" class="pay-card">
      <h2>订单支付</h2>
      <div class="info">
        <div><span class="label">订单号:</span> {{ order.orderNo }}</div>
        <div><span class="label">金额:</span> ￥{{ order.totalAmount }}</div>
      </div>
      <div class="method">
        <div class="label">支付方式:</div>
        <el-radio-group v-model="payMethod">
          <el-radio label="alipay">支付宝</el-radio>
          <el-radio label="wechat">微信</el-radio>
          <el-radio label="bank">银行卡</el-radio>
        </el-radio-group>
      </div>
      <div class="actions">
        <el-button type="primary" @click="confirmPay">确认支付</el-button>
        <el-button @click="cancelPay">取消返回</el-button>
      </div>
      <div class="tips">提示：V1 为模拟支付，确认后订单状态变更为 PAID。</div>
    </div>
  </div>
</template>

<style scoped>
.pay-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 30px 20px;
}

.pay-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px 28px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.08);
}

.pay-card h2 {
  margin: 0 0 16px;
  font-size: 22px;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
  color: #374151;
}

.label {
  color: #6b7280;
  margin-right: 6px;
}

.method {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.tips {
  font-size: 12px;
  color: #9ca3af;
}
</style>
