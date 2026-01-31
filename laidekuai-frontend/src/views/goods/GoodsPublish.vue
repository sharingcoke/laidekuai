<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import goodsApi from '@/api/goods'
import fileApi from '@/api/file' // 需要创建file.js

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const goodsId = route.params.id

const form = reactive({
    title: '',
    subTitle: '',
    categoryId: null, // V1暂无分类列表API，可先写死或填数字
    price: 0,
    stock: 1,
    detail: '',
    imageUrls: []
})

// TODO: 分类列表应当从API获取
const categories = [
    { id: 101, name: '数码产品' },
    { id: 102, name: '书籍资料' },
    { id: 103, name: '生活用品' },
    { id: 104, name: '服装鞋包' },
    { id: 105, name: '其他' }
]

const uploadHeaders = ref({
    Authorization: `Bearer ${localStorage.getItem('token')}`
})

const fetchDetail = async () => {
    loading.value = true
    try {
        const res = await goodsApi.detail(goodsId)
        if (res.code === 0) {
            const data = res.data
            form.title = data.title
            form.subTitle = data.subTitle
            form.categoryId = data.categoryId
            form.price = data.price
            form.stock = data.stock
            form.detail = data.detail
            try {
                form.imageUrls = JSON.parse(data.imageUrls).map(url => ({ name: 'img', url }))
            } catch {
                form.imageUrls = []
            }
        }
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    if (goodsId) {
        isEdit.value = true
        fetchDetail()
    }
})

// 上传成功
const handleUploadSuccess = (response, uploadFile, uploadFiles) => {
    if (response.code === 0) {
        // 替换为相对路径或完整URL
        uploadFile.url = response.data
    } else {
        ElMessage.error(response.message || '上传失败')
    }
}

// 移除图片
const handleRemove = (uploadFile, uploadFiles) => {
    // urls自动更新
    console.log(uploadFile, uploadFiles)
}

const handleSubmit = async () => {
    if (!form.title || !form.price || !form.stock) {
        ElMessage.warning('请填写必填项')
        return
    }
    
    // 构造图片URL列表 (String array)
    // uploadFiles in element plus might differ
    // 简单起见，这里假设form.imageUrls绑定的是fileList
    // 但是element upload v-model:file-list是对象数组
    
    // 我们需要遍历 fileList 提取 url
    // [TEST] 
    // if (realImageUrls.length === 0) { ... }
    const realImageUrls = form.imageUrls.map(item => item.url || item.response?.data).filter(url => !!url)
    
    if (realImageUrls.length === 0) {
        ElMessage.warning('请至少上传一张图片')
        return
    }

    submitting.value = true
    try {
        const payload = {
            ...form,
            imageUrls: realImageUrls
        }
        
        let res
        if (isEdit.value) {
            res = await goodsApi.update(goodsId, payload)
        } else {
            res = await goodsApi.create(payload)
        }
        
        if (res.code === 0) {
            ElMessage.success(isEdit.value ? '更新成功' : '发布成功')
            router.push('/my-goods')
        }
    } catch (error) {
        console.error(error)
    } finally {
        submitting.value = false
    }
}
</script>

<template>
  <div class="publish-page">
    <div class="page-header">
         <h2>{{ isEdit ? '编辑商品' : '发布新商品' }}</h2>
    </div>

    <div class="form-container" v-loading="loading">
        <el-form :model="form" label-width="100px">
            <el-form-item label="商品标题" required>
                <el-input v-model="form.title" placeholder="请输入商品标题" maxlength="100" show-word-limit />
            </el-form-item>
            
            <el-form-item label="副标题">
                <el-input v-model="form.subTitle" placeholder="简短描述" maxlength="200" show-word-limit />
            </el-form-item>

            <el-form-item label="商品分类" required>
                <el-select v-model="form.categoryId" placeholder="请选择分类">
                    <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
                </el-select>
            </el-form-item>

            <el-form-item label="价格" required>
                <el-input-number v-model="form.price" :precision="2" :step="1" :min="0" />
            </el-form-item>

            <el-form-item label="库存" required>
                <el-input-number v-model="form.stock" :step="1" :min="1" :precision="0" />
            </el-form-item>

            <el-form-item label="商品图片" required>
                <el-upload
                    v-model:file-list="form.imageUrls"
                    action="/api/files/upload"
                    :headers="uploadHeaders"
                    list-type="picture-card"
                    :on-success="handleUploadSuccess"
                    :on-remove="handleRemove"
                    accept="image/*"
                    :limit="5"
                >
                    <el-icon>+</el-icon>
                </el-upload>
                <div class="tips">第一张图片将作为主图，最多上传5张。</div>
            </el-form-item>

            <el-form-item label="商品详情">
                <el-input v-model="form.detail" type="textarea" rows="6" placeholder="详细描述您的商品..." />
            </el-form-item>

            <el-form-item>
                <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
                    {{ isEdit ? '保存修改' : '立即发布' }}
                </el-button>
                <el-button size="large" @click="router.back()">取消</el-button>
            </el-form-item>
        </el-form>
    </div>
  </div>
</template>

<style scoped>
.publish-page {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}
.page-header {
    margin-bottom: 30px;
    border-bottom: 1px solid #eee;
    padding-bottom: 15px;
}
.form-container {
    background: white;
    padding: 30px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.tips {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
}
</style>
