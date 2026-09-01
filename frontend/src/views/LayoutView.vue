<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">🎓 自动排课系统</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#a6adb4"
        active-text-color="#ffffff"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ $route.meta.title }}</div>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><UserFilled /></el-icon>
            {{ authStore.username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const menus = [
  { path: '/dashboard', title: '首页', icon: 'HomeFilled' },
  { path: '/teachers', title: '教师管理', icon: 'User' },
  { path: '/classes', title: '班级管理', icon: 'OfficeBuilding' },
  { path: '/courses', title: '课程管理', icon: 'Reading' },
  { path: '/classrooms', title: '教室管理', icon: 'School' },
  { path: '/time-slots', title: '时间段管理', icon: 'Clock' },
  { path: '/schedules', title: '排课管理', icon: 'Calendar' },
  { path: '/timetable', title: '课表查询', icon: 'Grid' }
]

function handleCommand(cmd) {
  if (cmd === 'logout') {
    authStore.logout()
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background-color: #001529;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 17px;
  font-weight: 600;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.aside :deep(.el-menu-item.is-active) {
  background-color: #1890ff;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #303133;
}
.main {
  padding: 16px;
  overflow: auto;
}
</style>
