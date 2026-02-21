<script setup>
import { ref, reactive, onMounted } from 'vue'
import auditLogApi from '@/api/auditLog'

const loading = ref(false)
const logList = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  orderNo: '',
  action: '',
  operator: ''
})

const actionOptions = [
  { label: '退款通过', value: 'REFUND_APPROVE' },
  { label: '退款驳回', value: 'REFUND_REJECT' },
  { label: '管理员代发货', value: 'SHIP_BY_ADMIN' },
  { label: '商品审核通过', value: 'GOODS_APPROVE' },
  { label: '商品审核驳回', value: 'GOODS_REJECT' }
]

const actionLabelMap = actionOptions.reduce((acc, item) => {
  acc[item.value] = item.label
  return acc
}, {})

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page,
      size: queryParams.size
    }
    if (queryParams.orderNo?.trim()) params.orderNo = queryParams.orderNo.trim()
    if (queryParams.action) params.action = queryParams.action
    if (queryParams.operator?.trim()) params.operator = queryParams.operator.trim()
    const res = await auditLogApi.list(params)
    if (res.code === 0) {
      logList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取审计日志失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchLogs()
}

const handleReset = () => {
  queryParams.orderNo = ''
  queryParams.action = ''
  queryParams.operator = ''
  queryParams.page = 1
  fetchLogs()
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchLogs()
}

const formatAction = (value) => {
  return actionLabelMap[value] || value || '-'
}

onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <div class="audit-log-page page-shell page-stack">
    <div class="page-header">
      <div>
        <h2>审计日志</h2>
        <p class="page-desc">追踪关键操作与处理记录，支持按订单与操作者筛选。</p>
      </div>
    </div>

    <el-card class="filter-card panel-card" shadow="never">
      <el-form class="filter-form" :inline="true" label-width="70px">
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable />
        </el-form-item>
        <el-form-item label="动作">
          <el-select v-model="queryParams.action" placeholder="动作" clearable style="width: 180px">
            <el-option
              v-for="item in actionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="queryParams.operator" placeholder="操作人" clearable />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card panel-card" shadow="never">
      <div class="table-container table-panel" v-loading="loading">
        <el-table
          :data="logList"
          style="width: 100%"
          row-key="id"
          stripe
          border
          empty-text="暂无审计记录"
        >
          <el-table-column prop="createdAt" label="时间" width="180" />
          <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
          <el-table-column label="动作" width="160">
            <template #default="{ row }">{{ formatAction(row.action) }}</template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="140" />
          <el-table-column prop="operatorRole" label="角色" width="120" />
          <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
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
