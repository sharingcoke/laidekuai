<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import reviewApi from '@/api/review'

const loading = ref(false)
const reviewList = ref([])
const total = ref(0)
const activeTab = ref('all')

const queryParams = reactive({
  page: 1,
  size: 10,
  status: ''
})

const fetchReviews = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page,
      size: queryParams.size,
      status: queryParams.status || undefined
    }
    const res = await reviewApi.listAdmin(params)
    if (res.code === 0) {
      reviewList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取评价列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTabClick = (tab) => {
  queryParams.status = tab.props.name === 'all' ? '' : tab.props.name
  queryParams.page = 1
  fetchReviews()
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchReviews()
}

const handleHide = (review) => {
  ElMessageBox.confirm('确定隐藏该评价吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await reviewApi.hide(review.id)
    if (res.code === 0) {
      ElMessage.success('已隐藏')
      fetchReviews()
    }
  }).catch(() => {})
}

const handleDelete = (review) => {
  ElMessageBox.confirm('确定删除该评价吗？删除后不可恢复。', '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await reviewApi.remove(review.id)
    if (res.code === 0) {
      ElMessage.success('已删除')
      fetchReviews()
    }
  }).catch(() => {})
}

const formatStatus = (status) => {
  const map = {
    'visible': '可见',
    'hidden': '隐藏',
    'deleted': '已删除'
  }
  return map[status] || status
}

const statusType = (status) => {
  if (status === 'visible') return 'success'
  if (status === 'hidden') return 'warning'
  if (status === 'deleted') return 'info'
  return ''
}

onMounted(() => {
  fetchReviews()
})
</script>

<template>
  <div class="review-manage-page">
    <div class="page-header">
      <div>
        <h2>评价管理</h2>
        <p class="page-desc">控制评价可见性，保持内容健康。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="tabs-panel" @tab-click="handleTabClick">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="可见" name="visible" />
      <el-tab-pane label="隐藏" name="hidden" />
      <el-tab-pane label="已删除" name="deleted" />
    </el-tabs>

    <el-card class="table-card" shadow="never">
      <div class="table-container" v-loading="loading">
        <el-table
          :data="reviewList"
          style="width: 100%"
          stripe
          border
          row-key="id"
          empty-text="暂无评价"
        >
          <el-table-column prop="goodsTitle" label="商品" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.goodsTitle || `商品#${row.goodsId}` }}
            </template>
          </el-table-column>
          <el-table-column prop="rating" label="评分" width="140">
            <template #default="{ row }">
              <el-rate :model-value="row.rating" disabled show-score />
            </template>
          </el-table-column>
          <el-table-column prop="buyerName" label="用户" width="140" show-overflow-tooltip />
          <el-table-column prop="content" label="评价内容" min-width="220" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ formatStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'visible'"
                size="small"
                @click="handleHide(row)"
              >
                隐藏
              </el-button>
              <el-button
                v-if="row.status !== 'deleted'"
                size="small"
                type="danger"
                @click="handleDelete(row)"
              >
                删除
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
.review-manage-page {
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

.tabs-panel {
  margin-bottom: 16px;
  background: white;
  padding: 6px 16px;
  border-radius: 8px;
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
