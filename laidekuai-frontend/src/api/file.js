import request from '@/utils/request'

/**
 * 文件相关API
 */
export default {
    /**
     * 上传文件
     */
    upload(file) {
        const formData = new FormData()
        formData.append('file', file)
        return request({
            url: '/files/upload',
            method: 'post',
            data: formData,
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
    }
}
