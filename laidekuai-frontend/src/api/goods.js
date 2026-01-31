import request from '@/utils/request'

/**
 * 商品相关API
 */
export default {
  /**
   * 创建商品
   */
  create(data) {
    return request({
      url: '/goods',
      method: 'post',
      data
    })
  },

  /**
   * 更新商品
   */
  update(id, data) {
    return request({
      url: `/goods/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 提交审核
   */
  submitForAudit(id) {
    return request({
      url: `/goods/${id}/submit`,
      method: 'post'
    })
  },

  /**
   * 删除/下架商品
   */
  delete(id) {
    return request({
      url: `/goods/${id}`,
      method: 'delete'
    })
  },

  /**
   * 获取商品详情
   */
  detail(id) {
    return request({
      url: `/goods/${id}`,
      method: 'get'
    })
  },

  /**
   * 商品列表/搜索 (公开)
   */
  list(params) {
    return request({
      url: '/goods',
      method: 'get',
      params
    })
  },

  /**
   * 管理员审核通过
   */
  approve(id) {
    return request({
      url: `/goods/admin/${id}/approve`,
      method: 'post'
    })
  },

  /**
   * 管理员审核驳回
   */
  reject(id, reason) {
    return request({
      url: `/goods/admin/${id}/reject`,
      method: 'post',
      data: { reason }
    })
  },

  /**
   * 我的商品列表
   */
  listMy(params) {
    return request({
      url: '/goods/my',
      method: 'get',
      params
    })
  },

  /**
   * 管理员商品列表
   */
  listAdmin(params) {
    return request({
      url: '/goods/admin/list',
      method: 'get',
      params
    })
  },

  /**
   * 下架商品
   */
  offline(id) {
    return request({
      url: `/goods/${id}/offline`,
      method: 'post'
    })
  }
}
