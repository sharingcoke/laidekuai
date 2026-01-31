<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import goodsApi from '@/api/goods'

const loading = ref(false)
const goods = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const keyword = ref('')
const placeholder = 'https://via.placeholder.com/300x180?text=No+Image'

const getCover = (imageJson) => {
  if (!imageJson) return placeholder
  try {
    const arr = JSON.parse(imageJson)
    return arr && arr[0] ? arr[0] : placeholder
  } catch (e) {
    return placeholder
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await goodsApi.list({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    if (res.code === 0) {
      goods.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载商品列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchList()
}

const handleSizeChange = (val) => {
  size.value = val
  page.value = 1
  fetchList()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="goods-page">
    <div class="page-header">
      <h2>发现好物</h2>
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索商品关键词"
          clearable
          size="large"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <el-skeleton v-if="loading" rows="6" animated />

    <el-empty v-else-if="!goods.length" description="暂无商品" />

    <div v-else class="goods-grid">
      <el-card
        v-for="item in goods"
        :key="item.id"
        shadow="hover"
        class="goods-card"
        @click=".push(/goods/)"
      >
        <div class="cover" :style="{ backgroundImage: url() }"></div>
        <div class="title" :title="item.title">{{ item.title }}</div>
        <div class="meta">
          <span class="price">￥{{ Number(item.price || 0).toFixed(2) }}</span>
          <span class="stock">库存 {{ item.stock ?? '-' }}</span>
        </div>
      </el-card>
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        :current-page="page"
        :page-size="size"
        :page-sizes="[12, 24, 36, 48]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped>
.goods-page {
  padding: 24px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 16px;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}
.search-box {
  flex: 1;
}
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.goods-card {
  cursor: pointer;
  transition: transform 0.15s ease;
}
.goods-card:hover {
  transform: translateY(-4px);
}
.cover {
  width: 100%;
  height: 150px;
  background-size: cover;
  background-position: center;
  border-radius: 6px;
}
.title {
  margin-top: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  min-height: 42px;
}
.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  color: #666;
}
.price {
  color: #ff6b3b;
  font-weight: 700;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
