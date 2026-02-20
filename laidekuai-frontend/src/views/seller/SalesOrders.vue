<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import orderApi from '@/api/order'

const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const activeTab = ref('all')

const queryParams = reactive({
  page: 1,
  size: 10,
  status: ''
})

const shipDialogVisible = ref(false)
const shipForm = reactive({
  itemId: null,
  shipCompany: '',
  trackingNo: ''
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.listSeller(queryParams)
    if (res.code === 0) {
      orderList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取卖家订单失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTabClick = (tab) => {
  queryParams.status = tab.props.name === 'all' ? '' : tab.props.name
  queryParams.page = 1
  fetchOrders()
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchOrders()
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
    const res = await orderApi.shipItem(shipForm.itemId, {
      shipCompany: shipForm.shipCompany,
      trackingNo: shipForm.trackingNo
    })
    if (res.code === 0) {
      ElMessage.success('发货成功')
      shipDialogVisible.value = false
      fetchOrders()
    }
  } catch (error) {
    console.error('发货失败:', error)
  }
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
    'REFUNDED': '已退款'
  }
  return map[status] || status
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="seller-orders-page page-shell">
    <div class="page-header">
      <div>
        <h2>卖家订单</h2>
        <p class="page-desc">聚焦待发货与履约进度，明细与操作区间距统一。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="order-tabs panel-card tabs-panel">
      <el-tab-pane label="全部订单" name="all"></el-tab-pane>
      <el-tab-pane label="待发货" name="PAID"></el-tab-pane>
      <el-tab-pane label="已发货" name="SHIPPED"></el-tab-pane>
      <el-tab-pane label="已完成" name="COMPLETED"></el-tab-pane>
    </el-tabs>

    <div class="order-list" v-loading="loading">
      <el-empty v-if="!loading && orderList.length === 0" description="暂无订单" />

      <div v-else class="order-item" v-for="order in orderList" :key="order.id">
        <div class="order-header">
          <span class="order-no">订单号: {{ order.orderNo }}</span>
          <span class="create-time">{{ order.createdAt }}</span>
          <el-tag class="status-tag">{{ formatOrderStatus(order.status) }}</el-tag>
        </div>

        <div class="order-body">
          <div class="order-goods-list">
            <div class="goods-item" v-for="item in order.items" :key="item.id">
              <div class="goods-detail">
                <div class="goods-title">{{ item.goodsTitle }}</div>
                <div class="goods-meta">数量: {{ item.quantity }}</div>
              </div>
              <div class="item-status">
                <el-tag type="info">{{ formatItemStatus(item.itemStatus) }}</el-tag>
              </div>
              <div class="item-actions">
                <el-button
                  v-if="order.status === 'PAID' && item.itemStatus === 'PAID'"
                  type="primary"
                  size="small"
                  @click="openShipDialog(item)"
                >
                  发货
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        layout="prev, pager, next, total"
        :total="total"
        @current-change="handleCurrentChange"
        background
      />
    </div>

    <el-dialog v-model="shipDialogVisible" title="订单项发货" width="420px">
      <el-form label-width="96px" class="form-standard dialog-form">
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
.order-tabs {
  margin-bottom: 16px;
}

.order-item {
  background: white;
  border: 1px solid var(--ldk-border);
  margin-bottom: 14px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--ldk-shadow-sm);
}

.order-header {
  padding: 12px 18px;
  background: #f9fbff;
  border-bottom: 1px solid var(--ldk-border);
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--ldk-text-secondary);
  gap: 16px;
  flex-wrap: wrap;
}

.status-tag {
  margin-left: auto;
}

.order-body {
  padding: 16px 18px;
}

.goods-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed var(--ldk-border);
}

.goods-item:last-child {
  border-bottom: none;
}

.goods-detail {
  flex: 1;
}

.goods-title {
  font-size: 14px;
  color: var(--ldk-text-primary);
  font-weight: 500;
  margin-bottom: 4px;
}

.goods-meta {
  color: #999;
  font-size: 12px;
}

.item-status {
  width: 100px;
  text-align: center;
}

.item-actions {
  width: 120px;
  text-align: right;
}

.pagination-container {
  justify-content: center;
}

@media (max-width: 768px) {
  .goods-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .item-status,
  .item-actions {
    width: 100%;
    text-align: left;
  }
}
</style>
