<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import addressApi from '@/api/address'

const loading = ref(false)
const addressList = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

const resetForm = () => {
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detail = ''
  form.isDefault = 0
  editingId.value = null
}

const fetchAddresses = async () => {
  loading.value = true
  try {
    const res = await addressApi.list()
    if (res.code === 0) {
      addressList.value = res.data || []
    }
  } catch (error) {
    console.error('获取地址失败:', error)
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (addr) => {
  editingId.value = addr.id
  form.receiverName = addr.receiverName
  form.receiverPhone = addr.receiverPhone
  form.province = addr.province
  form.city = addr.city
  form.district = addr.district
  form.detail = addr.detail
  form.isDefault = addr.isDefault ? 1 : 0
  dialogVisible.value = true
}

const saveAddress = async () => {
  if (!form.receiverName || !form.receiverPhone || !form.detail) {
    ElMessage.warning('请完善收货人、电话与详细地址')
    return
  }
  const payload = {
    receiverName: form.receiverName,
    receiverPhone: form.receiverPhone,
    province: form.province,
    city: form.city,
    district: form.district,
    detail: form.detail,
    isDefault: form.isDefault
  }
  try {
    const res = editingId.value
      ? await addressApi.update(editingId.value, payload)
      : await addressApi.add(payload)
    if (res.code === 0) {
      ElMessage.success(editingId.value ? '地址已更新' : '地址已新增')
      dialogVisible.value = false
      fetchAddresses()
    }
  } catch (error) {
    console.error('保存地址失败:', error)
  }
}

const setDefault = async (addr) => {
  const res = await addressApi.setDefault(addr.id)
  if (res.code === 0) {
    ElMessage.success('已设为默认地址')
    fetchAddresses()
  }
}

const deleteAddress = (addr) => {
  ElMessageBox.confirm('确定删除该地址吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await addressApi.delete(addr.id)
    if (res.code === 0) {
      ElMessage.success('地址已删除')
      fetchAddresses()
    }
  }).catch(() => {})
}

const maskPhone = (phone) => {
  if (!phone || phone.length < 7) return phone || ''
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}

const fullAddress = (addr) => {
  return `${addr.province || ''}${addr.city || ''}${addr.district || ''}${addr.detail || ''}`
}

onMounted(() => {
  fetchAddresses()
})
</script>

<template>
  <div class="address-page">
    <div class="page-header">
      <div>
        <h2>地址管理</h2>
        <p class="page-desc">维护收货地址与默认地址。</p>
      </div>
      <el-button type="primary" @click="openAdd">新增地址</el-button>
    </div>

    <div class="address-list" v-loading="loading">
      <el-empty v-if="!loading && addressList.length === 0" description="暂无地址" />
      <div v-else class="address-card" v-for="addr in addressList" :key="addr.id">
        <div class="info">
          <div class="line">
            <span class="name">{{ addr.receiverName }}</span>
            <span class="phone">{{ maskPhone(addr.receiverPhone) }}</span>
            <el-tag v-if="addr.isDefault" size="small" type="success">默认</el-tag>
          </div>
          <div class="detail">{{ fullAddress(addr) }}</div>
        </div>
        <div class="actions">
          <el-button size="small" @click="openEdit(addr)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteAddress(addr)">删除</el-button>
          <el-button
            v-if="!addr.isDefault"
            size="small"
            type="primary"
            plain
            @click="setDefault(addr)"
          >
            设为默认
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑地址' : '新增地址'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="收货人">
          <el-input v-model="form.receiverName" placeholder="姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.receiverPhone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="所在地区">
          <div class="region-row">
            <el-input v-model="form.province" placeholder="省" />
            <el-input v-model="form.city" placeholder="市" />
            <el-input v-model="form.district" placeholder="区" />
          </div>
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.detail" type="textarea" placeholder="街道门牌信息" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.isDefault" :true-label="1" :false-label="0">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.address-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
}

.page-desc {
  margin: 8px 0 0;
  font-size: 13px;
  color: #909399;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-card {
  background: white;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
}

.info .line {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.name {
  font-weight: 600;
  color: #1f2937;
}

.phone {
  color: #6b7280;
}

.detail {
  color: #374151;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.region-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  width: 100%;
}
</style>
