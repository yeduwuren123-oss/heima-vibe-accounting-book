<template>
  <div class="category-manage">
    <!-- 类型切换 -->
    <el-card style="margin-bottom: 16px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <el-radio-group v-model="currentType" @change="loadCategories" size="default">
          <el-radio-button value="EXPENSE">支出分类</el-radio-button>
          <el-radio-button value="INCOME">收入分类</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="openAdd">新增分类</el-button>
      </div>
    </el-card>

    <!-- 分类树 -->
    <el-card v-loading="loading">
      <div v-if="categories.length === 0" style="text-align: center; padding: 60px; color: #999">暂无分类</div>
      <el-collapse v-else>
        <el-collapse-item v-for="parent in categories" :key="parent.id">
          <template #title>
            <div style="display: flex; align-items: center; gap: 8px">
              <span style="font-weight: bold; font-size: 15px">{{ parent.name }}</span>
              <el-tag v-if="parent.preset" type="info" size="small">预置</el-tag>
              <span style="font-size: 12px; color: #999">{{ parent.children?.length || 0 }} 个子分类</span>
            </div>
          </template>
          <div
            v-for="child in parent.children"
            :key="child.id"
            style="display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; border-bottom: 1px solid #f0f0f0"
          >
            <div style="display: flex; align-items: center; gap: 6px">
              <span>{{ child.name }}</span>
              <el-icon v-if="child.preset" style="color: #ccc" :size="14"><Lock /></el-icon>
            </div>
            <div v-if="!child.preset" style="display: flex; gap: 4px">
              <el-button type="primary" link size="small" @click.stop="openEdit(child)">编辑</el-button>
              <el-button type="danger" link size="small" @click.stop="handleDelete(child)">删除</el-button>
            </div>
            <span v-else style="font-size: 12px; color: #ccc">系统预置</span>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑分类' : '新增分类'"
      width="450px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item v-if="!editingId" label="所属大类" prop="parentId">
          <el-select v-model="form.parentId" placeholder="请选择一级分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="10" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '../api/category'

const currentType = ref('EXPENSE')
const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  parentId: null,
  name: ''
})

const rules = {
  parentId: [{ required: true, message: '请选择所属大类', trigger: 'change' }],
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

onMounted(() => {
  loadCategories()
})

async function loadCategories() {
  loading.value = true
  try {
    categories.value = await getCategories({ type: currentType.value })
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editingId.value = null
  form.parentId = null
  form.name = ''
  dialogVisible.value = true
}

function openEdit(cat) {
  editingId.value = cat.id
  form.parentId = null
  form.name = cat.name
  dialogVisible.value = true
}

async function save() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingId.value) {
      await updateCategory(editingId.value, { name: form.name })
      ElMessage.success('修改成功')
    } else {
      await createCategory({ name: form.name, parentId: form.parentId })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await loadCategories()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(cat) {
  try {
    await ElMessageBox.confirm(`确定要删除"${cat.name}"吗？`, '确认删除', { type: 'warning' })
    await deleteCategory(cat.id)
    ElMessage.success('删除成功')
    await loadCategories()
  } catch {
    // 取消
  }
}
</script>
