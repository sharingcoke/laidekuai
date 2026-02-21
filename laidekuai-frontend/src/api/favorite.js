import request from '@/utils/request'

/**
 * 收藏API
 */
export default {
  list(params) {
    return request({
      url: '/favorites',
      method: 'get',
      params
    })
  },
  add(goodsId) {
    return request({
      url: `/favorites/${goodsId}`,
      method: 'post'
    })
  },
  remove(goodsId) {
    return request({
      url: `/favorites/${goodsId}`,
      method: 'delete'
    })
  },
  check(goodsId) {
    return request({
      url: `/favorites/${goodsId}/check`,
      method: 'get'
    })
  }
}
