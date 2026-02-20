<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, EditPen } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickName: ''
})

const loading = ref(false)

/**
 * 注册
 */
const handleRegister = async () => {
  // 表单验证
  if (!form.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (form.value.username.length < 3) {
    ElMessage.warning('用户名至少3个字符')
    return
  }
  if (form.value.username.length > 32) {
    ElMessage.warning('用户名最多32个字符')
    return
  }
  if (!form.value.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (form.value.password.length < 6 || form.value.password.length > 32) {
    ElMessage.warning('密码长度需在6-32个字符')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  if (form.value.nickName && form.value.nickName.length > 32) {
    ElMessage.warning('昵称最多32个字符')
    return
  }

  loading.value = true

  try {
    const success = await authStore.register({
      username: form.value.username,
      password: form.value.password,
      nickName: form.value.nickName
    })

    if (success) {
      ElMessage.success('注册成功，请登录')

      // 跳转登录页并带上 redirect
      router.push({
        path: '/login',
        query: { redirect: route.query.redirect }
      })
    }
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到登录页
 */
const goToLogin = () => {
  router.push({
    path: '/login',
    query: { redirect: route.query.redirect }
  })
}
</script>

<template>
  <div class="register-page">
    <div class="register-container panel-card">
      <div class="register-header">
        <h1>注册</h1>
        <p>创建账号后即可发布和购买商品</p>
      </div>

      <el-form :model="form" label-width="96px" class="register-form form-standard" @submit.prevent="handleRegister">
        <el-form-item label="用户名">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名（至少3个字符）"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（至少6个字符）"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input
            v-model="form.nickName"
            placeholder="请输入昵称"
            size="large"
            :prefix-icon="EditPen"
          />
        </el-form-item>

        <el-form-item class="form-actions">
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
            class="register-button"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="goToLogin">立即登录</el-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at 12% 12%, #dce9ff 0, rgba(220, 233, 255, 0) 34%),
    radial-gradient(circle at 86% 82%, #d9f1ff 0, rgba(217, 241, 255, 0) 34%),
    #f2f6fc;
}

.register-container {
  width: min(500px, 100%);
  padding: 34px 32px 28px;
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-header h1 {
  font-size: 30px;
  color: var(--ldk-text-primary);
  margin-bottom: 10px;
  font-weight: 600;
}

.register-header p {
  font-size: 14px;
  color: var(--ldk-text-secondary);
}

.register-form {
  margin-top: 20px;
}

.register-button {
  width: 100%;
  margin-top: 6px;
}

.register-footer {
  text-align: center;
  margin-top: 14px;
  font-size: 14px;
  color: var(--ldk-text-secondary);
}

.register-footer .el-link {
  margin-left: 8px;
}

@media (max-width: 768px) {
  .register-page {
    padding: 14px;
  }

  .register-container {
    padding: 24px 18px 20px;
  }
}
</style>
