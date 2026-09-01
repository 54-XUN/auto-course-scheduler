<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增班级课程</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="className" label="班级" min-width="160" />
      <el-table-column prop="courseName" label="课程" min-width="160" />
      <el-table-column prop="weeklyHours" label="周学时" width="120">
        <template #default="{ row }">
          {{ row.weeklyHours ?? '使用课程默认' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑班级课程' : '新增班级课程'" width="460px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="班级" prop="classId">
        <el-select v-model="form.classId" placeholder="请选择班级" style="width: 100%">
          <el-option v-for="item in classes" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="课程" prop="courseId">
        <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
          <el-option v-for="item in courses" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="周学时" prop="weeklyHours">
        <el-input-number v-model="form.weeklyHours" :min="1" :max="8" :placeholder="'留空使用课程默认'" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { listClassCourses, createClassCourse, updateClassCourse, deleteClassCourse } from '../api/classCourse'
import { listClasses } from '../api/baseData'
import { listCourses } from '../api/baseData'

const list = ref([])
const classes = ref([])
const courses = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const emptyForm = { id: null, classId: null, courseId: null, weeklyHours: null }
const form = reactive({ ...emptyForm })

const rules = {
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const [classCourseList, classList, courseList] = await Promise.all([
      listClassCourses(),
      listClasses(),
      listCourses()
    ])
    list.value = classCourseList
    classes.value = classList
    courses.value = courseList
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, emptyForm, row ? { ...row } : {})
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = {
      classId: form.classId,
      courseId: form.courseId,
      weeklyHours: form.weeklyHours || null
    }
    if (form.id) {
      await updateClassCourse(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createClassCourse(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除「${row.className} - ${row.courseName}」吗？`, '提示', { type: 'warning' })
  await deleteClassCourse(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>
