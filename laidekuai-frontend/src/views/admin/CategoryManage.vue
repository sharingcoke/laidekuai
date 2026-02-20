<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import categoryApi from '@/api/category'

const loading = ref(false)
const categoryTree = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')

const form = reactive({
  id: null,
  name: '',
  parentId: null,
  sortOrder: 0,
  iconUrl: ''
})

const resetForm = () => {
  form.id = null
  form.name = ''
  form.parentId = null
  form.sortOrder = 0
  form.iconUrl = ''
}

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await categoryApi.list()
    if (res.code === 0) {
      categoryTree.value = res.data || []
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  } finally {
    loading.value = false
  }
}

const flattenCategories = (nodes, level = 1, result = []) => {
  nodes.forEach(node => {
    result.push({
      id: node.id,
      name: `${'—'.repeat(Math.max(level - 1, 0))} ${node.name}`,
      level: node.level
    })
    if (node.children && node.children.length > 0) {
      flattenCategories(node.children, level + 1, result)
    }
  })
  return result
}

const parentOptions = computed(() => flattenCategories(categoryTree.value, 1, []))

const openCreate = () => {
  resetForm()
  dialogTitle.value = '新增分类'
  dialogVisible.value = true
}

const openEdit = (row) => {
  form.id = row.id
  form.name = row.name
  form.parentId = row.parentId || null
  form.sortOrder = row.sortOrder || 0
  form.iconUrl = row.iconUrl || ''
  dialogTitle.value = '编辑分类'
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  const payload = {
    name: form.name,
    parentId: form.parentId || null,
    sortOrder: form.sortOrder,
    iconUrl: form.iconUrl
  }
  try {
    const res = form.id
      ? await categoryApi.update(form.id, payload)
      : await categoryApi.create(payload)
    if (res.code === 0) {
      ElMessage.success(form.id ? '分类已更新' : '分类已创建')
      dialogVisible.value = false
      fetchCategories()
    }
  } catch (error) {
    console.error('提交分类失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除分类 ${row.name} 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await categoryApi.remove(row.id)
    if (res.code === 0) {
      ElMessage.success('分类已删除')
      fetchCategories()
    }
  }).catch(() => {})
}

const statusLabel = (status) => {
  if (status === 'ENABLED') return '启用'
  if (status === 'DISABLED') return '禁用'
  return status || '-'
}

onMounted(() => {
  fetchCategories()
})
</script>

<template>
  <div class="category-manage-page">
    <div class="page-header">
      <div>
        <h2>分类管理</h2>
        <p class="page-desc">维护分类层级与排序，删除前自动校验商品引用。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>

    <el-card class="table-card" shadow="never">
      <div class="table-container" v-loading="loading">
        <el-table
          :data="categoryTree"
          row-key="id"
          border
          stripe
          default-expand-all
          :tree-props="{ children: 'children' }"
        >
          <el-table-column prop="name" label="分类名称" min-width="200" />
          <el-table-column prop="level" label="层级" width="80" />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form label-width="90px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="form.parentId" placeholder="选择父分类（可选）" clearable>
            <el-option :value="null" label="无（一级分类）" />
            <el-option
              v-for="item in parentOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="图标URL">
          <el-input v-model="form.iconUrl" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.category-manage-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
}

.page-desc {
  margin: 8px 0 0;
  font-size: 13px;
  color: #909399;
}

.table-card {
  border-radius: 8px;
}

.table-container {
  padding: 6px 0 0;
}
</style>
