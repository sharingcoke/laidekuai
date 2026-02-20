<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import orderApi from '@/api/order'

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
      orderNo: queryParams.orderNo || undefined,
      buyerId: queryParams.buyerId || undefined,
      sellerId: queryParams.sellerId || undefined,
      startTime: queryParams.startTime || undefined,
      endTime: queryParams.endTime || undefined
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
  <div class="admin-orders-page">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="order-tabs">
      <el-tab-pane label="全部订单" name="all"></el-tab-pane>
      <el-tab-pane label="待发货" name="PAID"></el-tab-pane>
      <el-tab-pane label="已发货" name="SHIPPED"></el-tab-pane>
      <el-tab-pane label="退款中" name="REFUNDING"></el-tab-pane>
    </el-tabs>

    <div class="search-bar">
      <el-input v-model="queryParams.orderNo" placeholder="订单号" style="width: 200px" clearable />
      <el-input v-model="queryParams.buyerId" placeholder="买家ID" style="width: 140px" clearable />
      <el-input v-model="queryParams.sellerId" placeholder="卖家ID" style="width: 140px" clearable />
      <el-date-picker
        v-model="createdRange"
        type="datetimerange"
        value-format="YYYY-MM-DD HH:mm:ss"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-container" v-loading="loading">
      <el-table :data="orderList" style="width: 100%">
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
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="buyerId" label="买家ID" width="100" />
        <el-table-column prop="sellerId" label="卖家ID" width="100" />
        <el-table-column prop="totalAmount" label="金额" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag>{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
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

    <el-dialog v-model="shipDialogVisible" title="代发货" width="420px">
      <el-form label-width="90px">
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
.admin-orders-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.order-tabs {
  margin-bottom: 20px;
  background: white;
  padding: 0 20px;
  border-radius: 4px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.table-container {
  background: white;
  padding: 20px;
  border-radius: 4px;
}

.item-list {
  padding: 10px 0;
}

.item-row {
  display: flex;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px dashed #eee;
}

.item-row:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
}

.item-info .title {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.item-info .meta {
  font-size: 12px;
  color: #999;
}

.item-status {
  width: 120px;
  text-align: center;
}

.item-actions {
  width: 120px;
  text-align: right;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
