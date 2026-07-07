<template>
  <el-container class="layout">
    <el-aside width="200px">
      <div class="logo">黑马记账</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/add">
          <el-icon><Edit /></el-icon>
          <span>记账</span>
        </el-menu-item>
        <el-menu-item index="/list">
          <el-icon><List /></el-icon>
          <span>记录</span>
        </el-menu-item>
        <el-menu-item index="/stats">
          <el-icon><DataAnalysis /></el-icon>
          <span>统计</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <h2>{{ pageTitle }}</h2>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => '/' + route.path.split('/')[1])
const pageTitle = computed(() => route.meta?.title || '')
</script>

<style scoped>
.layout {
  height: 100vh;
}
.el-aside {
  background-color: #304156;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.el-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.el-header h2 {
  font-size: 18px;
  color: #333;
}
.el-main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
