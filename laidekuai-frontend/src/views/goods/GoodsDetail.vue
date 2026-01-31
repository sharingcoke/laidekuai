<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import goodsApi from '@/api/goods'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const good = ref(null)
const coverList = computed(() => {
  if (!good.value?.imageUrls) return []
  try {
    return JSON.parse(good.value.imageUrls) || []
  } catch (e) {
    return []
  }
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await goodsApi.detail(route.params.id)
    if (res.code === 0) {
      good.value = res.data
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('加载商品详情失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<template>
  <div class="goods-detail" v-loading="loading">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item to="/">首页</el-breadcrumb-item>
      <el-breadcrumb-item to="/goods">商品</el-breadcrumb-item>
      <el-breadcrumb-item>{{ good?.title || '详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-if="good" class="detail-container">
      <div class="gallery">
        <el-image
          v-for="(img, idx) in coverList.length ? coverList : ['https://via.placeholder.com/400x300?text=No+Image']"
          :key="idx"
          :src="img"
          fit="cover"
          :preview-src-list="coverList"
        />
      </div>
      <div class="info">
        <h2>{{ good.title }}</h2>
        <p class="subtitle" v-if="good.subTitle">{{ good.subTitle }}</p>
        <div class="price">￥{{ Number(good.price || 0).toFixed(2) }}</div>
        <div class="meta">
          <span>库存：{{ good.stock ?? '-' }}</span>
          <span>状态：{{ good.status }}</span>
        </div>
        <el-divider />
        <div class="actions">
          <el-button type="primary" :disabled="good.status !== 'APPROVED'" @click="ElMessage.info('下单流程待接入')">
            立即购买
          </el-button>
          <el-button @click="ElMessage.info('加入购物车待接入')">加入购物车</el-button>
        </div>
      </div>
    </div>

    <el-card v-if="good" class="detail-card" shadow="never">
      <template #header>
        <span>商品详情</span>
      </template>
      <div class="detail-text">{{ good.detail || '暂无描述' }}</div>
    </el-card>
  </div>
</template>

<style scoped>
.goods-detail {
  padding: 24px;
}
.breadcrumb {
  margin-bottom: 16px;
}
.detail-container {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 24px;
  margin-bottom: 16px;
}
.gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}
.gallery :deep(.el-image) {
  width: 100%;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
}
.info h2 {
  margin: 0 0 8px;
  font-size: 24px;
}
.subtitle {
  color: #888;
  margin: 0 0 8px;
}
.price {
  color: #ff6b3b;
  font-size: 26px;
  font-weight: 700;
  margin: 12px 0;
}
.meta {
  color: #666;
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}
.actions {
  display: flex;
  gap: 12px;
}
.detail-card {
  margin-top: 12px;
}
.detail-text {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #444;
}
</style>
