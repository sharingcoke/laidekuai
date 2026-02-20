import request from '@/utils/request'

/**
 * 订单相关API
 */
export default {
    /**
     * 创建订单
     * @param {Object} data { addressId, items: [{ goodsId, quantity, cartId }], remark }
     */
    create(data) {
        return request({
            url: '/orders',
            method: 'post',
            data
        })
    },

    /**
     * 支付订单
     */
    pay(id) {
        return request({
            url: `/orders/${id}/pay`,
            method: 'post'
        })
    },

    /**
     * 取消订单
     */
    cancel(id, reason) {
        return request({
            url: `/orders/${id}/cancel`,
            method: 'post',
            data: { reason }
        })
    },

    /**
     * 申请退款
     */
    refund(id, reason) {
        return request({
            url: `/orders/${id}/refund`,
            method: 'post',
            data: { reason }
        })
    },

    /**
     * 确认收货
     */
    receive(id) {
        return request({
            url: `/orders/${id}/receive`,
            method: 'post'
        })
    },

    /**
     * 订单列表
     * @param {Object} params { page, size, status, type } type=buyer|seller
     */
    list(params) {
        return request({
            url: '/orders',
            method: 'get',
            params
        })
    },

    /**
     * 卖家订单列表
     */
    listSeller(params) {
        return request({
            url: '/orders/seller',
            method: 'get',
            params
        })
    },

    /**
     * 订单详情
     */
    detail(id) {
        return request({
            url: `/orders/${id}`,
            method: 'get'
        })
    },

    /**
     * 卖家发货（订单项级）
     */
    shipItem(id, data) {
        return request({
            url: `/seller/order-items/${id}/ship`,
            method: 'post',
            data
        })
    },

    /**
     * 管理员代发货（订单项级）
     */
    shipItemByAdmin(id, data) {
        return request({
            url: `/admin/order-items/${id}/ship`,
            method: 'post',
            data
        })
    },

    /**
     * 兼容旧调用：发货
     */
    ship(id, data) {
        return this.shipItem(id, data)
    },

    /**
     * 卖家处理退款
     */
    handleRefund(id, approved, remark) {
        return request({
            url: `/orders/${id}/refund/handle`,
            method: 'post',
            data: { approved, remark }
        })
    }
}
