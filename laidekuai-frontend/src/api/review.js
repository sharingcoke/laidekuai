import request from '@/utils/request'

/**
 * 评价相关API（管理员）
 */
export default {
    /**
     * 创建评价
     */
    create(data) {
        return request({
            url: '/reviews',
            method: 'post',
            data
        })
    },

    /**
     * 我的评价列表
     */
    listMy(params) {
        return request({
            url: '/reviews/my',
            method: 'get',
            params
        })
    },

    /**
     * 商品评价列表
     */
    listGoods(goodsId, params) {
        return request({
            url: `/reviews/goods/${goodsId}`,
            method: 'get',
            params
        })
    },

    /**
     * 商品评分
     */
    getGoodsRating(goodsId) {
        return request({
            url: `/reviews/goods/${goodsId}/rating`,
            method: 'get'
        })
    },

    /**
     * 卖家回复评价
     */
    reply(id, sellerReply) {
        return request({
            url: `/reviews/${id}/reply`,
            method: 'post',
            data: { sellerReply }
        })
    },

    /**
     * 管理员评价列表
     */
    listAdmin(params) {
        return request({
            url: '/admin/reviews',
            method: 'get',
            params
        })
    },

    /**
     * 隐藏评价
     */
    hide(id) {
        return request({
            url: `/admin/reviews/${id}/hide`,
            method: 'post'
        })
    },

    /**
     * 删除评价（软删）
     */
    remove(id) {
        return request({
            url: `/admin/reviews/${id}/delete`,
            method: 'post'
        })
    }
}
