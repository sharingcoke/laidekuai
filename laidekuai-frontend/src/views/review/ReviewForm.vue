<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import orderApi from '@/api/order'
import reviewApi from '@/api/review'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const order = ref(null)
const orderItem = ref(null)

const form = reactive({
  rating: 0,
  content: '',
  images: [],
  isAnonymous: false
})

const uploadHeaders = ref({
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
})

const canReview = computed(() => {
  return order.value && (order.value.status === 'COMPLETED' || order.value.status === 'REFUNDED')
})

const isRefunded = computed(() => order.value?.status === 'REFUNDED')

const fetchOrder = async () => {
  const orderId = route.query.orderId
  const orderItemId = route.query.orderItemId
  if (!orderId || !orderItemId) {
    ElMessage.error('订单参数不完整')
    router.back()
    return
  }

  loading.value = true
  try {
    const res = await orderApi.detail(orderId)
    if (res.code === 0) {
      order.value = res.data
      orderItem.value = order.value.items?.find(item => String(item.id) === String(orderItemId)) || null
      if (!orderItem.value) {
        ElMessage.error('未找到对应订单项')
        router.back()
      }
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handleUploadSuccess = (response, uploadFile) => {
  if (response.code === 0) {
    uploadFile.url = response.data
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const submitReview = async () => {
  if (!orderItem.value) {
    ElMessage.warning('订单项不存在')
    return
  }
  if (!canReview.value) {
    ElMessage.warning('当前订单状态不可评价')
    return
  }
  if (!form.rating) {
    ElMessage.warning('请先评分')
    return
  }

  const imageUrls = form.images
    .map(item => item.url || item.response?.data)
    .filter(url => !!url)

  submitting.value = true
  try {
    const res = await reviewApi.create({
      orderItemId: orderItem.value.id,
      rating: form.rating,
      content: form.content,
      images: imageUrls,
      isAnonymous: form.isAnonymous
    })
    if (res.code === 0) {
      ElMessage.success('评价提交成功')
      router.push('/reviews/my')
    }
  } catch (error) {
    console.error('提交评价失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchOrder()
})
</script>

<template>
  <div class="review-form-page page-shell" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>创建评价</h2>
        <p class="page-desc">按订单项进行评价，真实反馈帮助更多人。</p>
      </div>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-card class="panel-card info-card" shadow="never" v-if="order && orderItem">
      <div class="info-row">
        <div class="info-item">
          <span class="label">订单项</span>
          <span class="value">#{{ orderItem.id }}</span>
        </div>
        <div class="info-item">
          <span class="label">商品</span>
          <span class="value">{{ orderItem.goodsTitle }}</span>
        </div>
        <div class="info-item">
          <span class="label">卖家</span>
          <span class="value">{{ order.sellerName || `用户${order.sellerId}` }}</span>
        </div>
        <div class="info-item">
          <span class="label">订单状态</span>
          <el-tag v-if="isRefunded" type="warning">退款评价</el-tag>
          <el-tag v-else type="success">正常评价</el-tag>
        </div>
      </div>
    </el-card>

    <el-card class="panel-card form-card" shadow="never">
      <el-form label-width="90px" class="form-standard">
        <el-form-item label="评分">
          <el-rate v-model="form.rating" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="写下你的使用体验"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            v-model:file-list="form.images"
            action="/api/files/upload"
            list-type="picture-card"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            multiple
          >
            <span>上传</span>
          </el-upload>
        </el-form-item>
        <el-form-item label="匿名评价">
          <el-switch v-model="form.isAnonymous" />
        </el-form-item>
        <el-form-item class="form-actions">
          <el-button type="primary" :loading="submitting" @click="submitReview">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.review-form-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card,
.form-card {
  border-radius: 8px;
}

.info-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  font-size: 12px;
  color: #909399;
}

.value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

@media (max-width: 900px) {
  .info-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .info-row {
    grid-template-columns: 1fr;
  }
}
</style>
