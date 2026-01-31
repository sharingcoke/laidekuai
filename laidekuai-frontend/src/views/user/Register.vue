<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

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
  if (!form.value.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (form.value.password.length < 6) {
    ElMessage.warning('密码至少6个字符')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  if (!form.value.nickName) {
    ElMessage.warning('请输入昵称')
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
      ElMessage.success('注册成功')

      // 跳转到之前的页面或首页
      const redirect = route.query.redirect || '/'
      router.push(redirect)
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
    <div class="register-container">
      <div class="register-header">
        <h1>注册</h1>
        <p>加入来得快</p>
      </div>

      <el-form :model="form" class="register-form" @submit.prevent="handleRegister">
        <el-form-item label="用户名">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名（至少3个字符）"
            size="large"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（至少6个字符）"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input
            v-model="form.nickName"
            placeholder="请输入昵称"
            size="large"
          />
        </el-form-item>

        <el-form-item>
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-container {
  width: 450px;
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.register-header p {
  font-size: 14px;
  color: #999;
}

.register-form {
  margin-top: 30px;
}

.register-button {
  width: 100%;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.register-footer .el-link {
  margin-left: 8px;
}
</style>
