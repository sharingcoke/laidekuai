<script setup>
import { ref, onMounted, reactive } from 'vue'
import userApi from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const activeTab = ref('all')
const queryParams = reactive({
  page: 1,
  size: 10,
  status: ''
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page,
      size: queryParams.size,
      status: queryParams.status || undefined
    }
    const res = await userApi.list(params)
    if (res.code === 0) {
      userList.value = res.data.records
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
  fetchUsers()
}

const handleDisable = (user) => {
  ElMessageBox.confirm(`确定禁用用户 ${user.username} 吗?`, '警告', {
    type: 'warning'
  }).then(async () => {
    const res = await userApi.disable(user.id)
    if (res.code === 0) {
      ElMessage.success('用户已禁用')
      fetchUsers()
    }
  }).catch(() => {})
}

const handleEnable = (user) => {
  ElMessageBox.confirm(`确定启用用户 ${user.username} 吗?`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await userApi.enable(user.id)
    if (res.code === 0) {
      ElMessage.success('用户已启用')
      fetchUsers()
    }
  }).catch(() => {})
}

const handleResetPassword = (user) => {
  ElMessageBox.confirm(`确定重置用户 ${user.username} 的密码吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await userApi.resetPassword(user.id)
    if (res.code === 0) {
      ElMessage.success('密码已重置为默认值')
    }
  }).catch(() => {})
}

const handleCurrentChange = (val) => {
  queryParams.page = val
  fetchUsers()
}

onMounted(() => {
    fetchUsers()
})
</script>

<template>
  <div class="user-list-page page-shell">
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p class="page-desc">用户状态、角色和注册信息统一按表格管理。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="tabs-panel" @tab-click="handleTabClick">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="正常" name="ACTIVE" />
      <el-tab-pane label="禁用" name="DISABLED" />
    </el-tabs>

    <div class="table-container panel-card table-panel" v-loading="loading">
       <el-table :data="userList" border style="width: 100%">
           <el-table-column prop="id" label="ID" width="80" align="center" />
           <el-table-column prop="username" label="用户名" width="150" />
           <el-table-column prop="nickName" label="昵称" width="150" />
           <el-table-column prop="role" label="角色" width="100" align="center">
               <template #default="{ row }">
                   <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">{{ row.role }}</el-tag>
               </template>
           </el-table-column>
           <el-table-column prop="phone" label="手机号" width="150" />
           <el-table-column prop="status" label="状态" width="100" align="center">
               <template #default="{ row }">
                   <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
                       {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
                   </el-tag>
               </template>
           </el-table-column>
           <el-table-column prop="createdAt" label="注册时间" width="180" />
           <el-table-column label="操作" min-width="150">
               <template #default="{ row }">
                   <el-button
                     v-if="row.role !== 'ADMIN' && row.status === 'ACTIVE'"
                     type="danger"
                     size="small"
                     @click="handleDisable(row)"
                   >
                     禁用
                   </el-button>
                   <el-button
                     v-if="row.role !== 'ADMIN' && row.status === 'DISABLED'"
                     type="success"
                     size="small"
                     @click="handleEnable(row)"
                   >
                     启用
                   </el-button>
                   <el-button
                     v-if="row.role !== 'ADMIN'"
                     size="small"
                     @click="handleResetPassword(row)"
                   >
                     重置密码
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
                @current-change="fetchUsers"
                background
           />
       </div>
    </div>
  </div>
</template>

<style scoped>
.table-container {
    padding-top: 12px;
    padding-bottom: 12px;
}
.tabs-panel {
    margin-bottom: 16px;
    background: white;
    padding: 6px 16px;
    border-radius: 8px;
}
.pagination-container {
    margin-top: 16px;
}
</style>
