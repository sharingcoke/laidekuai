<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import categoryApi from '@/api/category'

const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: null
  },
  placeholder: {
    type: String,
    default: '请选择分类'
  },
  clearable: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const loading = ref(false)
const options = ref([])

const cascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  emitPath: false,
  checkStrictly: false
}

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await categoryApi.listTree()
    if (res.code === 0) {
      options.value = res.data || []
    }
  } catch (error) {
    console.error('获取分类失败:', error)
    ElMessage.error('获取分类失败')
  } finally {
    loading.value = false
  }
}

const handleChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value)
}

onMounted(() => {
  fetchCategories()
})
</script>

<template>
  <el-cascader
    :model-value="props.modelValue"
    :options="options"
    :props="cascaderProps"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :loading="loading"
    filterable
    @update:model-value="handleChange"
  />
</template>
