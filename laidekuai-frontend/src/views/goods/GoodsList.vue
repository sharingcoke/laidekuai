<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import goodsApi from '@/api/goods'
import CategorySelect from '@/components/common/CategorySelect.vue'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()

// 搜索参数
const queryParams = reactive({
  page: 1,
  size: 12,
  keyword: '',
  categoryId: null
})

const loading = ref(false)
const goodsList = ref([])
const total = ref(0) // 总条数

/**
 * 获取商品列表
 */
const fetchGoods = async () => {
  loading.value = true
  try {
    const res = await goodsApi.list(queryParams)
    if (res.code === 0) {
      goodsList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  queryParams.page = 1
  fetchGoods()
}

/**
 * ????
 */
const handleCategoryChange = () => {
  queryParams.page = 1
  fetchGoods()
}

/**
 * 分页改变
 */
const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchGoods()
}

/**
 * 跳转详情
 */
const goToDetail = (id) => {
  router.push(`/goods/${id}`) // 路由配置需匹配 /goods/:id
}

/**
 * 获取主图
 */
const getMainImage = (imageUrls) => {
  if (!imageUrls) return 'https://via.placeholder.com/300x300?text=No+Image'
  try {
    const urls = JSON.parse(imageUrls)
    return urls[0] || 'https://via.placeholder.com/300x300?text=No+Image'
  } catch (e) {
    return 'https://via.placeholder.com/300x300?text=Error'
  }
}

/**
 * 格式化价格
 */
const formatPrice = (price) => {
  return typeof price === 'number' ? price.toFixed(2) : '0.00'
}

onMounted(() => {
  fetchGoods()
})
</script>

<template>
  <div class="goods-list-page">
    <div class="search-bar">
      <CategorySelect
        v-model="queryParams.categoryId"
        class="category-select"
        placeholder="????"
        @change="handleCategoryChange"
      />
      <el-input
        v-model="queryParams.keyword"
        placeholder="????..."
        class="search-input"
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>
    </div>

    <div class="goods-container" v-loading="loading">
      <el-empty v-if="!loading && goodsList.length === 0" description="暂无商品" />
      
      <div v-else class="goods-grid">
        <el-card
          v-for="item in goodsList"
          :key="item.id"
          class="goods-card"
          :body-style="{ padding: '0px' }"
          shadow="hover"
          @click="goToDetail(item.id)"
        >
          <div class="goods-image-wrapper">
             <el-image 
               :src="getMainImage(item.imageUrls)" 
               fit="cover" 
               class="goods-image"
               loading="lazy"
             >
               <template #placeholder>
                 <div class="image-placeholder">加载中...</div>
               </template>
               <template #error>
                 <div class="image-error">Failed</div>
               </template>
             </el-image>
          </div>
          <div class="goods-info">
            <h3 class="goods-title" :title="item.title">{{ item.title }}</h3>
            <div class="goods-price">¥ {{ formatPrice(item.price) }}</div>
            <div class="goods-meta">
              <span>{{ item.sellerName || '卖家' }}</span>
              <span class="stock">库存: {{ item.stock }}</span>
            </div>
          </div>
        </el-card>
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
.goods-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-bar {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.category-select {
  min-width: 200px;
}

.search-input {
  max-width: 600px;
  width: 100%;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.goods-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.goods-card:hover {
  transform: translateY(-5px);
}

.goods-image-wrapper {
  height: 260px;
  overflow: hidden;
  position: relative;
  background: #f5f7fa;
}

.goods-image {
  width: 100%;
  height: 100%;
  display: block;
}

.image-placeholder, .image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
  font-size: 14px;
}

.goods-info {
  padding: 14px;
}

.goods-title {
  margin: 0 0 10px;
  font-size: 16px;
  color: #303133;
  line-height: 1.4;
  height: 44px; /* 2 lines */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goods-price {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 8px;
}

.goods-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
