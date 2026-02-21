import request from '@/utils/request'

/**
 * 公告API
 */
export default {
    /**
     * 已发布公告列表
     */
    list(params) {
        return request({
            url: '/notices',
            method: 'get',
            params
        })
    },

    /**
     * 公告详情
     */
  detail(id) {
    return request({
      url: `/notices/${id}`,
      method: 'get'
    })
  },

  /**
   * 管理端公告列表
   */
  listAdmin(params) {
    return request({
      url: '/admin/notices',
      method: 'get',
      params
    })
  },

  /**
   * 创建公告
   */
  create(data) {
    return request({
      url: '/admin/notices',
      method: 'post',
      data
    })
  },

  /**
   * 更新公告
   */
  update(id, data) {
    return request({
      url: `/admin/notices/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除公告
   */
  remove(id) {
    return request({
      url: `/admin/notices/${id}`,
      method: 'delete'
    })
  },

  /**
   * 发布公告
   */
  publish(id) {
    return request({
      url: `/admin/notices/${id}/publish`,
      method: 'post'
    })
  },

  /**
   * 下线公告
   */
  offline(id) {
    return request({
      url: `/admin/notices/${id}/offline`,
      method: 'post'
    })
  }
}
