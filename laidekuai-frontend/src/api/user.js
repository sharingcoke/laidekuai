import request from '@/utils/request'

/**
 * 用户管理API
 */
export default {
    /**
     * 更新用户信息
     */
    update(id, data) {
        return request({
            url: `/users/${id}`,
            method: 'put',
            data
        })
    },

    /**
     * 修改密码
     */
    changePassword(id, data) {
        return request({
            url: `/users/${id}/password`,
            method: 'put',
            data
        })
    },

    /**
     * 管理员：获取用户列表
     */
    list(params) {
        return request({
            url: '/admin/users',
            method: 'get',
            params
        })
    },

    /**
     * 管理员：更新用户状态
     */
    updateStatus(id, status) {
        return request({
            url: `/admin/users/${id}/status`,
            method: 'put',
            params: { status }
        })
    }
}
