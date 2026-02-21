<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import noticeApi from '@/api/notice'

const route = useRoute()
const loading = ref(false)
const notice = ref(null)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await noticeApi.detail(route.params.id)
    if (res.code === 0) {
      notice.value = res.data
    }
  } catch (error) {
    console.error('获取公告失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="notice-detail-page" v-loading="loading">
    <div v-if="notice" class="notice-card">
      <h1 class="title">{{ notice.title }}</h1>
      <div class="meta">
        <span class="label">发布时间</span>
        <span class="value">{{ notice.publishedAt || '-' }}</span>
      </div>
      <div class="content">{{ notice.content }}</div>
    </div>
  </div>
</template>

<style scoped>
.notice-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
}

.notice-card {
  background: white;
  border-radius: 10px;
  padding: 28px 32px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.08);
}

.title {
  font-size: 28px;
  color: #1f2937;
  margin: 0 0 16px;
}

.meta {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 24px;
  display: flex;
  gap: 10px;
}

.content {
  font-size: 15px;
  line-height: 1.8;
  color: #374151;
  white-space: pre-line;
}
</style>
