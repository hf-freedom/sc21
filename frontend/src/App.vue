<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <h1>风控交易系统</h1>
    </el-header>
    <el-container>
      <el-aside width="200px" class="app-aside">
        <el-menu
          :default-active="activeMenu"
          class="menu"
          router
        >
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <span>用户列表</span>
          </el-menu-item>
          <el-menu-item index="/rules">
            <el-icon><Setting /></el-icon>
            <span>风控规则</span>
          </el-menu-item>
          <el-menu-item index="/transactions">
            <el-icon><Document /></el-icon>
            <span>交易记录</span>
          </el-menu-item>
          <el-menu-item index="/initiate">
            <el-icon><Plus /></el-icon>
            <span>发起交易</span>
          </el-menu-item>
          <el-menu-item index="/pending">
            <el-icon><Clock /></el-icon>
            <span>待审核列表</span>
          </el-menu-item>
          <el-menu-item index="/blacklist">
            <el-icon><Warning /></el-icon>
            <span>黑名单管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { User, Setting, Document, Plus, Clock, Warning } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.app-container {
  height: 100vh;
}

.app-header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.app-header h1 {
  font-size: 20px;
  font-weight: 500;
}

.app-aside {
  background-color: #304156;
}

.app-main {
  background-color: #f5f7fa;
  padding: 20px;
}

.menu {
  border-right: none;
}

.el-menu-item {
  color: #bfcbd9;
}

.el-menu-item:hover {
  background-color: #263445;
}

.el-menu-item.is-active {
  background-color: #409eff;
  color: white;
}
</style>
