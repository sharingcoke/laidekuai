<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import userApi from '@/api/user'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const activeTab = ref('info')
const loading = ref(false)

const infoForm = reactive({
  nickName: '',
  phone: '',
  avatar: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(() => {
  if (authStore.user) {
    infoForm.nickName = authStore.user.nickName
    infoForm.phone = authStore.user.phone
    infoForm.avatar = authStore.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
})

/**
 * 更新信息
 */
const updateInfo = async () => {
    loading.value = true
    try {
        const res = await userApi.update(authStore.user.id, {
            nickName: infoForm.nickName,
            phone: infoForm.phone,
            avatar: infoForm.avatar
        })
        if (res.code === 0) {
            ElMessage.success('更新成功')
            // 更新store
            authStore.fetchCurrentUser()
        }
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}

/**
 * 修改密码
 */
const updatePassword = async () => {
    if (pwdForm.newPassword !== pwdForm.confirmPassword) {
        ElMessage.warning('两次密码不一致')
        return
    }
    if (pwdForm.newPassword.length < 6) {
        ElMessage.warning('密码至少6位')
        return
    }

    try {
        const res = await userApi.changePassword(authStore.user.id, {
            oldPassword: pwdForm.oldPassword,
            newPassword: pwdForm.newPassword
        })
        if (res.code === 0) {
            ElMessage.success('密码修改成功，请重新登录')
            authStore.logout()
            window.location.href = '/login'
        }
    } catch (error) {
        console.error(error)
    }
}
</script>

<template>
  <div class="profile-page page-shell">
    <div class="page-header">
      <div>
        <h2>个人中心</h2>
        <p class="page-desc">资料、密码和常用入口分区展示，结构更清晰。</p>
      </div>
    </div>

    <div class="profile-container">
      <div class="sidebar panel-card">
        <div class="user-card">
           <el-avatar :size="80" :src="infoForm.avatar" />
           <div class="username">{{ authStore.user?.username }}</div>
           <div class="role-tag">{{ authStore.user?.roleName || '普通用户' }}</div>
        </div>
        
        <el-menu :default-active="activeTab" class="profile-menu" @select="(idx) => activeTab = idx">
          <el-menu-item index="info">
            <span>基本资料</span>
          </el-menu-item>
          <el-menu-item index="password">
            <span>修改密码</span>
          </el-menu-item>
          <el-menu-item index="my-goods" @click="$router.push('/my-goods')">
            <span>我发布的商品</span>
          </el-menu-item>
          <el-menu-item index="my-orders" @click="$router.push('/orders')">
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="my-address" @click="$router.push('/addresses')">
            <span>地址管理</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.isAdmin" index="admin" @click="$router.push('/admin/orders')">
            <span>管理端入口</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="content-area panel-card">
         <div v-if="activeTab === 'info'" class="tab-content">
            <h3>基本资料</h3>
            <el-form label-width="96px" class="form-standard profile-form">
                <el-form-item label="昵称">
                    <el-input v-model="infoForm.nickName" />
                </el-form-item>
                <el-form-item label="手机号">
                    <el-input v-model="infoForm.phone" />
                </el-form-item>
                <el-form-item label="头像URL">
                     <el-input v-model="infoForm.avatar" placeholder="输入图片地址" />
                </el-form-item>
                <el-form-item class="form-actions">
                    <el-button type="primary" :loading="loading" @click="updateInfo">保存修改</el-button>
                </el-form-item>
            </el-form>
         </div>

         <div v-if="activeTab === 'password'" class="tab-content">
            <h3>修改密码</h3>
             <el-form label-width="96px" class="form-standard profile-form">
                <el-form-item label="原密码">
                    <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码">
                    <el-input v-model="pwdForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认新密码">
                    <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item class="form-actions">
                    <el-button type="primary" @click="updatePassword">修改密码</el-button>
                </el-form-item>
            </el-form>
         </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  display: flex;
  gap: 18px;
  min-height: 500px;
}

.sidebar {
  width: 250px;
  padding: 20px 0;
}

.user-card {
  text-align: center;
  padding: 0 20px 20px;
  border-bottom: 1px solid var(--ldk-border);
  margin-bottom: 10px;
}

.username {
  font-size: 18px;
  font-weight: 600;
  margin-top: 10px;
  color: var(--ldk-text-primary);
}

.role-tag {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.profile-menu {
  border-right: none;
}

.content-area {
  flex: 1;
  padding: 26px 28px;
}

.profile-form {
  max-width: 520px;
}

.tab-content h3 {
  margin-top: 0;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--ldk-border);
  color: var(--ldk-text-primary);
}

@media (max-width: 900px) {
  .profile-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .content-area {
    padding: 18px 16px;
  }
}
</style>
