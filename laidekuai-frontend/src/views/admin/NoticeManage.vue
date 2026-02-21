<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import noticeApi from '@/api/notice'

const loading = ref(false)
const noticeList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const dialogMode = ref('create')
const form = reactive({
  id: null,
  title: '',
  content: ''
})

const statusLabel = (status) => {
  if (status === 'PUBLISHED') return '已发布'
  if (status === 'OFFLINE') return '已下线'
  return '草稿'
}

const fetchNotices = async () => {
  loading.value = true
  try {
    const res = await noticeApi.listAdmin({ page: page.value, size: size.value })
    if (res.code === 0) {
      noticeList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialogMode.value = 'create'
  form.id = null
  form.title = ''
  form.content = ''
  dialogVisible.value = true
}

const openEdit = (notice) => {
  dialogMode.value = 'edit'
  form.id = notice.id
  form.title = notice.title
  form.content = notice.content
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }

  try {
    const res = form.id
      ? await noticeApi.update(form.id, { title: form.title, content: form.content })
      : await noticeApi.create({ title: form.title, content: form.content })
    if (res.code === 0) {
      ElMessage.success(form.id ? '公告已更新' : '公告已创建')
      dialogVisible.value = false
      fetchNotices()
    }
  } catch (error) {
    console.error('保存公告失败:', error)
  }
}

const handlePublish = async (notice) => {
  try {
    const res = await noticeApi.publish(notice.id)
    if (res.code === 0) {
      ElMessage.success('公告已发布')
      fetchNotices()
    }
  } catch (error) {
    console.error('发布公告失败:', error)
  }
}

const handleOffline = async (notice) => {
  try {
    const res = await noticeApi.offline(notice.id)
    if (res.code === 0) {
      ElMessage.success('公告已下线')
      fetchNotices()
    }
  } catch (error) {
    console.error('下线公告失败:', error)
  }
}

const handleDelete = async (notice) => {
  try {
    await ElMessageBox.confirm('确认删除该公告吗？', '提示', { type: 'warning' })
    const res = await noticeApi.remove(notice.id)
    if (res.code === 0) {
      ElMessage.success('公告已删除')
      fetchNotices()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公告失败:', error)
    }
  }
}

const handlePageChange = (val) => {
  page.value = val
  fetchNotices()
}

onMounted(() => {
  fetchNotices()
})
</script>

<template>
  <div class="notice-manage-page page-shell page-stack">
    <div class="page-header">
      <div>
        <h2>公告管理</h2>
        <p class="page-desc">发布与维护前台公告内容。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增公告</el-button>
    </div>

    <el-card class="table-card panel-card" shadow="never">
      <div class="table-container table-panel" v-loading="loading">
        <el-table :data="noticeList" row-key="id" stripe border empty-text="暂无公告">
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'OFFLINE' ? 'info' : 'warning'">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status !== 'PUBLISHED'" link type="success" @click="handlePublish(row)">发布</el-button>
              <el-button v-if="row.status === 'PUBLISHED'" link type="warning" @click="handleOffline(row)">下线</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增公告' : '编辑公告'" width="640px">
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" rows="6" placeholder="请输入公告内容" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
