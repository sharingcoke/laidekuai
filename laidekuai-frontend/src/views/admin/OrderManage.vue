<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import orderApi from '@/api/order'

const router = useRouter()
const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const activeTab = ref('all')
const createdRange = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  status: '',
  orderNo: '',
  buyerId: '',
  sellerId: '',
  startTime: '',
  endTime: ''
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
    if (createdRange.value && createdRange.value.length === 2) {
      queryParams.startTime = createdRange.value[0]
      queryParams.endTime = createdRange.value[1]
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }

    const params = {
      page: queryParams.page,
      size: queryParams.size,
      status: queryParams.status || undefined,
      order_no: queryParams.orderNo || undefined,
      buyer_id: queryParams.buyerId || undefined,
      seller_id: queryParams.sellerId || undefined,
      start_time: queryParams.startTime || undefined,
      end_time: queryParams.endTime || undefined
    }
    const res = await orderApi.listAdmin(params)
    if (res.code === 0) {
      orderList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取管理员订单失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTabClick = (tab) => {
  queryParams.status = tab.props.name === 'all' ? '' : tab.props.name
  queryParams.page = 1
  fetchOrders()
}

const handleSearch = () => {
  queryParams.page = 1
  fetchOrders()
}

const handleReset = () => {
  queryParams.status = ''
  queryParams.orderNo = ''
  queryParams.buyerId = ''
  queryParams.sellerId = ''
  createdRange.value = []
  queryParams.page = 1
  fetchOrders()
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchOrders()
}

const goDetail = (orderId) => {
  router.push(`/admin/orders/${orderId}`)
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
      fetchOrders()
    }
  } catch (error) {
    console.error('代发货失败:', error)
  }
}

const canShip = (order, item) => {
  return order.status === 'PAID' && item.itemStatus === 'PAID'
}

const formatStatus = (status) => {
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
  <div class="admin-orders-page page-shell">
    <div class="page-header">
      <div>
        <h2>订单管理</h2>
        <p class="page-desc">筛选、展开明细和代发货操作集中在统一后台视图。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="order-tabs panel-card tabs-panel">
      <el-tab-pane label="全部订单" name="all"></el-tab-pane>
      <el-tab-pane label="待发货" name="PAID"></el-tab-pane>
      <el-tab-pane label="已发货" name="SHIPPED"></el-tab-pane>
      <el-tab-pane label="退款中" name="REFUNDING"></el-tab-pane>
    </el-tabs>

    <el-card class="filter-card" shadow="never">
      <el-form class="filter-form" :inline="true" label-width="70px">
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable />
        </el-form-item>
        <el-form-item label="买家ID">
          <el-input v-model="queryParams.buyerId" placeholder="买家ID" clearable />
        </el-form-item>
        <el-form-item label="卖家ID">
          <el-input v-model="queryParams.sellerId" placeholder="卖家ID" clearable />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="createdRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-container table-panel" v-loading="loading">
        <el-table
          :data="orderList"
          style="width: 100%"
          row-key="id"
          stripe
          border
          empty-text="暂无订单"
        >
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="item-list">
                <div class="item-row" v-for="item in row.items" :key="item.id">
                  <div class="item-info">
                    <div class="title">{{ item.goodsTitle }}</div>
                    <div class="meta">数量: {{ item.quantity }} | 单价: {{ item.price }}</div>
                  </div>
                  <div class="item-status">
                    <el-tag type="info">{{ formatItemStatus(item.itemStatus) }}</el-tag>
                  </div>
                  <div class="item-actions">
                    <el-button
                      v-if="canShip(row, item)"
                      type="primary"
                      size="small"
                      @click="openShipDialog(item)"
                    >
                      代发货
                    </el-button>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="orderNo" label="订单号" min-width="200" show-overflow-tooltip />
          <el-table-column prop="buyerId" label="买家ID" width="100" />
          <el-table-column prop="sellerId" label="卖家ID" width="100" />
          <el-table-column prop="totalAmount" label="金额" width="120" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag>{{ formatStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="goDetail(row.id)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>

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
      </div>
    </el-card>

    <el-dialog v-model="shipDialogVisible" title="代发货" width="420px">
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

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 10px 16px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.item-list {
  padding: 8px 12px;
  background: var(--ldk-surface-soft);
  border-radius: 10px;
}

.item-row {
  display: grid;
  grid-template-columns: 1fr 120px 120px;
  gap: 16px;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed var(--ldk-border);
}

.item-row:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
}

.item-info .title {
  font-size: 14px;
  color: var(--ldk-text-primary);
  margin-bottom: 4px;
}

.item-info .meta {
  font-size: 12px;
  color: var(--ldk-text-secondary);
}

.item-status {
  text-align: center;
}

.item-actions {
  text-align: right;
}

.pagination-container {
  margin-top: 16px;
}

@media (max-width: 900px) {
  .item-row {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .item-status,
  .item-actions {
    text-align: left;
  }
}
</style>
