<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import goodsApi from '@/api/goods'
import cartApi from '@/api/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const goodsId = route.params.id
const goods = ref(null)
const loading = ref(false)
const addingToCart = ref(false)
const quantity = ref(1)
const activeImageIndex = ref(0) // 当前显示的图片索引

// 计算属性
const imageUrls = computed(() => {
  if (!goods.value || !goods.value.imageUrls) return []
  try {
    return JSON.parse(goods.value.imageUrls)
  } catch (e) {
    return []
  }
})

const currentImage = computed(() => {
  if (imageUrls.value.length === 0) {
    return 'https://via.placeholder.com/600x600?text=No+Image'
  }
  return imageUrls.value[activeImageIndex.value]
})

const isMyGoods = computed(() => {
  return authStore.user && goods.value && authStore.user.id === goods.value.sellerId
})

const canBuy = computed(() => {
  return goods.value && goods.value.status === 'ON_SHELF' && goods.value.stock > 0
})

/**
 * 获取商品详情
 */
const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await goodsApi.detail(goodsId)
    if (res.code === 0) {
      goods.value = res.data
    } else {
      ElMessage.error(res.message || '获取商品详情失败')
      router.push('/goods')
    }
  } catch (error) {
    console.error('获取商品详情错误:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 切换图片
 */
const setActiveImage = (index) => {
  activeImageIndex.value = index
}

/**
 * 加入购物车
 */
const addToCart = async () => {
  if (!authStore.isLoggedIn) {
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  if (isMyGoods.value) {
    ElMessage.warning('不能购买自己的商品')
    return
  }

  addingToCart.value = true
  try {
    const res = await cartApi.add(goods.value.id, quantity.value)
    if (res.code === 0) {
      ElMessage.success('已加入购物车')
    }
  } catch (error) {
    console.error('加入购物车失败:', error)
  } finally {
    addingToCart.value = false
  }
}

/**
 * 立即购买
 */
const buyNow = async () => {
  if (!authStore.isLoggedIn) {
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }
  
  if (isMyGoods.value) {
    ElMessage.warning('不能购买自己的商品')
    return
  }
  
  // 简单实现：先加入购物车，跳转到购物车选中（这里先只跳转购物车）
  // 更好的方式是跳转到确认订单页带上参数，但目前先复用购物车逻辑
  await addToCart()
  router.push('/cart')
}

/**
 * 联系卖家 (Message模块预留)
 */
const contactSeller = () => {
   if (!authStore.isLoggedIn) {
     router.push({ path: '/login', query: { redirect: route.fullPath } })
     return
   }
   ElMessage.info('留言功能开发中...')
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="goods-detail-page page-shell" v-loading="loading">
    <div v-if="goods" class="goods-detail-container">
      
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/goods' }">商品列表</el-breadcrumb-item>
        <el-breadcrumb-item>详情</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-main panel-card">
        <!-- 左侧图片区 -->
        <div class="gallery">
          <div class="main-image">
            <el-image 
              :src="currentImage" 
              fit="contain" 
              :preview-src-list="imageUrls"
              :initial-index="activeImageIndex"
              class="image-viewer"
            />
          </div>
          <div class="thumbnail-list" v-if="imageUrls.length > 1">
            <div 
              v-for="(url, index) in imageUrls" 
              :key="index"
              class="thumbnail-item"
              :class="{ active: activeImageIndex === index }"
              @mouseenter="setActiveImage(index)"
            >
              <el-image :src="url" fit="cover" />
            </div>
          </div>
        </div>

        <!-- 右侧信息区 -->
        <div class="info-panel">
          <h1 class="title">{{ goods.title }}</h1>
          <p class="subtitle" v-if="goods.subTitle">{{ goods.subTitle }}</p>

          <div class="price-box">
            <span class="currency">¥</span>
            <span class="price">{{ goods.price?.toFixed(2) }}</span>
          </div>

          <div class="meta-info">
            <div class="meta-row">
              <span class="label">卖家：</span>
              <span class="value">{{ goods.sellerName }}</span>
            </div>
            <div class="meta-row">
              <span class="label">分类：</span>
              <span class="value">{{ goods.categoryName || '默认分类' }}</span>
            </div>
            <div class="meta-row">
              <span class="label">库存：</span>
              <span class="value">{{ goods.stock }} 件</span>
            </div>
            <div class="meta-row">
              <span class="label">状态：</span>
              <span class="value status-tag">{{ goods.status === 'ON_SHELF' ? '在售' : '售罄/下架' }}</span>
            </div>
          </div>

          <div class="actions" v-if="canBuy && !isMyGoods">
            <el-input-number v-model="quantity" :min="1" :max="goods.stock" class="quantity-input" />
            
            <div class="btn-group">
                <el-button type="primary" size="large" @click="buyNow">立即购买</el-button>
                <el-button type="primary" plain size="large" :icon="ShoppingCart" :loading="addingToCart" @click="addToCart">
                    加入购物车
                </el-button>
            </div>
          </div>
          
          <div class="actions" v-else-if="isMyGoods">
              <el-alert title="这是您发布的商品" type="info" :closable="false" show-icon />
              <el-button type="primary" class="edit-btn" @click="router.push(`/goods/edit/${goods.id}`)">编辑商品</el-button>
          </div>
          
          <div class="actions" v-else>
               <el-button type="info" disabled size="large">商品已下架或售罄</el-button>
          </div>

          <div class="secondary-actions">
              <el-button link :icon="ChatDotRound" @click="contactSeller">联系卖家/留言</el-button>
              <!-- 收藏按钮预留 -->
          </div>
        </div>
      </div>

      <!-- 详情描述区 -->
      <div class="detail-content-section panel-card">
          <h3>商品详情</h3>
          <div class="content-body" v-html="goods.detail || '暂无描述'"></div>
      </div>

    </div>
    <el-empty v-else description="商品不存在或已删除" />
  </div>
</template>

<style scoped>
.breadcrumb {
  margin-bottom: 16px;
}

.detail-main {
  display: flex;
  gap: 32px;
  padding: 28px;
  margin-bottom: 24px;
}

.gallery {
  width: 420px;
  flex-shrink: 0;
}

.main-image {
  width: 100%;
  height: 420px;
  border: 1px solid var(--ldk-border);
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 12px;
  background: #fbfcfe;
}

.image-viewer {
  width: 100%;
  height: 100%;
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
}

.thumbnail-item {
  width: 64px;
  height: 64px;
  border: 2px solid transparent;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s;
}

.thumbnail-item.active {
  border-color: var(--ldk-primary);
}

.info-panel {
  flex: 1;
}

.title {
  font-size: 28px;
  color: var(--ldk-text-primary);
  margin: 0 0 8px;
  line-height: 1.35;
}

.subtitle {
  font-size: 15px;
  color: var(--ldk-text-secondary);
  margin-bottom: 18px;
}

.price-box {
  background: linear-gradient(120deg, #fff2f2 0%, #fff8f1 100%);
  border: 1px solid #ffdede;
  padding: 16px 18px;
  border-radius: 10px;
  margin-bottom: 20px;
  color: #e05555;
}

.currency {
  font-size: 20px;
  margin-right: 4px;
}

.price {
  font-size: 36px;
  font-weight: bold;
}

.meta-info {
  margin-bottom: 24px;
  display: grid;
  gap: 10px;
}

.meta-row {
  font-size: 14px;
  display: flex;
}

.label {
  color: var(--ldk-text-secondary);
  width: 72px;
  flex-shrink: 0;
}

.value {
  color: var(--ldk-text-regular);
}

.actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.quantity-input {
  width: 130px;
}

.btn-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.secondary-actions {
  margin-top: 6px;
  padding-top: 14px;
  border-top: 1px solid var(--ldk-border);
}

.detail-content-section {
  padding: 26px 28px;
}

.detail-content-section h3 {
  margin-top: 0;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--ldk-border);
  margin-bottom: 18px;
  font-size: 20px;
  color: var(--ldk-text-primary);
}

.content-body {
  line-height: 1.75;
  color: var(--ldk-text-regular);
}

@media (max-width: 768px) {
  .detail-main {
    flex-direction: column;
    padding: 18px;
  }

  .gallery {
    width: 100%;
  }

  .main-image {
    height: 300px;
  }

  .title {
    font-size: 24px;
  }

  .price {
    font-size: 30px;
  }
}
</style>
