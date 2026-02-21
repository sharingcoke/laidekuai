<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import orderApi from '@/api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const order = ref(null)

const refundStatus = computed(() => {
  if (!order.value) return '-'
  if (order.value.status === 'REFUNDING') return '退款中'
  if (order.value.status === 'REFUNDED') return '已退款'
  if (order.value.status === 'PAID' && (order.value.refundRequestCount || 0) > 0) return '驳回'
  return order.value.status || '-'
})

const applyTime = computed(() => {
  if (!order.value) return '-'
  return order.value.updatedAt || order.value.createdAt || '-'
})

const records = computed(() => {
  if (!order.value) return []
  const list = []
  if ((order.value.refundRequestCount || 0) > 0) {
    list.push({ time: applyTime.value, text: '买家提交退款申请' })
  }
  if (order.value.status === 'REFUNDING') {
    list.push({ time: '-', text: '卖家审核中' })
  }
  if (order.value.status === 'REFUNDED') {
    list.push({ time: order.value.cancelTime || order.value.updatedAt || '-', text: '管理员通过' })
  }
  if (order.value.status === 'PAID' && (order.value.refundRequestCount || 0) > 0) {
    list.push({ time: order.value.updatedAt || '-', text: '卖家/管理员驳回' })
  }
  return list
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await orderApi.detail(route.params.id)
    if (res.code === 0) {
      order.value = res.data
    }
  } catch (error) {
    console.error('获取退款详情失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="refund-detail-page page-shell" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>退款详情</h2>
        <p class="page-desc">查看退款状态、原因与处理记录。</p>
      </div>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-card v-if="order" class="panel-card info-card" shadow="never">
      <div class="info-grid">
        <div class="info-item">
          <span class="label">状态</span>
          <el-tag>{{ refundStatus }}</el-tag>
        </div>
        <div class="info-item">
          <span class="label">原因</span>
          <span class="value">{{ order.refundReason || '暂无记录' }}</span>
        </div>
        <div class="info-item">
          <span class="label">申请时间</span>
          <span class="value">{{ applyTime }}</span>
        </div>
      </div>
    </el-card>

    <el-card v-if="order" class="panel-card record-card" shadow="never">
      <div class="section-title">处理记录</div>
      <div v-if="records.length === 0" class="muted">暂无处理记录</div>
      <div v-else class="record-list">
        <div v-for="(record, index) in records" :key="index" class="record-item">
          <span class="record-time">{{ record.time }}</span>
          <span class="record-text">{{ record.text }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.refund-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: #303133;
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

.record-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.record-item {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #606266;
}

.record-time {
  min-width: 160px;
  color: #909399;
}

.muted {
  color: #b1b3b8;
  font-size: 12px;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
