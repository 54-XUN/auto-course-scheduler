<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增教师</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="code" label="编号" width="100" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column label="性别" width="80">
        <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column label="可教课程" min-width="200">
        <template #default="{ row }">
          <el-tag
            v-for="name in row.courseNames"
            :key="name"
            size="small"
            class="course-tag"
          >
            {{ name }}
          </el-tag>
          <span v-if="!row.courseNames || row.courseNames.length === 0" class="muted">未设置</span>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑教师' : '新增教师'" width="520px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="编号" prop="code">
        <el-input v-model="form.code" placeholder="如 T006" />
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio :value="1">男</el-radio>
          <el-radio :value="0">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="职称">
        <el-select v-model="form.title" placeholder="选择职称" clearable style="width: 100%">
          <el-option v-for="t in titles" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="可教课程">
        <el-select v-model="form.courseIds" multiple placeholder="选择可教课程" style="width: 100%">
          <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
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
import {
  listTeachers,
  createTeacher,
  updateTeacher,
  deleteTeacher,
  listCourses
} from '../api/baseData'

const list = ref([])
const courses = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const titles = ['助教', '讲师', '副教授', '教授']

const emptyForm = {
  id: null,
  code: '',
  name: '',
  gender: 1,
  phone: '',
  title: '',
  courseIds: []
}
const form = reactive({ ...emptyForm })

const rules = {
  code: [{ required: true, message: '请输入教师编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    list.value = await listTeachers()
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, emptyForm, row ? { ...row } : {})
  if (row && !form.courseIds) form.courseIds = []
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateTeacher(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createTeacher(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除教师「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteTeacher(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(async () => {
  await load()
  courses.value = await listCourses()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.course-tag {
  margin-right: 4px;
}
.muted {
  color: #c0c4cc;
}
</style>
