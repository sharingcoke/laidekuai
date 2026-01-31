import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authApi from '@/api/auth'

/**
 * 认证Store
 */
export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isSeller = computed(() => user.value?.role === 'SELLER')
  const isBuyer = computed(() => user.value?.role === 'BUYER')

  // Actions
  /**
   * 登录
   */
  async login(credentials) {
    const res = await authApi.login(credentials)

    if (res.code === 0) {
      token.value = res.data.token
      user.value = res.data.user

      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))

      return true
    }

    return false
  }

  /**
   * 注册
   */
  async register(userData) {
    const res = await authApi.register(userData)

    if (res.code === 0) {
      token.value = res.data.token
      user.value = res.data.user

      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))

      return true
    }

    return false
  }

  /**
   * 获取当前用户信息
   */
  async fetchCurrentUser() {
    const res = await authApi.getCurrentUser()

    if (res.code === 0) {
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
      return true
    }

    return false
  }

  /**
   * 退出登录
   */
  logout() {
    token.value = ''
    user.value = null

    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    // State
    token,
    user,
    // Getters
    isLoggedIn,
    isAdmin,
    isSeller,
    isBuyer,
    // Actions
    login,
    register,
    fetchCurrentUser,
    logout
  }
})
