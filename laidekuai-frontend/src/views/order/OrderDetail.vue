<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import orderApi from '@/api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const order = ref(null)

const isDisputed = computed(() => order.value?.status === 'DISPUTED')
const disputeAppliedAt = computed(() => order.value?.disputeTime || order.value?.createdAt || '-')

const hasShippedItems = computed(() => {
  return (order.value?.items || []).some(item => item.itemStatus === 'SHIPPED' || item.itemStatus === 'COMPLETED')
})

const canRefund = computed(() => {
  return order.value?.status === 'PAID' && !hasShippedItems.value
})

const showRefundDetail = computed(() => {
  if (!order.value) return false
  return order.value.status === 'REFUNDING'
    || order.value.status === 'REFUNDED'
    || (order.value.refundRequestCount || 0) > 0
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await orderApi.detail(route.params.id)
    if (res.code === 0) {
      order.value = res.data
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePay = () => {
  if (!order.value) return
  router.push(`/order/pay/${order.value.id}`)
}

const handleCancel = async () => {
  if (!order.value) return
  try {
    await ElMessageBox.confirm('确认要取消订单吗？', '提示', { type: 'warning' })
    const res = await orderApi.cancel(order.value.id, '用户主动取消')
    if (res.code === 0) {
      ElMessage.success('订单已取消')
      fetchDetail()
    }
  } catch (error) {}
}

const handleReceive = async () => {
  if (!order.value) return
  try {
    await ElMessageBox.confirm('确认已收到货物吗？', '提示', { type: 'warning' })
    const res = await orderApi.receive(order.value.id)
    if (res.code === 0) {
      ElMessage.success('确认收货成功')
      fetchDetail()
    }
  } catch (error) {}
}

const handleRefund = async () => {
  if (!order.value) return
  if (!canRefund.value) {
    ElMessage.warning('订单已发货，无法申请退款')
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入退款原因', '申请退款', {
      confirmButtonText: '提交',
      cancelButtonText: '取消'
    })
    if (value) {
      const res = await orderApi.refund(order.value.id, value)
      if (res.code === 0) {
        ElMessage.success('退款申请已提交')
        fetchDetail()
      }
    }
  } catch (error) {}
}

const handleRefundDetail = () => {
  if (!order.value) return
  router.push(`/orders/${order.value.id}/refund`)
}

const handleDisputeDetail = () => {
  ElMessage.info('争议模块尚未实现')
}

const formatStatus = (status) => {
  const map = {
    PENDING_PAY: '待支付',
    PAID: '待发货',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELED: '已取消',
    REFUNDING: '退款中',
    REFUNDED: '已退款',
    DISPUTED: '争议中'
  }
  return map[status] || status
}

const formatItemStatus = (status) => {
  const map = {
    PENDING_PAY: '待支付',
    PAID: '待发货',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELED: '已取消',
    REFUNDING: '退款中',
    REFUNDED: '已退款',
    DISPUTED: '争议中'
  }
  return map[status] || status
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="order-detail-page page-shell" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>订单详情</h2>
        <p class="page-desc">订单信息、物流与操作集中展示。</p>
      </div>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-card v-if="order" class="panel-card info-card" shadow="never">
      <div class="info-grid">
        <div class="info-item">
          <span class="label">订单号</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="info-item">
          <span class="label">状态</span>
          <el-tag>{{ formatStatus(order.status) }}</el-tag>
        </div>
        <div class="info-item">
          <span class="label">金额</span>
          <span class="value">￥{{ order.totalAmount }}</span>
        </div>
        <div class="info-item">
          <span class="label">卖家</span>
          <span class="value">{{ order.sellerName || `卖家${order.sellerId || ''}` }}</span>
        </div>
        <div class="info-item">
          <span class="label">创建时间</span>
          <span class="value">{{ order.createdAt || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付时间</span>
          <span class="value">{{ order.payTime || '-' }}</span>
        </div>
        <div class="info-item full">
          <span class="label">收货地址</span>
          <span class="value">{{ order.receiverName }} {{ order.receiverPhone }} {{ order.receiverAddress }}</span>
        </div>
        <div class="info-item full">
          <span class="label">买家备注</span>
          <span class="value">{{ order.remark || '无' }}</span>
        </div>
        <div class="info-item full" v-if="order.status === 'CANCELED'">
          <span class="label">取消原因</span>
          <span class="value">{{ order.cancelReason || '无' }}</span>
        </div>
      </div>
    </el-card>

    <el-card v-if="order" class="panel-card action-card" shadow="never">
      <div class="action-row">
        <el-button v-if="order.status === 'PENDING_PAY'" type="primary" @click="handlePay">去支付</el-button>
        <el-button v-if="order.status === 'PENDING_PAY'" @click="handleCancel">取消订单</el-button>
        <el-button v-if="order.status === 'SHIPPED'" type="success" @click="handleReceive">确认收货</el-button>
        <el-button v-if="order.status === 'PAID'" :disabled="!canRefund" @click="handleRefund">申请退款</el-button>
        <el-button v-if="showRefundDetail" @click="handleRefundDetail">退款详情</el-button>
        <el-button v-if="isDisputed" @click="handleDisputeDetail">查看争议详情</el-button>
      </div>
      <div v-if="order.status === 'REFUNDING'" class="muted">退款审核中，等待卖家或管理员处理。</div>
      <div v-if="order.status === 'DISPUTED'" class="muted">争议处理中，等待管理员裁决。</div>
    </el-card>

    <el-card v-if="order" class="panel-card items-card" shadow="never">
      <div class="section-title">商品清单</div>
      <el-table :data="order.items || []" border stripe empty-text="暂无订单商品">
        <el-table-column prop="goodsTitle" label="商品" min-width="200" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="price" label="单价" width="120">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="小计" width="120">
          <template #default="{ row }">￥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="itemStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ formatItemStatus(row.itemStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="物流信息" min-width="180">
          <template #default="{ row }">
            <div v-if="row.shipCompany || row.trackingNo" class="ship-info">
              <div>{{ row.shipCompany || '-' }}</div>
              <div class="muted">单号: {{ row.trackingNo || '-' }}</div>
            </div>
            <span v-else class="muted">暂无</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="order && isDisputed" class="panel-card dispute-card" shadow="never">
      <div class="section-title">争议处理</div>
      <div class="dispute-grid">
        <div class="dispute-item">
          <span class="label">争议原因</span>
          <span class="value">{{ order.refundReason || '暂无记录' }}</span>
        </div>
        <div class="dispute-item">
          <span class="label">申请时间</span>
          <span class="value">{{ disputeAppliedAt }}</span>
        </div>
        <div class="dispute-item">
          <span class="label">退款次数</span>
          <span class="value">{{ order.refundRequestCount ?? 0 }} 次</span>
        </div>
      </div>
      <div class="dispute-tip">⚠️ 争议处理中，当前订单无法继续履约。</div>
    </el-card>
  </div>
</template>

<style scoped>
.order-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: #303133;
}

.info-item.full {
  grid-column: span 4;
}

.label {
  color: #909399;
  font-size: 12px;
}

.value {
  color: #303133;
  font-weight: 500;
}

.section-title {
  margin-bottom: 12px;
  font-weight: 600;
  color: #303133;
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.ship-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.muted {
  color: #b1b3b8;
  font-size: 12px;
}

.dispute-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px 16px;
}

.dispute-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dispute-tip {
  margin-top: 12px;
  color: #e6a23c;
  font-size: 12px;
}

@media (max-width: 992px) {
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .info-item.full {
    grid-column: span 2;
  }

  .dispute-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-item.full {
    grid-column: span 1;
  }

  .dispute-grid {
    grid-template-columns: 1fr;
  }
}
</style>
