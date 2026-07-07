<template>
  <div class="record-list">
    <!-- 筛选栏 -->
    <el-card style="margin-bottom: 16px">
      <el-form :model="filters" inline>
        <el-form-item label="类型">
          <el-select v-model="filters.type" placeholder="全部" clearable @change="search" style="width: 100px">
            <el-option label="全部" value="" />
            <el-option label="支出" value="EXPENSE" />
            <el-option label="收入" value="INCOME" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.parentCategoryId" placeholder="全部大类" clearable @change="onFilterCategoryChange" style="width: 130px">
            <el-option v-for="cat in filterCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
          <el-select v-model="filters.categoryId" placeholder="全部小类" clearable style="width: 130px; margin-left: 8px">
            <el-option v-for="cat in filterChildren" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="filters.minAmount" :precision="2" placeholder="最低" style="width: 110px" />
          <span style="margin: 0 4px">-</span>
          <el-input-number v-model="filters.maxAmount" :precision="2" placeholder="最高" style="width: 110px" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="搜索备注" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 记录表格 -->
    <el-card>
      <template #header>
        <span>记账记录（共 {{ total }} 条）</span>
      </template>
      <el-table :data="records" stripe v-loading="loading">
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'" size="small">
              {{ row.type === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column label="金额" width="130">
          <template #default="{ row }">
            <span :style="{ color: row.type === 'INCOME' ? '#27ae60' : '#e74c3c', fontWeight: 'bold' }">
              {{ row.type === 'INCOME' ? '+' : '-' }}¥{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="180">
          <template #default="{ row }">
            {{ row.parentCategoryName }} / {{ row.categoryName }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200">
          <template #default="{ row }">
            <span v-if="row.remark">{{ row.remark }}</span>
            <span v-else style="color: #ccc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; text-align: right">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="search"
          @current-change="search"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑记录" width="500px">
      <!-- 类型切换 -->
      <div style="margin-bottom: 16px; text-align: center">
        <el-radio-group v-model="editForm.type" @change="onEditTypeChange" size="default">
          <el-radio-button value="EXPENSE">支出</el-radio-button>
          <el-radio-button value="INCOME">收入</el-radio-button>
        </el-radio-group>
      </div>
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="80px">
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="editForm.amount" :precision="2" :min="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="editForm.parentCategoryId" placeholder="一级分类" @change="onEditParentChange" style="width: 48%; margin-right: 4%">
            <el-option v-for="cat in editCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
          <el-select v-model="editForm.categoryId" placeholder="二级分类" style="width: 48%">
            <el-option v-for="cat in editChildren" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="recordDate">
          <el-date-picker v-model="editForm.recordDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories } from '../api/category'
import { getRecords, updateRecord, deleteRecord } from '../api/record'

const filterCategories = ref([])
const filterChildren = ref([])
const editCategories = ref([])
const editChildren = ref([])
const records = ref([])
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)
const dialogVisible = ref(false)
const editingId = ref(null)
const editFormRef = ref(null)

const filters = reactive({
  type: '',
  dateRange: null,
  parentCategoryId: null,
  categoryId: null,
  minAmount: null,
  maxAmount: null,
  keyword: ''
})

const editForm = reactive({
  type: 'EXPENSE',
  amount: null,
  parentCategoryId: null,
  categoryId: null,
  recordDate: '',
  remark: ''
})

const rules = {
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

onMounted(() => {
  search()
})

async function loadFilterCategories() {
  filterCategories.value = await getCategories({ type: filters.type || undefined })
  filterChildren.value = []
  filters.parentCategoryId = null
  filters.categoryId = null
}

function onFilterCategoryChange(parentId) {
  filters.categoryId = null
  const parent = filterCategories.value.find(c => c.id === parentId)
  filterChildren.value = parent ? (parent.children || []) : []
}

async function onEditTypeChange() {
  editForm.parentCategoryId = null
  editForm.categoryId = null
  editCategories.value = await getCategories({ type: editForm.type })
  editChildren.value = []
}

function onEditParentChange(parentId) {
  editForm.categoryId = null
  const parent = editCategories.value.find(c => c.id === parentId)
  editChildren.value = parent ? (parent.children || []) : []
}

async function search() {
  loading.value = true
  try {
    const params = {
      page: page.value - 1,
      size: size.value
    }
    if (filters.type) params.type = filters.type
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }
    if (filters.categoryId) {
      params.categoryId = filters.categoryId
    } else if (filters.parentCategoryId) {
      params.parentCategoryId = filters.parentCategoryId
    }
    if (filters.minAmount) params.minAmount = filters.minAmount
    if (filters.maxAmount) params.maxAmount = filters.maxAmount
    if (filters.keyword) params.keyword = filters.keyword

    const res = await getRecords(params)
    records.value = res.content
    total.value = res.totalElements
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function resetFilters() {
  filters.type = ''
  filters.dateRange = null
  filters.parentCategoryId = null
  filters.categoryId = null
  filters.minAmount = null
  filters.maxAmount = null
  filters.keyword = ''
  filterChildren.value = []
  page.value = 1
  search()
}

async function openEdit(row) {
  editingId.value = row.id
  editForm.type = row.type
  editForm.amount = row.amount
  editForm.recordDate = row.recordDate
  editForm.remark = row.remark || ''
  editCategories.value = await getCategories({ type: row.type })
  editForm.parentCategoryId = row.parentCategoryId
  editForm.categoryId = row.categoryId
  onEditParentChange(row.parentCategoryId)
  // 确保选中的二级分类在列表中
  setTimeout(() => {
    editForm.categoryId = row.categoryId
  }, 50)
  dialogVisible.value = true
}

async function saveEdit() {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updateRecord(editingId.value, {
      type: editForm.type,
      amount: editForm.amount,
      categoryId: editForm.categoryId,
      recordDate: editForm.recordDate,
      remark: editForm.remark || ''
    })
    ElMessage.success('修改成功')
    dialogVisible.value = false
    search()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '确认删除', { type: 'warning' })
    await deleteRecord(id)
    ElMessage.success('删除成功')
    search()
  } catch {
    // 用户取消
  }
}
</script>
