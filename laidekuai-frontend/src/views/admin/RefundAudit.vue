<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import orderApi from '@/api/order'

const loading = ref(false)
const orderList = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  status: 'REFUNDING',
  orderNo: '',
  buyerId: '',
  sellerId: ''
})

const fetchRefunds = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page,
      size: queryParams.size,
      status: queryParams.status,
      order_no: queryParams.orderNo || undefined,
      buyer_id: queryParams.buyerId || undefined,
      seller_id: queryParams.sellerId || undefined
    }
    const res = await orderApi.listAdmin(params)
    if (res.code === 0) {
      orderList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取退款审核列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchRefunds()
}

const handleReset = () => {
  queryParams.orderNo = ''
  queryParams.buyerId = ''
  queryParams.sellerId = ''
  queryParams.page = 1
  fetchRefunds()
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchRefunds()
}

const handleApprove = (order) => {
  ElMessageBox.confirm(`确定通过订单 ${order.orderNo} 的退款申请吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await orderApi.approveRefundByAdmin(order.id)
    if (res.code === 0) {
      ElMessage.success('已通过退款')
      fetchRefunds()
    }
  }).catch(() => {})
}

const handleReject = (order) => {
  ElMessageBox.confirm(`确定驳回订单 ${order.orderNo} 的退款申请吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await orderApi.rejectRefundByAdmin(order.id)
    if (res.code === 0) {
      ElMessage.success('已驳回退款')
      fetchRefunds()
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
  fetchRefunds()
})
</script>

<template>
  <div class="refund-audit-page">
    <div class="page-header">
      <div>
        <h2>退款审核</h2>
        <p class="page-desc">集中处理退款中订单，避免并发审核冲突。</p>
      </div>
    </div>

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
        <el-form-item class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-container" v-loading="loading">
        <el-table
          :data="orderList"
          style="width: 100%"
          row-key="id"
          stripe
          border
          empty-text="暂无退款订单"
        >
          <el-table-column prop="orderNo" label="订单号" min-width="200" show-overflow-tooltip />
          <el-table-column prop="buyerId" label="买家ID" width="100" />
          <el-table-column prop="sellerId" label="卖家ID" width="100" />
          <el-table-column prop="totalAmount" label="金额" width="120" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag type="warning">{{ formatStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button
                type="success"
                size="small"
                :disabled="row.status !== 'REFUNDING'"
                @click="handleApprove(row)"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                :disabled="row.status !== 'REFUNDING'"
                @click="handleReject(row)"
              >
                驳回
              </el-button>
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
  </div>
</template>

<style scoped>
.refund-audit-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.filter-card {
  margin-bottom: 16px;
  border-radius: 8px;
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

.table-card {
  border-radius: 8px;
}

.table-container {
  padding: 6px 0 0;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
