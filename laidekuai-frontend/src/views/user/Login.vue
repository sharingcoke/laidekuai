<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)

/**
 * 登录
 */
const handleLogin = async () => {
  // 表单验证
  if (!form.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (form.value.username.length < 3 || form.value.username.length > 32) {
    ElMessage.warning('用户名长度需在3-32个字符')
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

  loading.value = true

  try {
    const success = await authStore.login(form.value)

    if (success) {
      ElMessage.success('登录成功')

      // 跳转到之前的页面或首页
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到注册页
 */
const goToRegister = () => {
  router.push({
    path: '/register',
    query: { redirect: route.query.redirect }
  })
}
</script>

<template>
  <div class="login-page">
    <div class="login-container panel-card">
      <div class="login-header">
        <h1>登录</h1>
        <p>欢迎回到来得快，继续你的交易流程</p>
      </div>

      <el-form :model="form" class="login-form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item class="form-actions">
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-button"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="goToRegister">立即注册</el-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at 14% 8%, #dce9ff 0, rgba(220, 233, 255, 0) 36%),
    radial-gradient(circle at 90% 82%, #d9f1ff 0, rgba(217, 241, 255, 0) 34%),
    #f2f6fc;
}

.login-container {
  width: min(440px, 100%);
  padding: 34px 32px 28px;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-header h1 {
  font-size: 30px;
  color: var(--ldk-text-primary);
  margin-bottom: 10px;
  font-weight: 600;
}

.login-header p {
  font-size: 14px;
  color: var(--ldk-text-secondary);
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  margin-top: 6px;
}

.login-footer {
  text-align: center;
  margin-top: 14px;
  font-size: 14px;
  color: var(--ldk-text-secondary);
}

.login-footer .el-link {
  margin-left: 8px;
}

@media (max-width: 768px) {
  .login-page {
    padding: 14px;
  }

  .login-container {
    padding: 24px 18px 20px;
  }
}
</style>
