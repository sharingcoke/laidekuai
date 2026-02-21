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
    }
}
