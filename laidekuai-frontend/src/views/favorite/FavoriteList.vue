<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import favoriteApi from '@/api/favorite'

const router = useRouter()
const loading = ref(false)
const favoriteList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const parseImages = (goods) => {
  if (!goods || !goods.imageUrls) return []
  try {
    return JSON.parse(goods.imageUrls)
  } catch (e) {
    return []
  }
}

const getCover = (goods) => {
  const images = parseImages(goods)
  return images.length ? images[0] : ''
}

const fetchFavorites = async () => {
  loading.value = true
  try {
    const res = await favoriteApi.list({ page: page.value, size: size.value })
    if (res.code === 0) {
      favoriteList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取收藏失败:', error)
  } finally {
    loading.value = false
  }
}

const handleRemove = async (goods) => {
  try {
    const res = await favoriteApi.remove(goods.id)
    if (res.code === 0) {
      ElMessage.success('已取消收藏')
      fetchFavorites()
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
  }
}

const handlePageChange = (val) => {
  page.value = val
  fetchFavorites()
}

onMounted(() => {
  fetchFavorites()
})
</script>

<template>
  <div class="favorite-page page-shell page-stack">
    <div class="page-header">
      <div>
        <h2>我的收藏</h2>
        <p class="page-desc">查看与管理你收藏的商品。</p>
      </div>
    </div>

    <el-card class="table-card panel-card" shadow="never">
      <div class="table-container table-panel" v-loading="loading">
        <el-table :data="favoriteList" row-key="id" stripe border empty-text="暂无收藏">
          <el-table-column label="商品" min-width="260">
            <template #default="{ row }">
              <div class="goods-cell">
                <el-image v-if="getCover(row)" :src="getCover(row)" fit="cover" class="goods-cover" />
                <div class="goods-info">
                  <div class="title">{{ row.title }}</div>
                  <div class="price">¥{{ row.price?.toFixed(2) }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="100" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button link type="primary" @click="router.push(`/goods/${row.id}`)">查看</el-button>
              <el-button link type="danger" @click="handleRemove(row)">取消收藏</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            layout="prev, pager, next, total"
            :total="total"
            @current-change="handlePageChange"
            background
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.goods-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.goods-cover {
  width: 64px;
  height: 64px;
  border-radius: 8px;
}

.goods-info .title {
  font-weight: 600;
  color: var(--ldk-text-primary);
}

.goods-info .price {
  margin-top: 4px;
  color: var(--ldk-primary);
}
</style>
