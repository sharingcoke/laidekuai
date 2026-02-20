<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import goodsApi from '@/api/goods'
import CategorySelect from '@/components/common/CategorySelect.vue'
import { Edit, Delete, View, VideoPause, Position } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const goodsList = ref([])
const total = ref(0)
const activeTab = ref('all')

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: '',
  categoryId: null
})

const fetchMyGoods = async () => {
  loading.value = true
  try {
    const res = await goodsApi.listMy(queryParams)
    if (res.code === 0) {
      goodsList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取商品失败', error)
  } finally {
    loading.value = false
  }
}

const handleTabClick = (tab) => {
  queryParams.status = tab.props.name === 'all' ? '' : tab.props.name
  queryParams.page = 1
  queryParams.keyword = ''
  queryParams.categoryId = null
  fetchMyGoods()
}

const handleSearch = () => {
    queryParams.page = 1
    fetchMyGoods()
}

const handleCategoryChange = () => {
    queryParams.page = 1
    fetchMyGoods()
}

const handleEdit = (id) => {
    router.push(`/goods/edit/${id}`)
}

const handleDelete = (id) => {
    ElMessageBox.confirm('确定要删除该商品吗?', '提示', { type: 'warning' })
    .then(async () => {
        const res = await goodsApi.delete(id)
        if (res.code === 0) {
            ElMessage.success('删除成功')
            fetchMyGoods()
        }
    }).catch(() => {})
}

const handleOffline = (id) => {
    ElMessageBox.confirm('确定要下架该商品吗?', '提示', { type: 'warning' })
    .then(async () => {
        const res = await goodsApi.offline(id)
        if (res.code === 0) {
            ElMessage.success('下架成功')
            fetchMyGoods()
        }
    }).catch(() => {})
}

const handleSubmit = (id) => {
    ElMessageBox.confirm('确定要提交审核吗?', '提示', { type: 'info' })
    .then(async () => {
        const res = await goodsApi.submitForAudit(id)
        if (res.code === 0) {
            ElMessage.success('提交成功')
            fetchMyGoods()
        }
    }).catch(() => {})
}

const getMainImage = (urls) => {
    try {
        return JSON.parse(urls)[0]
    } catch {
        return ''
    }
}

const formatStatus = (status) => {
    const map = {
        'DRAFT': '草稿',
        'PENDING': '待审核',
        'APPROVED': '已上架',
        'REJECTED': '已驳回',
        'OFFLINE': '已下架'
    }
    return map[status] || status
}

onMounted(() => {
    fetchMyGoods()
})
</script>

<template>
  <div class="my-goods-page">
    <div class="page-header">
         <h2>我发布的商品</h2>
         <el-button type="primary" @click="router.push('/goods/publish')">发布新商品</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="goods-tabs">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="已上架" name="APPROVED"></el-tab-pane>
        <el-tab-pane label="待审核" name="PENDING"></el-tab-pane>
        <el-tab-pane label="草稿箱" name="DRAFT"></el-tab-pane>
        <el-tab-pane label="已下架" name="OFFLINE"></el-tab-pane>
    </el-tabs>

    <div class="search-bar">
        <CategorySelect
            v-model="queryParams.categoryId"
            class="category-select"
            placeholder="&#x9009;&#x62E9;&#x5206;&#x7C7B;"
            @change="handleCategoryChange"
        />
        <el-input v-model="queryParams.keyword" placeholder="&#x641C;&#x7D22;&#x5546;&#x54C1;&#x6807;&#x9898;" style="width: 300px" clearable @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">&#x641C;&#x7D22;</el-button>
    </div>

    <div class="goods-table" v-loading="loading">
        <el-table :data="goodsList" style="width: 100%">
            <el-table-column label="商品信息" min-width="300">
                <template #default="{ row }">
                    <div class="goods-info">
                        <el-image :src="getMainImage(row.imageUrls)" class="goods-img" fit="cover" />
                        <div class="goods-detail">
                            <div class="goods-title">{{ row.title }}</div>
                            <div class="goods-price">¥ {{ row.price }}</div>
                        </div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
                <template #default="{ row }">
                    <el-tag>{{ formatStatus(row.status) }}</el-tag>
                    <div v-if="row.status === 'REJECTED'" class="reject-reason" :title="row.auditReason">
                        驳回原因: {{ row.auditReason }}
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="库存" width="100" prop="stock" align="center" />
            <el-table-column label="创建时间" width="180" prop="createdAt" align="center" />
            <el-table-column label="操作" width="250" fixed="right">
                <template #default="{ row }">
                    <el-button link type="primary" :icon="View" @click="router.push(`/goods/${row.id}`)">查看</el-button>
                    
                    <template v-if="row.status === 'DRAFT' || row.status === 'REJECTED' || row.status === 'OFFLINE'">
                         <el-button link type="primary" :icon="Edit" @click="handleEdit(row.id)">编辑</el-button>
                         <el-button link type="danger" :icon="Delete" @click="handleDelete(row.id)">删除</el-button>
                    </template>
                    
                    <template v-if="row.status === 'DRAFT' || row.status === 'REJECTED'">
                        <el-button link type="success" :icon="Position" @click="handleSubmit(row.id)">提交</el-button>
                    </template>

                    <template v-if="row.status === 'APPROVED'">
                         <el-button link type="warning" :icon="VideoPause" @click="handleOffline(row.id)">下架</el-button>
                    </template>
                </template>
            </el-table-column>
        </el-table>
    </div>

    <div class="pagination-container" v-if="total > 0">
         <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            layout="prev, pager, next, total"
            :total="total"
            @current-change="fetchMyGoods"
            background
         />
    </div>
  </div>
</template>

<style scoped>
.my-goods-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.search-bar {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
    align-items: center;
    flex-wrap: wrap;
}

.category-select {
    min-width: 200px;
}
.goods-info {
    display: flex;
    gap: 10px;
    align-items: center;
}
.goods-img {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    background: #f5f7fa;
}
.goods-title {
    font-size: 14px;
    font-weight: bold;
    color: #333;
    line-height: 1.4;
}
.goods-price {
    color: #f56c6c;
    margin-top: 5px;
}
.reject-reason {
    font-size: 12px;
    color: #f56c6c;
    margin-top: 5px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 100px;
}
.pagination-container {
    margin-top: 20px;
    text-align: right;
}
</style>
