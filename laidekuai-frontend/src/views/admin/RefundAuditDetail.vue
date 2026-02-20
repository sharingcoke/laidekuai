<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import orderApi from '@/api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const order = ref(null)
const rejectReason = ref('')

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

const handleApprove = () => {
  if (!order.value) return
  ElMessageBox.confirm(`确定通过订单 ${order.value.orderNo} 的退款申请吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await orderApi.approveRefundByAdmin(order.value.id)
    if (res.code === 0) {
      ElMessage.success('已通过退款')
      fetchDetail()
    }
  }).catch(() => {})
}

const handleReject = () => {
  if (!order.value) return
  if (!rejectReason.value) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  ElMessageBox.confirm(`确定驳回订单 ${order.value.orderNo} 的退款申请吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await orderApi.rejectRefundByAdmin(order.value.id, rejectReason.value)
    if (res.code === 0) {
      ElMessage.success('已驳回退款')
      fetchDetail()
    }
  }).catch(() => {})
}

const formatStatus = (status) => {
  const map = {
    'REFUNDING': '退款中',
    'REFUNDED': '已退款',
    'PAID': '待发货'
  }
  return map[status] || status
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="refund-detail-page" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>退款审核 #{{ order?.id || '-' }}</h2>
        <p class="page-desc">仅对退款中订单进行审核，避免重复操作。</p>
      </div>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-card class="info-card" shadow="never">
      <div class="info-grid">
        <div class="info-item">
          <span class="label">订单号</span>
          <span class="value">{{ order?.orderNo || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">买家ID</span>
          <span class="value">{{ order?.buyerId || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">金额</span>
          <span class="value">￥{{ order?.totalAmount || '0.00' }}</span>
        </div>
        <div class="info-item">
          <span class="label">状态</span>
          <el-tag type="warning">{{ formatStatus(order?.status) }}</el-tag>
        </div>
        <div class="info-item full">
          <span class="label">申请原因</span>
          <span class="value">{{ order?.refundReason || '暂无记录' }}</span>
        </div>
      </div>
    </el-card>

    <el-card class="action-card" shadow="never">
      <div class="action-row">
        <el-button
          type="success"
          :disabled="order?.status !== 'REFUNDING'"
          @click="handleApprove"
        >
          通过
        </el-button>
        <el-button
          type="danger"
          :disabled="order?.status !== 'REFUNDING'"
          @click="handleReject"
        >
          驳回
        </el-button>
      </div>
      <el-form label-width="80px" class="reject-form">
        <el-form-item label="驳回原因">
          <el-input
            v-model="rejectReason"
            placeholder="请填写驳回原因"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="items-card" shadow="never">
      <div class="section-title">订单明细</div>
      <el-table :data="order?.items || []" stripe border empty-text="暂无订单项">
        <el-table-column prop="goodsTitle" label="商品" min-width="180" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="price" label="单价" width="120" />
        <el-table-column prop="itemStatus" label="状态" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.refund-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.info-card,
.action-card,
.items-card {
  border-radius: 8px;
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
  font-size: 14px;
  font-weight: 500;
}

.muted {
  color: #b1b3b8;
}

.action-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.reject-form {
  max-width: 560px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}
</style>
