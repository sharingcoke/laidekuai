<script setup>
import { ref, onMounted, reactive } from 'vue'
import userApi from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  status: ''
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await userApi.list(queryParams)
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

const handleStatusChange = (user) => {
    const newStatus = user.status === 'NORMAL' ? 'LOCKED' : 'NORMAL'
    const actionText = newStatus === 'NORMAL' ? '解封' : '封禁'
    
    ElMessageBox.confirm(`确定要${actionText}用户 ${user.username} 吗?`, '警告', {
        type: 'warning'
    }).then(async () => {
        const res = await userApi.updateStatus(user.id, newStatus)
        if (res.code === 0) {
            ElMessage.success('操作成功')
            user.status = newStatus
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
                   <el-tag :type="row.status === 'NORMAL' ? 'success' : 'danger'">
                       {{ row.status === 'NORMAL' ? '正常' : '封禁' }}
                   </el-tag>
               </template>
           </el-table-column>
           <el-table-column prop="createdAt" label="注册时间" width="180" />
           <el-table-column label="操作" min-width="150">
               <template #default="{ row }">
                   <el-button 
                     v-if="row.role !== 'ADMIN'"
                     :type="row.status === 'NORMAL' ? 'danger' : 'success'" 
                     size="small" 
                     @click="handleStatusChange(row)"
                   >
                     {{ row.status === 'NORMAL' ? '封禁' : '解封' }}
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
.pagination-container {
    margin-top: 16px;
}
</style>
