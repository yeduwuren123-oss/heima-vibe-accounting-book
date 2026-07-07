<template>
  <div class="stats">
    <!-- 月份选择 -->
    <el-card style="margin-bottom: 16px">
      <el-form inline>
        <el-form-item label="选择月份">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
            @change="loadStats"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 收支总览三卡片 -->
    <div style="display: flex; gap: 16px; margin-bottom: 16px">
      <el-card style="flex: 1">
        <div style="text-align: center; padding: 20px">
          <div style="font-size: 14px; color: #999; margin-bottom: 8px">总收入</div>
          <div style="font-size: 28px; color: #27ae60; font-weight: bold">
            +¥{{ monthlyStats.incomeTotal || '0.00' }}
          </div>
        </div>
      </el-card>
      <el-card style="flex: 1">
        <div style="text-align: center; padding: 20px">
          <div style="font-size: 14px; color: #999; margin-bottom: 8px">总支出</div>
          <div style="font-size: 28px; color: #e74c3c; font-weight: bold">
            -¥{{ monthlyStats.expenseTotal || '0.00' }}
          </div>
        </div>
      </el-card>
      <el-card style="flex: 1">
        <div style="text-align: center; padding: 20px">
          <div style="font-size: 14px; color: #999; margin-bottom: 8px">结余</div>
          <div
            :style="{
              fontSize: '28px',
              fontWeight: 'bold',
              color: balanceNum >= 0 ? '#27ae60' : '#e74c3c'
            }"
          >
            {{ balanceNum >= 0 ? '+' : '' }}¥{{ monthlyStats.balance || '0.00' }}
          </div>
        </div>
      </el-card>
    </div>

    <!-- 支出饼图 -->
    <el-card style="margin-bottom: 16px" v-if="expenseStats.length > 0">
      <template #header>支出分类占比</template>
      <div style="display: flex">
        <div ref="expenseChart" style="width: 400px; height: 400px; flex-shrink: 0"></div>
        <div style="flex: 1; padding-left: 20px">
          <el-table :data="expenseStats" size="small">
            <el-table-column prop="categoryName" label="分类" />
            <el-table-column label="金额" width="120">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column label="占比" width="100">
              <template #default="{ row }">{{ row.percentage }}%</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>

    <!-- 收入饼图 -->
    <el-card style="margin-bottom: 16px" v-if="incomeStats.length > 0">
      <template #header>收入分类占比</template>
      <div style="display: flex">
        <div ref="incomeChart" style="width: 400px; height: 400px; flex-shrink: 0"></div>
        <div style="flex: 1; padding-left: 20px">
          <el-table :data="incomeStats" size="small">
            <el-table-column prop="categoryName" label="分类" />
            <el-table-column label="金额" width="120">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column label="占比" width="100">
              <template #default="{ row }">{{ row.percentage }}%</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>

    <!-- 空状态 -->
    <el-card v-if="expenseStats.length === 0 && incomeStats.length === 0" style="margin-bottom: 16px">
      <div style="text-align: center; padding: 60px; color: #999">该月暂无收支记录</div>
    </el-card>

    <!-- 每日明细 -->
    <el-card>
      <template #header>每日收支明细</template>
      <div v-if="dailyList.length > 0">
        <el-table :data="dailyList" size="small" max-height="500">
          <el-table-column prop="date" label="日期" width="150" />
          <el-table-column label="支出" width="150">
            <template #default="{ row }">
              <span v-if="row.expense > 0" style="color: #e74c3c">-¥{{ row.expense }}</span>
              <span v-else style="color: #ccc">-</span>
            </template>
          </el-table-column>
          <el-table-column label="收入" width="150">
            <template #default="{ row }">
              <span v-if="row.income > 0" style="color: #27ae60">+¥{{ row.income }}</span>
              <span v-else style="color: #ccc">-</span>
            </template>
          </el-table-column>
          <el-table-column label="可视化">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 4px">
                <div
                  :style="{
                    height: '12px',
                    width: row.expenseBar + '%',
                    backgroundColor: '#e74c3c',
                    borderRadius: '6px',
                    minWidth: row.expenseBar > 0 ? '4px' : '0'
                  }"
                ></div>
                <div
                  :style="{
                    height: '12px',
                    width: row.incomeBar + '%',
                    backgroundColor: '#27ae60',
                    borderRadius: '6px',
                    minWidth: row.incomeBar > 0 ? '4px' : '0'
                  }"
                ></div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else style="text-align: center; padding: 60px; color: #999">
        该月暂无收支记录
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getMonthlyStats, getDailyStats } from '../api/record'

const selectedMonth = ref(new Date().toISOString().slice(0, 7))
const monthlyStats = ref({ incomeTotal: 0, expenseTotal: 0, balance: 0, expenseStats: [], incomeStats: [] })
const dailyStats = ref({})
const expenseChart = ref(null)
const incomeChart = ref(null)
let expenseChartInstance = null
let incomeChartInstance = null

const balanceNum = computed(() => parseFloat(monthlyStats.value.balance || 0))

const expenseStats = computed(() => monthlyStats.value.expenseStats || [])
const incomeStats = computed(() => monthlyStats.value.incomeStats || [])

onMounted(() => {
  loadStats()
})

async function loadStats() {
  const [year, month] = selectedMonth.value.split('-')
  const [statsRes, dailyRes] = await Promise.all([
    getMonthlyStats(parseInt(year), parseInt(month)),
    getDailyStats(parseInt(year), parseInt(month))
  ])
  monthlyStats.value = statsRes
  dailyStats.value = dailyRes
  await nextTick()
  renderCharts()
}

function renderCharts() {
  if (expenseChartInstance) expenseChartInstance.dispose()
  if (incomeChartInstance) incomeChartInstance.dispose()

  if (expenseChart.value && expenseStats.value.length > 0) {
    expenseChartInstance = echarts.init(expenseChart.value)
    expenseChartInstance.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
      series: [{
        type: 'pie', radius: ['45%', '75%'],
        data: expenseStats.value.map(s => ({ name: s.categoryName, value: parseFloat(s.amount) })),
        label: { formatter: '{b}\n{d}%' }
      }]
    })
  }

  if (incomeChart.value && incomeStats.value.length > 0) {
    incomeChartInstance = echarts.init(incomeChart.value)
    incomeChartInstance.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
      series: [{
        type: 'pie', radius: ['45%', '75%'],
        data: incomeStats.value.map(s => ({ name: s.categoryName, value: parseFloat(s.amount) })),
        label: { formatter: '{b}\n{d}%' }
      }]
    })
  }
}

const dailyList = computed(() => {
  const entries = Object.entries(dailyStats.value)
  if (entries.length === 0) return []

  let maxAmount = 0
  for (const [, v] of entries) {
    maxAmount = Math.max(maxAmount, parseFloat(v.expense || 0), parseFloat(v.income || 0))
  }
  if (maxAmount === 0) maxAmount = 1

  return entries.map(([date, v]) => ({
    date,
    expense: parseFloat(v.expense || 0).toFixed(2),
    income: parseFloat(v.income || 0).toFixed(2),
    expenseBar: (parseFloat(v.expense || 0) / maxAmount) * 100,
    incomeBar: (parseFloat(v.income || 0) / maxAmount) * 100
  })).sort((a, b) => b.date.localeCompare(a.date))
})
</script>
