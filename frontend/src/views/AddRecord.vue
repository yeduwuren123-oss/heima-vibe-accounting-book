<template>
  <div class="add-record">
    <el-card>
      <template #header>
        <span>记一笔</span>
      </template>

      <!-- 收支类型切换 -->
      <div style="margin-bottom: 20px; text-align: center">
        <el-radio-group v-model="form.type" @change="onTypeChange" size="large">
          <el-radio-button value="EXPENSE">
            支出 <span style="color: #e74c3c">−</span>
          </el-radio-button>
          <el-radio-button value="INCOME">
            收入 <span style="color: #27ae60">+</span>
          </el-radio-button>
        </el-radio-group>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" style="max-width: 500px; margin: 0 auto">
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :precision="2"
            :min="0.01"
            :max="999999.99"
            placeholder="请输入金额"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.parentCategoryId" placeholder="一级分类" @change="onParentChange" style="width: 48%; margin-right: 4%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
          <el-select v-model="form.categoryId" placeholder="二级分类" style="width: 48%">
            <el-option
              v-for="cat in currentChildren"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="日期" prop="recordDate">
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="选填，记录一些备注信息"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submit" :loading="submitting" size="large" style="width: 100%">
            {{ form.type === 'INCOME' ? '记一笔收入' : '记一笔支出' }}
          </el-button>
          <el-button @click="reset" style="margin-top: 8px">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCategories } from '../api/category'
import { createRecord } from '../api/record'

const categories = ref([])
const currentChildren = ref([])

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  type: 'EXPENSE',
  amount: null,
  parentCategoryId: null,
  categoryId: null,
  recordDate: new Date().toISOString().slice(0, 10),
  remark: ''
})

const rules = {
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择二级分类', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

onMounted(() => {
  loadCategories()
})

async function loadCategories() {
  categories.value = await getCategories({ type: form.type })
  currentChildren.value = []
  form.parentCategoryId = null
  form.categoryId = null
}

function onTypeChange() {
  loadCategories()
}

function onParentChange(parentId) {
  form.categoryId = null
  const parent = categories.value.find(c => c.id === parentId)
  currentChildren.value = parent ? (parent.children || []) : []
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createRecord({
      type: form.type,
      amount: form.amount,
      categoryId: form.categoryId,
      recordDate: form.recordDate,
      remark: form.remark || ''
    })
    ElMessage.success('记账成功！')
    reset()
  } catch (e) {
    ElMessage.error(e.message || '记账失败')
  } finally {
    submitting.value = false
  }
}

function reset() {
  form.amount = null
  form.parentCategoryId = null
  form.categoryId = null
  form.recordDate = new Date().toISOString().slice(0, 10)
  form.remark = ''
  currentChildren.value = []
  formRef.value?.clearValidate()
}
</script>
