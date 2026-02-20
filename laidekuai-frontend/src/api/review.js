import request from '@/utils/request'

/**
 * 评价相关API（管理员）
 */
export default {
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
