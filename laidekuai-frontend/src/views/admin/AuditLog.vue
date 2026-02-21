<script setup>
import { ref, reactive } from 'vue'

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
  { label: '代发货', value: 'SHIP_BY_ADMIN' },
  { label: '商品审核', value: 'GOODS_APPROVE' }
]

const handleSearch = () => {
  queryParams.page = 1
}

const handleReset = () => {
  queryParams.orderNo = ''
  queryParams.action = ''
  queryParams.operator = ''
  queryParams.page = 1
}

const handleCurrentChange = (page) => {
  queryParams.page = page
}
</script>

<template>
  <div class="audit-log-page page-shell">
    <div class="page-header">
      <div>
        <h2>审计日志</h2>
        <p class="page-desc">审计日志功能占位，待后续接入数据与筛选逻辑。</p>
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
          <el-table-column prop="action" label="动作" width="140" />
          <el-table-column prop="operator" label="操作人" width="140" />
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

<style scoped>
.audit-log-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
