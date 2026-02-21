import request from '@/utils/request'

/**
 * 争议处理相关 API
 */
export default {
  /**
   * 管理员争议列表
   */
  list(params) {
    return request({
      url: '/admin/disputes',
      method: 'get',
      params
    })
  }
}
