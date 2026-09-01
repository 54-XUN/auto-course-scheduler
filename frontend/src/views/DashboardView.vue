<template>
  <div>
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.title" :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat">
            <el-icon :size="40" :color="card.color">
              <component :is="card.icon" />
            </el-icon>
            <div>
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="action-card" shadow="never">
      <template #header>
        <span>快速开始</span>
      </template>
      <el-steps :active="activeStep" align-center>
        <el-step title="维护基础数据" description="教师 / 班级 / 课程 / 教室" />
        <el-step title="触发自动排课" description="排课管理 → 开始排课" />
        <el-step title="查看与调整课表" description="课表查询 → 拖拽调课" />
      </el-steps>
      <div class="action-btns">
        <el-button type="primary" @click="$router.push('/teachers')">维护基础数据</el-button>
        <el-button type="success" @click="$router.push('/schedules')">开始自动排课</el-button>
        <el-button type="warning" @click="$router.push('/timetable')">查看课表</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { listTeachers, listClasses, listCourses, listClassrooms } from '../api/baseData'
import { listSchedules } from '../api/schedule'

const stats = ref({ teachers: '-', classes: '-', courses: '-', schedules: '-' })

const cards = computed(() => [
  { title: '教师', value: stats.value.teachers, icon: 'User', color: '#409EFF' },
  { title: '班级', value: stats.value.classes, icon: 'OfficeBuilding', color: '#67C23A' },
  { title: '课程', value: stats.value.courses, icon: 'Reading', color: '#E6A23C' },
  { title: '已排课程节数', value: stats.value.schedules, icon: 'Calendar', color: '#F56C6C' }
])

const activeStep = computed(() => {
  if (Number(stats.value.schedules) > 0) return 3
  if (Number(stats.value.teachers) > 0 && Number(stats.value.courses) > 0) return 1
  return 0
})

onMounted(async () => {
  const [teachers, classes, courses, schedules] = await Promise.all([
    listTeachers(),
    listClasses(),
    listCourses(),
    listSchedules()
  ])
  stats.value = {
    teachers: teachers.length,
    classes: classes.length,
    courses: courses.length,
    schedules: schedules.length
  }
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 16px;
}
.stat {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}
.stat-title {
  font-size: 13px;
  color: #909399;
}
.action-card {
  margin-top: 8px;
}
.action-btns {
  margin-top: 24px;
  text-align: center;
}
</style>
