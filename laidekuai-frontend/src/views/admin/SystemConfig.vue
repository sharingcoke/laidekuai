<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const form = reactive({
  adminResetPassword: '123456',
  maxUploadSize: '20MB',
  maxActiveOrders: 10
})

const handleSave = () => {
  ElMessage.info('系统配置保存功能暂未开放')
}
</script>

<template>
  <div class="admin-system page-shell">
    <div class="page-header">
      <div>
        <h2>系统配置</h2>
        <p class="page-desc">维护平台关键配置项（V1 仅展示）。</p>
      </div>
    </div>

    <el-card class="panel-card" shadow="never">
      <el-form label-width="140px" class="form-standard">
        <el-form-item label="管理员重置默认密码">
          <el-input v-model="form.adminResetPassword" disabled />
        </el-form-item>
        <el-form-item label="上传文件大小上限">
          <el-input v-model="form.maxUploadSize" disabled />
        </el-form-item>
        <el-form-item label="买家最大活跃订单数">
          <el-input v-model="form.maxActiveOrders" disabled />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.admin-system {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
