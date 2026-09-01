<template>
  <div class="grid-wrapper">
    <table class="timetable">
      <thead>
        <tr>
          <th class="section-col">节次</th>
          <th v-for="d in 5" :key="d">{{ dayNames[d] }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in 8" :key="s">
          <td class="section-col">第 {{ s }} 节</td>
          <td
            v-for="d in 5"
            :key="d"
            class="cell"
            :class="{ 'cell-droppable': canDrop(d, s) }"
            @dragover.prevent
            @drop="onDrop(d, s)"
          >
            <div
              v-if="grid[d][s]"
              class="lesson"
              draggable="true"
              :class="{ dragging: dragging && dragging.scheduleId === grid[d][s].scheduleId }"
              @dragstart="onDragStart(grid[d][s])"
              @dragend="onDragEnd"
            >
              <div class="lesson-course">{{ grid[d][s].courseName }}</div>
              <div class="lesson-meta">{{ grid[d][s].teacherName }}</div>
              <div class="lesson-meta">{{ grid[d][s].className }} | {{ grid[d][s].classroomName }}</div>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  cells: { type: Array, default: () => [] },
  selectedClassroomId: { type: Number, default: null }
})
const emit = defineEmits(['move'])

const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

const grid = computed(() => {
  const g = Array.from({ length: 6 }, () => Array.from({ length: 9 }, () => null))
  for (const cell of props.cells) {
    g[cell.weekDay][cell.section] = cell
  }
  return g
})

const dragging = ref(null)

function onDragStart(cell) {
  dragging.value = cell
}

function onDragEnd() {
  dragging.value = null
}

function onDrop(weekDay, section) {
  const from = dragging.value
  dragging.value = null
  if (!from) return
  if (from.weekDay === weekDay && from.section === section) return

  const target = grid.value[weekDay][section]
  if (target) {
    // 交换：目标课程移到源位置
    emit('move', {
      source: from,
      target,
      sourceTo: { weekDay: from.weekDay, section: from.section },
      targetTo: { weekDay, section }
    })
  } else {
    emit('move', { source: from, target: null, targetTo: { weekDay, section } })
  }
}

function canDrop(weekDay, section) {
  if (!dragging.value) return false
  const from = dragging.value
  return !(from.weekDay === weekDay && from.section === section)
}
</script>

<style scoped>
.grid-wrapper {
  overflow-x: auto;
}
.timetable {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}
.timetable th,
.timetable td {
  border: 1px solid #e4e7ed;
  text-align: center;
  vertical-align: middle;
}
.timetable th {
  background: #f5f7fa;
  padding: 10px 0;
  color: #303133;
}
.section-col {
  width: 80px;
  background: #fafafa;
  color: #606266;
  font-size: 13px;
}
.cell {
  height: 72px;
  padding: 2px;
}
.cell-droppable {
  background: #f0f9eb;
}
.lesson {
  height: 100%;
  border-radius: 6px;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  padding: 6px 4px;
  cursor: grab;
  transition: box-shadow 0.2s;
}
.lesson:hover {
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}
.lesson.dragging {
  opacity: 0.5;
}
.lesson-course {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}
.lesson-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>
