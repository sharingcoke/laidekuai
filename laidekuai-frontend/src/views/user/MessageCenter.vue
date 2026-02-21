<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import orderApi from '@/api/order'
import noticeApi from '@/api/notice'

const authStore = useAuthStore()
const loading = ref(false)
const messageList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const buildMessages = (buyerOrders, sellerOrders, notices) => {
  const messages = []

  buyerOrders.forEach(order => {
    if (order.status === 'SHIPPED') {
      messages.push({
        id: `buyer-${order.id}`,
        content: `订单#${order.orderNo} 已发货，请及时确认收货`,
        time: order.createdAt
      })
    }
    if (order.status === 'REFUNDED') {
      messages.push({
        id: `buyer-refund-${order.id}`,
        content: `订单#${order.orderNo} 退款已完成`,
        time: order.createdAt
      })
    }
  })

  sellerOrders.forEach(order => {
    if (order.status === 'PAID') {
      messages.push({
        id: `seller-${order.id}`,
        content: `订单#${order.orderNo} 待发货`,
        time: order.createdAt
      })
    }
  })

  notices.forEach(notice => {
    messages.push({
      id: `notice-${notice.id}`,
      content: `公告：${notice.title}`,
      time: notice.publishedAt || notice.createdAt
    })
  })

  return messages.sort((a, b) => new Date(b.time) - new Date(a.time))
}

const fetchMessages = async () => {
  loading.value = true
  try {
    const [buyerRes, noticeRes, sellerRes] = await Promise.all([
      orderApi.list({ page: 1, size: 50 }),
      noticeApi.list({ page: 1, size: 20 }),
      authStore.isSeller || authStore.isAdmin ? orderApi.listSeller({ page: 1, size: 50 }) : Promise.resolve({ code: 0, data: { records: [] } })
    ])

    const buyerOrders = buyerRes.code === 0 ? buyerRes.data.records : []
    const sellerOrders = sellerRes.code === 0 ? sellerRes.data.records : []
    const notices = noticeRes.code === 0 ? noticeRes.data.records : []

    const messages = buildMessages(buyerOrders, sellerOrders, notices)
    messageList.value = messages
    total.value = messages.length
  } catch (error) {
    console.error('获取消息失败:', error)
  } finally {
    loading.value = false
  }
}

const pagedMessages = () => {
  const start = (page.value - 1) * size.value
  return messageList.value.slice(start, start + size.value)
}

const handlePageChange = (val) => {
  page.value = val
}

onMounted(() => {
  fetchMessages()
})
</script>

<template>
  <div class="message-center-page">
    <div class="page-header">
      <div>
        <h2>消息中心</h2>
        <p class="page-desc">订单与公告动态集中展示。</p>
      </div>
    </div>

    <div class="message-list" v-loading="loading">
      <el-empty v-if="!loading && messageList.length === 0" description="暂无消息" />

      <div v-else class="message-item" v-for="item in pagedMessages()" :key="item.id">
        <div class="content">{{ item.content }}</div>
        <div class="time">{{ item.time }}</div>
      </div>
    </div>

    <div class="pagination-container" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        layout="prev, pager, next, total"
        :total="total"
        @current-change="handlePageChange"
        background
      />
    </div>
  </div>
</template>

<style scoped>
.message-center-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
}

.page-desc {
  margin: 8px 0 0;
  font-size: 13px;
  color: #909399;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  background: white;
  border-radius: 10px;
  padding: 14px 18px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.content {
  color: #1f2937;
  font-size: 14px;
}

.time {
  color: #9ca3af;
  font-size: 12px;
  white-space: nowrap;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
