<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import addressApi from '@/api/address'
import orderApi from '@/api/order'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)

const checkoutItems = ref([])
const addressList = ref([])
const selectedAddressId = ref(null)

// 地址表单
const addressDialogVisible = ref(false)
const addressForm = reactive({
  id: null,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

/**
 * 初始化
 */
onMounted(() => {
  // 加载结算商品
  const itemsStr = sessionStorage.getItem('checkoutItems')
  if (!itemsStr) {
    ElMessage.warning('没有选择结算商品')
    router.push('/cart')
    return
  }
  checkoutItems.value = JSON.parse(itemsStr)

  fetchAddresses()
})

/**
 * 获取地址列表
 */
const fetchAddresses = async () => {
  try {
    const res = await addressApi.list()
    if (res.code === 0) {
      addressList.value = res.data
      // 默认选中
      const defaultAddr = addressList.value.find(item => item.isDefault)
      if (defaultAddr) {
        selectedAddressId.value = defaultAddr.id
      } else if (addressList.value.length > 0) {
        selectedAddressId.value = addressList.value[0].id
      }
    }
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

/**
 * 计算总价
 */
const totalPrice = computed(() => {
  return checkoutItems.value.reduce((total, item) => {
    return total + item.goodsPrice * item.quantity
  }, 0)
})

/**
 * 提交订单
 */
const submitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  submitting.value = true
  try {
    const orderData = {
      addressId: selectedAddressId.value,
      items: checkoutItems.value.map(item => ({
        goodsId: item.goodsId,
        quantity: item.quantity,
        cartId: item.cartId // 如果是从购物车来的，传cartId以便后台删除
      })),
      remark: '' // 暂不支持备注
    }

    const res = await orderApi.create(orderData)
    if (res.code === 0) {
      ElMessage.success('订单创建成功')
      // 清空结算缓存
      sessionStorage.removeItem('checkoutItems')
      
      // 跳转订单列表或支付页
      const orders = res.data || []
      if (Array.isArray(orders) && orders.length === 1) {
        router.push(`/order/pay/${orders[0].id}`)
      } else {
        router.push('/orders')
      }
    }
  } catch (error) {
    console.error('创建订单失败:', error)
  } finally {
    submitting.value = false
  }
}

/**
 * 显示添加地址弹窗
 */
const showAddAddress = () => {
  Object.assign(addressForm, {
    id: null,
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: 0
  })
  addressDialogVisible.value = true
}

/**
 * 保存地址
 */
const saveAddress = async () => {
  // 简单验证
  if (!addressForm.receiverName || !addressForm.receiverPhone || !addressForm.detail) {
    ElMessage.warning('请填写完整地址信息')
    return
  }

  try {
    const res = await addressApi.add(addressForm)
    if (res.code === 0) {
      ElMessage.success('添加地址成功')
      addressDialogVisible.value = false
      fetchAddresses()
    }
  } catch (error) {
    console.error('保存地址失败:', error)
  }
}
</script>

<template>
  <div class="order-confirm-page page-shell">
    <div class="page-header">
      <div>
        <h2>确认订单</h2>
        <p class="page-desc">确认地址与商品后提交，信息展示按模块分区。</p>
      </div>
    </div>

    <!-- 1. 收货地址 -->
    <div class="section-card panel-card">
      <div class="section-header">
        <h3>收货地址</h3>
        <el-button type="primary" link @click="showAddAddress">新增地址</el-button>
      </div>
      
      <div class="address-list stagger-enter" v-if="addressList.length > 0">
        <div 
          v-for="addr in addressList" 
          :key="addr.id" 
          class="address-item"
          :class="{ active: selectedAddressId === addr.id }"
          @click="selectedAddressId = addr.id"
        >
          <div class="addr-name">{{ addr.receiverName }} <span class="phone">{{ addr.receiverPhone }}</span></div>
          <div class="addr-detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</div>
          <div class="default-tag" v-if="addr.isDefault">默认</div>
        </div>
      </div>
      <el-empty v-else description="暂无地址，请添加" />
    </div>

    <!-- 2. 商品清单 -->
    <div class="section-card panel-card">
      <div class="section-header">
        <h3>商品清单</h3>
      </div>
      
      <el-table :data="checkoutItems" style="width: 100%">
        <el-table-column label="商品" min-width="400">
          <template #default="{ row }">
             <div class="goods-info">
               <span class="goods-title">{{ row.goodsTitle }}</span>
             </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="150" align="center">
          <template #default="{ row }">¥ {{ row.goodsPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="数量" width="150" align="center" prop="quantity" />
        <el-table-column label="小计" width="150" align="center">
          <template #default="{ row }">
            <span class="subtotal">¥ {{ (row.goodsPrice * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 3. 底部结算 -->
    <div class="confirm-bar panel-card">
      <div class="price-info">
        <span class="label">应付金额：</span>
        <span class="total-price">¥ {{ totalPrice.toFixed(2) }}</span>
      </div>
      <el-button type="primary" size="large" :loading="submitting" @click="submitOrder">提交订单</el-button>
    </div>

    <!-- 地址弹窗 -->
    <el-dialog v-model="addressDialogVisible" title="新增收货地址" width="500px">
      <el-form :model="addressForm" label-width="96px" class="form-standard dialog-form">
        <el-form-item label="收货人">
          <el-input v-model="addressForm.receiverName" placeholder="姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addressForm.receiverPhone" placeholder="手机号码" />
        </el-form-item>
        <el-form-item label="地区">
           <!-- V1 简化为文本输入，后续可接入级联选择器 -->
           <el-row :gutter="10">
             <el-col :span="8"><el-input v-model="addressForm.province" placeholder="省" /></el-col>
             <el-col :span="8"><el-input v-model="addressForm.city" placeholder="市" /></el-col>
             <el-col :span="8"><el-input v-model="addressForm.district" placeholder="区" /></el-col>
           </el-row>
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="addressForm.detail" type="textarea" placeholder="街道门牌信息" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault" :true-label="1" :false-label="0">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addressDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAddress">确定</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped>

.section-card {
  padding: 20px;
  margin-bottom: 18px;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 15px;
}

.address-item {
  border: 1px solid var(--ldk-border);
  border-radius: 10px;
  padding: 15px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  min-height: 112px;
}

.address-item:hover {
  border-color: #c5d1e4;
}

.address-item.active {
  border-color: var(--ldk-primary);
  background-color: var(--ldk-primary-soft);
}

.addr-name {
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--ldk-text-primary);
}

.phone {
  margin-left: 10px;
  font-weight: normal;
}

.addr-detail {
  font-size: 14px;
  color: var(--ldk-text-regular);
  line-height: 1.5;
}

.default-tag {
  position: absolute;
  right: 10px;
  top: 10px;
  background: var(--ldk-primary);
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.subtotal {
  color: var(--ldk-danger);
  font-weight: bold;
}

.confirm-bar {
  padding: 14px 18px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  position: sticky;
  bottom: 10px;
  z-index: 20;
}

.price-info {
  display: flex;
  align-items: baseline;
}

.label {
  font-size: 16px;
}

.total-price {
  font-size: 32px;
  color: var(--ldk-danger);
  font-weight: bold;
  margin-left: 10px;
}

@media (max-width: 768px) {
  .confirm-bar {
    position: static;
    justify-content: space-between;
    flex-wrap: wrap;
  }

  .total-price {
    font-size: 28px;
  }
}
</style>
