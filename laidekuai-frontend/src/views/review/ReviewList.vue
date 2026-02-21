<script setup>
import { ref, reactive, onMounted } from 'vue'
import reviewApi from '@/api/review'

const loading = ref(false)
const reviewList = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10
})

const fetchReviews = async () => {
  loading.value = true
  try {
    const res = await reviewApi.listMy({
      page: queryParams.page,
      size: queryParams.size
    })
    if (res.code === 0) {
      reviewList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取我的评价失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchReviews()
}

onMounted(() => {
  fetchReviews()
})
</script>

<template>
  <div class="review-list-page page-shell">
    <div class="page-header">
      <div>
        <h2>我的评价</h2>
        <p class="page-desc">按订单项查看评价记录，退款评价单独标记。</p>
      </div>
    </div>

    <div class="review-list" v-loading="loading">
      <el-empty v-if="!loading && reviewList.length === 0" description="暂无评价" />

      <div v-else class="review-card list-item-card" v-for="review in reviewList" :key="review.id">
        <div class="list-item-head">
          <span class="title">{{ review.goodsTitle || `商品#${review.goodsId}` }}</span>
          <span class="meta">订单#{{ review.orderId }}</span>
          <el-tag v-if="review.isRefunded" type="warning">退款评价</el-tag>
          <span class="meta">{{ review.createdAt }}</span>
        </div>
        <div class="review-body">
          <el-rate :model-value="review.rating" disabled show-score />
          <div class="content">{{ review.content || '未填写评价内容' }}</div>
          <div class="images" v-if="review.images && review.images.length">
            <el-image
              v-for="(img, index) in review.images"
              :key="index"
              :src="img"
              fit="cover"
              class="review-image"
              :preview-src-list="review.images"
              :initial-index="index"
            />
          </div>
          <div v-if="review.reply" class="reply">
            <span class="label">卖家回复：</span>
            <span>{{ review.reply }}</span>
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
.review-list-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  padding-bottom: 12px;
}

.review-body {
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.content {
  font-size: 14px;
  color: var(--ldk-text-regular);
}

.images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.review-image {
  width: 88px;
  height: 88px;
  border-radius: 8px;
}

.reply {
  padding-top: 8px;
  border-top: 1px dashed var(--ldk-border);
  color: var(--ldk-text-secondary);
  font-size: 13px;
}

.reply .label {
  color: var(--ldk-text-primary);
  font-weight: 500;
}
</style>
