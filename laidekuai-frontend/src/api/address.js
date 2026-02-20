import request from '@/utils/request'

/**
 * 地址相关API
 */
export default {
    /**
     * 获取地址列表
     */
    list() {
        return request({
            url: '/addresses',
            method: 'get'
        })
    },

    /**
     * 添加地址
     */
    add(data) {
        return request({
            url: '/addresses',
            method: 'post',
            data
        })
    },

    /**
     * 更新地址
     */
    update(id, data) {
        return request({
            url: `/addresses/${id}`,
            method: 'put',
            data
        })
    },

    /**
     * 删除地址
     */
    delete(id) {
        return request({
            url: `/addresses/${id}`,
            method: 'delete'
        })
    },

    /**
     * 设置默认地址
     */
    setDefault(id) {
        return request({
            url: `/addresses/${id}/default`,
            method: 'put'
        })
    }
}
