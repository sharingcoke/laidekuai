<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import goodsApi from '@/api/goods'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const goodsList = ref([])
const total = ref(0)
const activeTab = ref('PENDING')

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: 'PENDING'
})

const fetchGoods = async () => {
    loading.value = true
    try {
        const res = await goodsApi.listAdmin(queryParams)
        if (res.code === 0) {
            goodsList.value = res.data.records
            total.value = res.data.total
        }
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}

const handleTabClick = (tab) => {
    queryParams.status = tab.props.name === 'all' ? '' : tab.props.name
    queryParams.page = 1
    fetchGoods()
}

const getMainImage = (urls) => { 
    try { return JSON.parse(urls)[0] } catch { return '' }
}

const formatStatus = (status) => {
    const map = { 'DRAFT': '草稿', 'PENDING': '待审核', 'APPROVED': '已上架', 'REJECTED': '已驳回', 'OFFLINE': '已下架' }
    return map[status] || status
}

const handleApprove = (id) => {
    ElMessageBox.confirm('确定通过审核并上架吗?', '提示', { type: 'success' })
    .then(async () => {
        const res = await goodsApi.approve(id)
        if (res.code === 0) {
            ElMessage.success('审核通过')
            fetchGoods()
        }
    }).catch(() => {})
}

const handleReject = (id) => {
    ElMessageBox.prompt('请输入驳回原因', '驳回审核', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /\S/,
        inputErrorMessage: '原因不能为空'
    }).then(async ({ value }) => {
        const res = await goodsApi.reject(id, value)
        if (res.code === 0) {
            ElMessage.success('已驳回')
            fetchGoods()
        }
    }).catch(() => {})
}

onMounted(() => {
    fetchGoods()
})
</script>

<template>
  <div class="goods-audit-page page-shell">
     <div class="page-header">
       <div>
         <h2>商品审核与管理</h2>
         <p class="page-desc">按待审优先展示，审核与检索区域分层更明确。</p>
       </div>
     </div>

     <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="audit-tabs panel-card tabs-panel">
         <el-tab-pane label="待审核" name="PENDING"></el-tab-pane>
         <el-tab-pane label="全部商品" name="all"></el-tab-pane>
     </el-tabs>

     <div class="search-bar search-panel panel-card toolbar-panel">
         <el-input v-model="queryParams.keyword" placeholder="搜索商品标题" style="width: 300px" clearable @change="fetchGoods" />
         <el-button type="primary" @click="fetchGoods">搜索</el-button>
     </div>

     <div class="table-container panel-card table-panel" v-loading="loading">
         <el-table :data="goodsList" style="width: 100%">
             <el-table-column label="商品信息" min-width="300">
                <template #default="{ row }">
                   <div class="goods-info">
                       <el-image :src="getMainImage(row.imageUrls)" class="goods-img" fit="cover" />
                       <div class="goods-detail">
                           <div class="goods-title">{{ row.title }}</div>
                           <div class="seller">卖家ID: {{ row.sellerId }}</div>
                       </div>
                   </div>
                </template>
             </el-table-column>
             <el-table-column label="价格" width="100" prop="price" />
             <el-table-column label="状态" width="100">
                 <template #default="{ row }">
                     <el-tag>{{ formatStatus(row.status) }}</el-tag>
                 </template>
             </el-table-column>
             <el-table-column label="提交时间" width="180" prop="updatedAt" />
             <el-table-column label="操作" width="200" fixed="right">
                 <template #default="{ row }">
                     <el-button link type="primary" @click="router.push(`/goods/${row.id}`)">查看</el-button>
                     <template v-if="row.status === 'PENDING'">
                         <el-button link type="success" @click="handleApprove(row.id)">通过</el-button>
                         <el-button link type="danger" @click="handleReject(row.id)">驳回</el-button>
                     </template>
                 </template>
             </el-table-column>
         </el-table>
         
         <div class="pagination-container" v-if="total > 0">
             <el-pagination
                v-model:current-page="queryParams.page"
                v-model:page-size="queryParams.size"
                layout="prev, pager, next, total"
                :total="total"
                @current-change="fetchGoods"
                background
            />
         </div>
     </div>
  </div>
</template>

<style scoped>
.audit-tabs {
    margin-bottom: 16px;
}

.search-panel {
    margin-bottom: 16px;
}

.table-container {
    padding-top: 12px;
    padding-bottom: 12px;
}
.goods-info {
    display: flex;
    gap: 12px;
    align-items: center;
}
.goods-img {
    width: 64px;
    height: 64px;
    border-radius: 8px;
    border: 1px solid var(--ldk-border);
}
.goods-detail {
    overflow: hidden;
}
.goods-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 5px;
    color: var(--ldk-text-primary);
}
.seller {
    font-size: 12px;
    color: #909399;
}
.pagination-container {
    margin-top: 16px;
}
</style>
