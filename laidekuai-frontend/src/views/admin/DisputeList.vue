<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import disputeApi from '@/api/dispute'

const loading = ref(false)
const disputeList = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  status: '',
  orderId: ''
})

const fetchDisputes = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page,
      size: queryParams.size,
      status: queryParams.status || undefined,
      order_id: queryParams.orderId || undefined
    }
    const res = await disputeApi.list(params)
    if (res.code === 0) {
      disputeList.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    console.error('获取争议列表失败:', error)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  queryParams.status = ''
  queryParams.orderId = ''
  queryParams.page = 1
  fetchDisputes()
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchDisputes()
}

const handleView = () => {
  ElMessage.info('争议详情模块尚未实现')
}

const formatStatus = (status) => {
  const map = {
    DISPUTED: '争议中',
    REFUNDED: '已退款',
    COMPLETED: '已完成',
    RESOLVED: '已裁决'
  }
  return map[status] || status || '-'
}

onMounted(() => {
  fetchDisputes()
})
</script>

<template>
  <div class="admin-dispute page-shell" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>争议列表</h2>
        <p class="page-desc">集中查看争议订单与处理状态。</p>
      </div>
    </div>

    <el-card class="panel-card filter-card" shadow="never">
      <div class="filter-row">
        <el-input v-model="queryParams.orderId" placeholder="订单号" clearable style="max-width: 220px" />
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="max-width: 180px">
          <el-option label="争议中" value="DISPUTED" />
          <el-option label="已裁决" value="RESOLVED" />
          <el-option label="已退款" value="REFUNDED" />
          <el-option label="已完成" value="COMPLETED" />
        </el-select>
        <el-button type="primary" @click="fetchDisputes">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <el-table :data="disputeList" stripe border empty-text="暂无争议记录">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderId" label="订单号" min-width="160" />
        <el-table-column prop="applicantName" label="申请人" min-width="120">
          <template #default="{ row }">{{ row.applicantName || `用户${row.applicantId || ''}` }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="争议原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag>{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" min-width="160" />
        <el-table-column label="操作" width="120">
          <template #default>
            <el-button size="small" @click="handleView">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
.admin-dispute {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}
</style>
