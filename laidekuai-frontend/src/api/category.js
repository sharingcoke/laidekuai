import request from '@/utils/request'

/**
 * 分类相关 API
 */
export default {
  /**
   * 获取分类树（公开）
   */
  listTree() {
    return request({
      url: '/categories',
      method: 'get'
    })
  },

  /**
   * 获取分类详情（公开）
   */
  detail(id) {
    return request({
      url: `/categories/${id}`,
      method: 'get'
    })
  }
}
