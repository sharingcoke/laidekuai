import request from '@/utils/request'

/**
 * 购物车相关API
 */
export default {
    /**
     * 添加到购物车
     */
    add(goodsId, quantity = 1) {
        return request({
            url: '/cart',
            method: 'post',
            data: { goodsId, quantity }
        })
    },

    /**
     * 获取购物车列表
     */
    list() {
        return request({
            url: '/cart',
            method: 'get'
        })
    },

    /**
     * 更新购物车商品数量
     */
    update(id, quantity) {
        return request({
            url: `/cart/${id}`,
            method: 'put',
            data: { quantity }
        })
    },

    /**
     * 删除购物车商品
     */
    delete(id) {
        return request({
            url: `/cart/${id}`,
            method: 'delete'
        })
    },

    /**
     * 清空购物车
     */
    clear() {
        return request({
            url: '/cart',
            method: 'delete'
        })
    }
}
