import request from '@/utils/request'

/**
 * 分类管理API
 */
export default {
    /**
     * 获取分类树
     */
    list() {
        return request({
            url: '/admin/categories',
            method: 'get'
        })
    },

    /**
     * 新增分类
     */
    create(data) {
        return request({
            url: '/admin/categories',
            method: 'post',
            data
        })
    },

    /**
     * 更新分类
     */
    update(id, data) {
        return request({
            url: `/admin/categories/${id}`,
            method: 'put',
            data
        })
    },

    /**
     * 删除分类
     */
    remove(id) {
        return request({
            url: `/admin/categories/${id}`,
            method: 'delete'
        })
    }
}
