<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button
        type="success"
        :icon="VideoPlay"
        :loading="autoRunning"
        @click="handleAutoSchedule"
      >
        开始自动排课
      </el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <span class="total-tip" v-if="list.length">共 {{ list.length }} 节课</span>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="className" label="班级" min-width="120" />
      <el-table-column prop="courseName" label="课程" min-width="110" />
      <el-table-column prop="teacherName" label="教师" width="100" />
      <el-table-column prop="classroomName" label="教室" min-width="130" />
      <el-table-column label="星期" width="90">
        <template #default="{ row }">{{ dayName(row.weekDay) }}</template>
      </el-table-column>
      <el-table-column label="节次" width="90">
        <template #default="{ row }">第 {{ row.section }} 节</template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, VideoPlay } from '@element-plus/icons-vue'
import { autoSchedule, listSchedules } from '../api/schedule'

const list = ref([])
const loading = ref(false)
const autoRunning = ref(false)

const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
function dayName(d) {
  return dayNames[d] || d
}

async function load() {
  loading.value = true
  try {
    list.value = await listSchedules()
  } finally {
    loading.value = false
  }
}

async function handleAutoSchedule() {
  await ElMessageBox.confirm(
    '自动排课将清空现有排课结果并重新计算，是否继续？',
    '开始自动排课',
    { type: 'warning', confirmButtonText: '开始排课', cancelButtonText: '取消' }
  )
  autoRunning.value = true
  try {
    const res = await autoSchedule()
    ElMessage.success(res.message || '排课成功')
    await load()
  } finally {
    autoRunning.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.total-tip {
  color: #909399;
  font-size: 13px;
  margin-left: auto;
}
</style>
