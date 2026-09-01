<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-radio-group v-model="viewType" @change="onViewTypeChange">
        <el-radio-button value="class">班级课表</el-radio-button>
        <el-radio-button value="teacher">教师课表</el-radio-button>
        <el-radio-button value="classroom">教室课表</el-radio-button>
      </el-radio-group>

      <el-select
        v-model="viewId"
        placeholder="请选择"
        style="width: 220px"
        @change="loadTimetable"
      >
        <el-option v-for="o in viewOptions" :key="o.id" :label="o.name" :value="o.id" />
      </el-select>

      <el-button type="primary" :icon="Download" :disabled="!timetable" @click="exportExcel">
        导出 Excel
      </el-button>

      <el-alert
        class="tip"
        type="info"
        :closable="false"
        title="拖拽课程卡片到其他格子可调课；拖到已有课程的格子可交换；冲突将被拦截"
      />
    </div>

    <div v-loading="loading">
      <el-empty v-if="!timetable" description="请选择班级 / 教师 / 教室查看课表" />
      <template v-else>
        <h3 class="view-name">
          {{ viewTypeLabel }}：{{ timetable.viewName }}
          <span v-if="!timetable.cells.length" class="muted">（暂无排课）</span>
        </h3>
        <TimetableGrid :cells="timetable.cells" @move="handleMove" />
      </template>
    </div>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import TimetableGrid from '../components/TimetableGrid.vue'
import {
  classTimetable,
  teacherTimetable,
  classroomTimetable,
  listSchedules,
  moveSchedule
} from '../api/schedule'
import { listTeachers, listClasses, listClassrooms } from '../api/baseData'

const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

const viewType = ref('class')
const viewId = ref(null)
const timetable = ref(null)
const loading = ref(false)

const teachers = ref([])
const classes = ref([])
const classrooms = ref([])
// 全量排课结果：用于前端冲突预检
const allSchedules = ref([])

const viewOptions = computed(() => {
  if (viewType.value === 'class') return classes.value
  if (viewType.value === 'teacher') return teachers.value
  return classrooms.value
})

const viewTypeLabel = computed(
  () => ({ class: '班级', teacher: '教师', classroom: '教室' })[viewType.value]
)

function onViewTypeChange() {
  viewId.value = null
  timetable.value = null
}

async function loadTimetable() {
  if (!viewId.value) return
  loading.value = true
  try {
    if (viewType.value === 'class') {
      timetable.value = await classTimetable(viewId.value)
    } else if (viewType.value === 'teacher') {
      timetable.value = await teacherTimetable(viewId.value)
    } else {
      timetable.value = await classroomTimetable(viewId.value)
    }
  } finally {
    loading.value = false
  }
}

async function refreshSchedules() {
  allSchedules.value = await listSchedules()
}

/** 前端冲突预检：检查目标时间段上教师/班级/教室是否被其他记录占用 */
function preCheck(schedule, targetSlotId) {
  if (targetSlotId == null) return null
  const others = allSchedules.value.filter((s) => s.id !== schedule.scheduleId)
  const busy = others.find((s) => s.timeSlotId === targetSlotId)
  const conflicts = others.filter((s) => s.timeSlotId === targetSlotId)
  const teacherConflict = conflicts.find((s) => s.teacherId === schedule.teacherId)
  const classConflict = conflicts.find((s) => s.classId === schedule.classId)
  const roomConflict = conflicts.find((s) => s.classroomId === schedule.classroomId)
  if (teacherConflict) return `教师「${teacherConflict.teacherName}」在该时间段已有课程`
  if (classConflict) return `班级「${classConflict.className}」在该时间段已有课程`
  if (roomConflict) return `教室「${roomConflict.classroomName}」在该时间段已被占用`
  return null
}

async function doMove(scheduleId, timeSlotId, classroomId) {
  await moveSchedule(scheduleId, { timeSlotId, classroomId })
}

async function handleMove({ source, target, targetTo }) {
  const targetSlotId = slotIdOf(targetTo)
  if (targetSlotId == null) {
    ElMessage.warning('无效的目标时间段')
    return
  }

  // 预检源课程移到目标位置
  const err1 = preCheck(source, targetSlotId)
  if (err1) {
    ElMessage.warning(`调课冲突：${err1}`)
    return
  }

  if (target) {
    // 交换：目标课程先移到源位置，预检反向冲突
    const sourceSlotId = slotIdOf({ weekDay: source.weekDay, section: source.section })
    const err2 = preCheck(target, sourceSlotId)
    if (err2) {
      ElMessage.warning(`调课冲突：${err2}`)
      return
    }
    await ElMessageBox.confirm(
      `确定交换「${source.courseName}」与「${target.courseName}」吗？`,
      '交换课程',
      { type: 'warning' }
    )
    // 先移走目标课程到源位置，再把源课程移到目标位置
    await doMove(target.scheduleId, sourceSlotId, target.classroomId)
    await doMove(source.scheduleId, targetSlotId, source.classroomId)
    ElMessage.success('交换成功')
  } else {
    await ElMessageBox.confirm(
      `确定将「${source.courseName}」调整到 ${dayNames[targetTo.weekDay]} 第 ${targetTo.section} 节吗？`,
      '调整课程',
      { type: 'warning' }
    )
    await doMove(source.scheduleId, targetSlotId, source.classroomId)
    ElMessage.success('调课成功')
  }

  await Promise.all([loadTimetable(), refreshSchedules()])
}

function slotIdOf({ weekDay, section }) {
  return timeSlotIdMap.get(`${weekDay}-${section}`) ?? null
}

const timeSlotIdMap = new Map()

async function loadTimeSlotMap() {
  // 通过任意课表无法拿到全部时间段，改由全量排课结果+时间段接口共同构建
  const { listTimeSlots } = await import('../api/baseData')
  const slots = await listTimeSlots()
  slots.forEach((s) => timeSlotIdMap.set(`${s.weekDay}-${s.section}`, s.id))
}

function exportExcel() {
  if (!timetable.value) return
  const rows = [['节次', '周一', '周二', '周三', '周四', '周五']]
  const grid = Array.from({ length: 9 }, () => Array.from({ length: 6 }, () => ''))
  for (const cell of timetable.value.cells) {
    grid[cell.section][cell.weekDay] = `${cell.courseName}\n${cell.teacherName}\n${cell.className} ${cell.classroomName}`
  }
  for (let s = 1; s <= 8; s++) {
    rows.push([`第 ${s} 节`, ...grid[s].slice(1)])
  }
  const ws = XLSX.utils.aoa_to_sheet(rows)
  ws['!cols'] = [{ wch: 8 }, { wch: 18 }, { wch: 18 }, { wch: 18 }, { wch: 18 }, { wch: 18 }]
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '课表')
  XLSX.writeFile(wb, `${timetable.value.viewName}课表.xlsx`)
}

onMounted(async () => {
  const [t, c, r] = await Promise.all([listTeachers(), listClasses(), listClassrooms()])
  teachers.value = t
  classes.value = c
  classrooms.value = r
  await refreshSchedules()
  await loadTimeSlotMap()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.tip {
  flex: 1;
  min-width: 260px;
}
.view-name {
  margin: 4px 0 12px;
  color: #303133;
}
.muted {
  color: #c0c4cc;
  font-size: 13px;
  font-weight: normal;
}
</style>
