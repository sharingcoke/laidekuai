import request from '@/utils/request'

/**
 * 用户认证API
 */
export default {
  /**
   * 用户登录
   */
  login(data) {
    return request({
      url: '/auth/login',
      method: 'post',
      data
    })
  },

  /**
   * 用户注册
   */
  register(data) {
    return request({
      url: '/auth/register',
      method: 'post',
      data
    })
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser() {
    return request({
      url: '/auth/me',
      method: 'get'
    })
  }
}
