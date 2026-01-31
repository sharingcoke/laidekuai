<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import orderApi from '@/api/order'

const router = useRouter()
const loading = ref(false)
const orderList = ref([])
const total = ref(0) // 总条数

const queryParams = reactive({
  page: 1,
  size: 10,
  status: '' // 状态筛选
})

/**
 * 获取订单列表
 */
const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.list(queryParams)
    if (res.code === 0) {
      orderList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取订单失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 切换状态Tab
 */
const handleTabClick = (tab) => {
  queryParams.status = tab.props.name === 'all' ? '' : tab.props.name
  queryParams.page = 1
  fetchOrders()
}

/**
 * 分页改变
 */
const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchOrders()
}

/**
 * 支付
 */
const handlePay = async (orderId) => {
  try {
    const res = await orderApi.pay(orderId)
    if (res.code === 0) {
      ElMessage.success('支付成功')
      fetchOrders()
    }
  } catch (error) {
    console.error('支付失败:', error)
  }
}

/**
 * 确认收货
 */
const handleReceive = (orderId) => {
  ElMessageBox.confirm('确认收到货物了吗?', '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await orderApi.receive(orderId)
        if (res.code === 0) {
          ElMessage.success('确认收货成功')
          fetchOrders()
        }
      } catch (error) {
         // error handled in request.js
      }
    }).catch(() => {})
}

/**
 * 取消订单
 */
const handleCancel = (orderId) => {
  ElMessageBox.confirm('确定要取消订单吗?', '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await orderApi.cancel(orderId, '用户主动取消')
        if (res.code === 0) {
          ElMessage.success('取消成功')
          fetchOrders()
        }
      } catch (error) {}
    }).catch(() => {})
}

/**
 * 申请退款 (V1简化，仅触发)
 */
const handleRefund = (orderId) => {
   // 实际应弹出对话框输入原因
   const reason = prompt('请输入退款原因')
   if (reason) {
       orderApi.refund(orderId, reason).then(res => {
           if (res.code === 0) {
               ElMessage.success('退款申请已提交')
               fetchOrders()
           }
       })
   }
}

const formatStatus = (status) => {
  const map = {
    'PENDING_PAY': '待支付',
    'PAID': '待发货',
    'SHIPPED': '待收货',
    'COMPLETED': '已完成',
    'CANCELED': '已取消',
    'REFUNDING': '退款中',
    'REFUNDED': '已退款',
    'DISPUTED': '争议中'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  if (status === 'PENDING_PAY') return 'danger'
  if (status === 'PAID') return 'warning'
  if (status === 'SHIPPED') return 'primary'
  if (status === 'COMPLETED') return 'success'
  return 'info'
}

/**
 * 看第一张图
 */
const getMainImage = (imageUrls) => {
    try {
        return JSON.parse(imageUrls)[0]
    } catch {
        return ''
    }
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="order-list-page">
    <div class="page-header">
      <h2>我的订单</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="order-tabs">
      <el-tab-pane label="全部订单" name="all"></el-tab-pane>
      <el-tab-pane label="待支付" name="PENDING_PAY"></el-tab-pane>
      <el-tab-pane label="待发货" name="PAID"></el-tab-pane>
      <el-tab-pane label="待收货" name="SHIPPED"></el-tab-pane>
      <el-tab-pane label="已完成" name="COMPLETED"></el-tab-pane>
    </el-tabs>

    <div class="order-list" v-loading="loading">
      <el-empty v-if="!loading && orderList.length === 0" description="暂无订单" />
      
      <div v-else class="order-item" v-for="order in orderList" :key="order.id">
        <div class="order-header">
          <span class="order-no">订单号: {{ order.orderNo }}</span>
          <span class="create-time">{{ order.createdAt }}</span>
          <span class="seller">卖家: {{ order.items[0]?.sellerName || '未知' }}</span>
          <el-tag :type="getStatusType(order.status)" class="status-tag">{{ formatStatus(order.status) }}</el-tag>
        </div>
        
        <div class="order-body">
            <div class="order-goods-list">
                <div class="goods-item" v-for="item in order.items" :key="item.id">
                    <el-image :src="getMainImage(item.goodsCover)" class="goods-img" fit="cover" />
                    <div class="goods-detail">
                        <div class="goods-title">{{ item.goodsTitle }}</div>
                        <div class="goods-price">¥ {{ item.price }} x {{ item.quantity }}</div>
                    </div>
                </div>
            </div>
            
            <div class="order-payment">
                <div class="total">合计: ¥ {{ order.totalAmount }}</div>
                <div class="shipping">(含运费 ¥ {{ order.shippingFee }})</div>
            </div>
            
            <div class="order-actions">
                <el-button v-if="order.status === 'PENDING_PAY'" type="primary" size="small" @click="handlePay(order.id)">立即支付</el-button>
                <el-button v-if="order.status === 'PENDING_PAY'" size="small" @click="handleCancel(order.id)">取消订单</el-button>
                
                <el-button v-if="order.status === 'SHIPPED'" type="success" size="small" @click="handleReceive(order.id)">确认收货</el-button>
                
                <el-button v-if="order.status === 'PAID'" size="small" @click="handleRefund(order.id)">申请退款</el-button>

                <!-- REVIEW按钮 (COMPLETED) -->
                <el-button v-if="order.status === 'COMPLETED'" type="info" size="small" plain>评价</el-button>
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

  </div>
</template>

<style scoped>
.order-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.order-tabs {
  margin-bottom: 20px;
  background: white;
  padding: 0 20px;
  border-radius: 4px;
}

.order-item {
  background: white;
  border: 1px solid #eee;
  margin-bottom: 20px;
  border-radius: 4px;
}

.order-header {
  padding: 10px 20px;
  background: #fbfbfb;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
  gap: 20px;
}

.status-tag {
  margin-left: auto;
}

.order-body {
  padding: 20px;
  display: flex;
  align-items: flex-start;
}

.order-goods-list {
  flex: 1;
}

.goods-item {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.goods-img {
  width: 80px;
  height: 80px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.goods-detail {
  flex: 1;
}

.goods-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
}

.goods-price {
  color: #999;
  font-size: 13px;
}

.order-payment {
  width: 150px;
  text-align: right;
  border-left: 1px solid #eee;
  padding-left: 20px;
  margin-right: 20px;
}

.total {
  font-weight: bold;
  font-size: 16px;
  color: #333;
}

.shipping {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.order-actions {
  width: 120px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
}

.pagination-container {
  text-align: center;
  margin-top: 20px;
}
</style>
