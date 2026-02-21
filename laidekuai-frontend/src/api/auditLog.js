import request from '@/utils/request'

/**
 * 审计日志 API
 */
export default {
  list(params) {
    return request({
      url: '/admin/audit-logs',
      method: 'get',
      params
    })
  }
}
