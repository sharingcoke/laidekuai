<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import cartApi from '@/api/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const cartList = ref([])
const selectedRows = ref([]) // 选中的行

/**
 * 获取购物车列表
 */
const fetchCart = async () => {
  loading.value = true
  try {
    const res = await cartApi.list()
    if (res.code === 0) {
      cartList.value = res.data
    }
  } catch (error) {
    console.error('获取购物车失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 更新数量
 */
const handleQuantityChange = async (item) => {
  try {
    await cartApi.update(item.id, item.quantity)
  } catch (error) {
    ElMessage.error('更新数量失败')
    // 恢复原状
    fetchCart() 
  }
}

/**
 * 删除商品
 */
const handleDelete = (item) => {
  ElMessageBox.confirm(
    '确定要从购物车删除该商品吗?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await cartApi.delete(item.id)
      if (res.code === 0) {
        ElMessage.success('删除成功')
        fetchCart() // 重新加载
      }
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

/**
 * 表格选择改变
 */
const handleSelectionChange = (val) => {
  selectedRows.value = val
}

/**
 * 计算总价
 */
const totalPrice = computed(() => {
  return selectedRows.value.reduce((total, item) => {
    return total + item.goodsPrice * item.quantity
  }, 0)
})

/**
 * 结算
 */
const handleCheckout = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }

  // 构造结算数据
  const checkoutItems = selectedRows.value.map(item => ({
    goodsId: item.goodsId,
    quantity: item.quantity,
    cartId: item.id,
    goodsTitle: item.goodsTitle,
    goodsPrice: item.goodsPrice,
    goodsCover: item.goodsCover
  }))

  // 存入 sessionStorage 供确认订单页使用
  sessionStorage.setItem('checkoutItems', JSON.stringify(checkoutItems))
  
  // 跳转到确认订单页
  router.push('/order/confirm')
}

/**
 * 获取主图
 */
const getMainImage = (imageUrls) => {
  if (!imageUrls) return 'https://via.placeholder.com/100x100?text=No+Image'
  try {
    const urls = JSON.parse(imageUrls)
    return urls[0] || 'https://via.placeholder.com/100x100?text=No+Image'
  } catch (e) {
    return 'https://via.placeholder.com/100x100?text=Error'
  }
}

onMounted(() => {
  fetchCart()
})
</script>

<template>
  <div class="cart-page">
    <div class="page-header">
      <h2>购物车 ({{ cartList.length }})</h2>
    </div>

    <div class="cart-container" v-loading="loading">
      <el-table 
        :data="cartList" 
        style="width: 100%" 
        @selection-change="handleSelectionChange"
        empty-text="购物车空空如也，快去选购吧"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="商品信息" min-width="400">
          <template #default="{ row }">
            <div class="goods-info">
              <el-image 
                :src="getMainImage(row.goodsCover)" 
                class="goods-cover"
                fit="cover"
              />
              <div class="goods-detail">
                <router-link :to="`/goods/${row.goodsId}`" class="goods-title">{{ row.goodsTitle }}</router-link>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="单价" width="150" align="center">
          <template #default="{ row }">
            <span class="price">¥ {{ row.goodsPrice?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="数量" width="180" align="center">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.quantity" 
              :min="1" 
              :max="99" 
              size="small"
              @change="handleQuantityChange(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="小计" width="150" align="center">
          <template #default="{ row }">
            <span class="subtotal">¥ {{ (row.goodsPrice * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button 
              type="danger" 
              link 
              :icon="Delete" 
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer" v-if="cartList.length > 0">
        <div class="left">
          <!-- 这里可以放全选按钮，Element表格已通过表头处理 -->
        </div>
        <div class="right">
          <span class="total-label">已选 {{ selectedRows.length }} 件商品，合计：</span>
          <span class="total-price">¥ {{ totalPrice.toFixed(2) }}</span>
          <el-button type="primary" size="large" @click="handleCheckout" :disabled="selectedRows.length === 0">
            结算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.cart-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  min-height: 400px;
}

.goods-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.goods-cover {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.goods-title {
  color: #303133;
  text-decoration: none;
  font-size: 14px;
  line-height: 1.4;
}

.goods-title:hover {
  color: #409eff;
}

.price {
  color: #606266;
  font-weight: bold;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.cart-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-footer .right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.total-label {
  color: #606266;
}

.total-price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}
</style>
