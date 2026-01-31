<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import adminUserApi from '@/api/adminUser'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const users = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref('')

const isAdmin = computed(() => authStore.isAdmin)

const fetchUsers = async () => {
  if (!isAdmin.value) return
  loading.value = true
  try {
    const res = await adminUserApi.list({ page: page.value, size: size.value, status: statusFilter.value || undefined })
    if (res.code === 0) {
      users.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  const nextStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  try {
    await ElMessageBox.confirm(`确定将用户 ${row.username} 状态改为 ${nextStatus === 'ACTIVE' ? '启用' : '禁用'}?`, '确认操作', {
      type: 'warning'
    })
    const res = await adminUserApi.updateStatus(row.id, nextStatus)
    if (res.code === 0) {
      ElMessage.success('状态更新成功')
      fetchUsers()
    }
  } catch (e) {
    // 用户取消或接口异常
  }
}

const handleSizeChange = (val) => {
  size.value = val
  page.value = 1
  fetchUsers()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="admin-user-page">
    <header class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>查看并管理平台用户状态</p>
      </div>
    </header>

    <section class="toolbar">
      <el-select v-model="statusFilter" placeholder="全部状态" clearable size="small" @change="fetchUsers">
        <el-option label="ACTIVE" value="ACTIVE" />
        <el-option label="DISABLED" value="DISABLED" />
      </el-select>
    </section>

    <el-table :data="users" stripe border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickName" label="昵称" min-width="120" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.role === 'ADMIN'" type="danger">ADMIN</el-tag>
          <el-tag v-else-if="row.role === 'BUYER'" type="success">BUYER</el-tag>
          <el-tag v-else type="info">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'warning'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" plain @click="toggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        :current-page="page"
        :page-size="size"
        :page-sizes="[10, 20, 30, 50]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped>
.admin-user-page {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.page-header p {
  margin: 4px 0 0;
  color: #888;
}

.toolbar {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
