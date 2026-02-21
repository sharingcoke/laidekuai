import request from '@/utils/request'

export default {
  create(goodsId, content) {
    return request({
      url: '/messages',
      method: 'post',
      data: { goodsId, content }
    })
  },

  listGoods(goodsId, params) {
    return request({
      url: `/goods/${goodsId}/messages`,
      method: 'get',
      params
    })
  },

  reply(messageId, content) {
    return request({
      url: `/messages/${messageId}/replies`,
      method: 'post',
      data: { content }
    })
  },

  listAdmin(params) {
    return request({
      url: '/admin/messages',
      method: 'get',
      params
    })
  },

  listAdminReplies(params) {
    return request({
      url: '/admin/message-replies',
      method: 'get',
      params
    })
  },

  hide(id) {
    return request({
      url: `/admin/messages/${id}/hide`,
      method: 'post'
    })
  },

  remove(id) {
    return request({
      url: `/admin/messages/${id}/delete`,
      method: 'post'
    })
  },

  hideReply(id) {
    return request({
      url: `/admin/message-replies/${id}/hide`,
      method: 'post'
    })
  },

  removeReply(id) {
    return request({
      url: `/admin/message-replies/${id}/delete`,
      method: 'post'
    })
  }
}
