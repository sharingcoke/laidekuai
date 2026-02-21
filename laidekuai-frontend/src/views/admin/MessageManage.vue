<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import messageApi from '@/api/message'

const loading = ref(false)
const messageList = ref([])
const total = ref(0)
const activeScope = ref('messages')
const activeStatus = ref('all')

const queryParams = reactive({
  page: 1,
  size: 10,
  status: ''
})

const fetchMessages = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page,
      size: queryParams.size,
      status: queryParams.status || undefined
    }
    const res = activeScope.value === 'messages'
      ? await messageApi.listAdmin(params)
      : await messageApi.listAdminReplies(params)
    if (res.code === 0) {
      messageList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取留言治理列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleScopeChange = (tab) => {
  activeScope.value = tab.props.name
  queryParams.page = 1
  fetchMessages()
}

const handleStatusChange = (tab) => {
  queryParams.status = tab.props.name === 'all' ? '' : tab.props.name
  queryParams.page = 1
  fetchMessages()
}

const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchMessages()
}

const handleHide = (row) => {
  ElMessageBox.confirm('确定隐藏该内容吗？', '提示', { type: 'warning' })
    .then(async () => {
      const res = activeScope.value === 'messages'
        ? await messageApi.hide(row.id)
        : await messageApi.hideReply(row.id)
      if (res.code === 0) {
        ElMessage.success('已隐藏')
        fetchMessages()
      }
    })
    .catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该内容吗？删除后不可恢复。', '提示', { type: 'warning' })
    .then(async () => {
      const res = activeScope.value === 'messages'
        ? await messageApi.remove(row.id)
        : await messageApi.removeReply(row.id)
      if (res.code === 0) {
        ElMessage.success('已删除')
        fetchMessages()
      }
    })
    .catch(() => {})
}

const formatStatus = (status) => {
  const map = {
    visible: '可见',
    hidden: '隐藏',
    deleted: '已删除'
  }
  return map[status] || status
}

const statusType = (status) => {
  if (status === 'visible') return 'success'
  if (status === 'hidden') return 'warning'
  if (status === 'deleted') return 'info'
  return ''
}

onMounted(() => {
  fetchMessages()
})
</script>

<template>
  <div class="message-manage-page page-shell page-stack">
    <div class="page-header">
      <div>
        <h2>留言治理</h2>
        <p class="page-desc">管理商品留言与回复内容。</p>
      </div>
    </div>

    <el-tabs v-model="activeScope" class="panel-card tabs-panel" @tab-click="handleScopeChange">
      <el-tab-pane label="留言" name="messages" />
      <el-tab-pane label="回复" name="replies" />
    </el-tabs>

    <el-tabs v-model="activeStatus" class="panel-card tabs-panel" @tab-click="handleStatusChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="可见" name="visible" />
      <el-tab-pane label="隐藏" name="hidden" />
      <el-tab-pane label="已删除" name="deleted" />
    </el-tabs>

    <el-card class="table-card panel-card" shadow="never">
      <div class="table-container table-panel" v-loading="loading">
        <el-table
          :data="messageList"
          style="width: 100%"
          stripe
          border
          row-key="id"
          empty-text="暂无数据"
        >
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column v-if="activeScope === 'messages'" prop="goodsId" label="商品ID" width="120" />
          <el-table-column v-if="activeScope === 'replies'" prop="parentId" label="留言ID" width="120" />
          <el-table-column prop="senderName" label="用户" width="140" show-overflow-tooltip />
          <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ formatStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'visible'"
                size="small"
                @click="handleHide(row)"
              >
                隐藏
              </el-button>
              <el-button
                v-if="row.status !== 'deleted'"
                size="small"
                type="danger"
                plain
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

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
    </el-card>
  </div>
</template>

<style scoped>
.table-container {
  min-height: 260px;
}
</style>
