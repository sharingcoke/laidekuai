import request from '@/utils/request'

/**
 * 管理员用户管理 API
 */
export default {
  /**
   * 分页查询用户
   */
  list(params) {
    return request({
      url: '/admin/users',
      method: 'get',
      params
    })
  },

  /**
   * 更新用户状态
   */
  updateStatus(id, status) {
    return request({
      url: `/admin/users/${id}/status`,
      method: 'put',
      params: { status }
    })
  }
}
