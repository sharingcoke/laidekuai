<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import orderApi from '@/api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const order = ref(null)

const shipDialogVisible = ref(false)
const shipForm = reactive({
  itemId: null,
  shipCompany: '',
  trackingNo: ''
})

const isDisputed = computed(() => order.value?.status === 'DISPUTED')
const disputeAppliedAt = computed(() => order.value?.disputeTime || order.value?.createdAt || '-')

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

const openShipDialog = (item) => {
  shipForm.itemId = item.id
  shipForm.shipCompany = ''
  shipForm.trackingNo = ''
  shipDialogVisible.value = true
}

const submitShip = async () => {
  if (!shipForm.shipCompany || !shipForm.trackingNo) {
    ElMessage.error('请填写物流公司和单号')
    return
  }
  try {
    const res = await orderApi.shipItemByAdmin(shipForm.itemId, {
      shipCompany: shipForm.shipCompany,
      trackingNo: shipForm.trackingNo
    })
    if (res.code === 0) {
      ElMessage.success('代发货成功')
      shipDialogVisible.value = false
      fetchDetail()
    }
  } catch (error) {
    console.error('代发货失败:', error)
  }
}

const goRefundAudit = () => {
  if (!order.value) return
  router.push(`/admin/refunds/${order.value.id}`)
}

const handleDisputeDetail = () => {
  ElMessage.info('争议模块尚未实现')
}

const handleDisputeResolve = () => {
  ElMessage.info('争议模块尚未实现')
}

const canShip = (item) => {
  return order.value?.status === 'PAID' && item.itemStatus === 'PAID'
}

const formatOrderStatus = (status) => {
  const map = {
    'PENDING_PAY': '待支付',
    'PAID': '待发货',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELED': '已取消',
    'REFUNDING': '退款中',
    'REFUNDED': '已退款',
    'DISPUTED': '争议中'
  }
  return map[status] || status
}

const formatItemStatus = (status) => {
  const map = {
    'PENDING_PAY': '待支付',
    'PAID': '待发货',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELED': '已取消',
    'REFUNDING': '退款中',
    'REFUNDED': '已退款',
    'DISPUTED': '争议中'
  }
  return map[status] || status
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="admin-order-detail page-shell" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>管理员订单详情</h2>
        <p class="page-desc">订单详情、履约操作与争议处理信息集中查看。</p>
      </div>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-card class="panel-card info-card" shadow="never" v-if="order">
      <div class="info-grid">
        <div class="info-item">
          <span class="label">订单号</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="info-item">
          <span class="label">状态</span>
          <el-tag>{{ formatOrderStatus(order.status) }}</el-tag>
        </div>
        <div class="info-item">
          <span class="label">金额</span>
          <span class="value">￥{{ order.totalAmount }}</span>
        </div>
        <div class="info-item">
          <span class="label">买家</span>
          <span class="value">{{ order.buyerName || `用户${order.buyerId}` }}</span>
        </div>
        <div class="info-item">
          <span class="label">卖家</span>
          <span class="value">{{ order.sellerName || `用户${order.sellerId}` }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付时间</span>
          <span class="value">{{ order.payTime || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">创建时间</span>
          <span class="value">{{ order.createdAt || '-' }}</span>
        </div>
        <div class="info-item full">
          <span class="label">收货信息</span>
          <span class="value">{{ order.receiverName }} {{ order.receiverPhone }} {{ order.receiverAddress }}</span>
        </div>
        <div class="info-item full">
          <span class="label">买家留言</span>
          <span class="value">{{ order.remark || '无' }}</span>
        </div>
      </div>
    </el-card>

    <el-card class="panel-card action-card" shadow="never" v-if="order">
      <div class="action-row">
        <el-button
          v-if="order.status === 'REFUNDING'"
          type="warning"
          @click="goRefundAudit"
        >
          退款审核
        </el-button>
        <el-button v-if="isDisputed" @click="handleDisputeDetail">查看争议详情</el-button>
        <el-button v-if="isDisputed" type="danger" @click="handleDisputeResolve">争议裁决</el-button>
      </div>
      <div v-if="!isDisputed && order.status !== 'REFUNDING'" class="muted">
        当前状态暂无可执行的管理操作。
      </div>
    </el-card>

    <el-card class="panel-card items-card" shadow="never" v-if="order">
      <div class="section-title">订单明细</div>
      <el-table :data="order.items || []" border stripe empty-text="暂无订单项">
        <el-table-column prop="goodsTitle" label="商品" min-width="200" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="price" label="单价" width="120">
          <template #default="{ row }">
            ￥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="小计" width="120">
          <template #default="{ row }">
            ￥{{ row.amount }}
          </template>
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
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button
              v-if="canShip(row)"
              size="small"
              type="primary"
              @click="openShipDialog(row)"
            >
              代发货
            </el-button>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="panel-card audit-card" shadow="never" v-if="order">
      <div class="section-title">审计记录</div>
      <div class="muted">暂无审计记录</div>
    </el-card>

    <el-card class="panel-card dispute-card" shadow="never" v-if="order && isDisputed">
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
      <div class="dispute-history">
        <span class="label">退款历史</span>
        <div class="muted">暂无记录</div>
      </div>
      <div class="dispute-tip">⚠️ 争议处理需等待管理员裁决，当前订单无法继续履约。</div>
    </el-card>

    <el-dialog v-model="shipDialogVisible" title="代发货" width="420px">
      <el-form label-width="90px" class="dialog-form">
        <el-form-item label="物流公司">
          <el-input v-model="shipForm.shipCompany" placeholder="请输入物流公司" />
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-order-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card,
.items-card,
.action-card,
.dispute-card,
.audit-card {
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

.ship-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.muted {
  color: #b1b3b8;
  font-size: 12px;
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
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

.dispute-history {
  margin-top: 12px;
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
