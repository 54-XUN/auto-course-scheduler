<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增课程</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="code" label="编号" width="100" />
      <el-table-column prop="name" label="课程名称" min-width="160" />
      <el-table-column prop="credit" label="学分" width="90" />
      <el-table-column prop="weeklyHours" label="周学时" width="90" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 0 ? 'danger' : 'success'" size="small">
            {{ row.type === 0 ? '必修' : '选修' }}
          </el-tag>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="460px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="编号" prop="code">
        <el-input v-model="form.code" placeholder="如 K007" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="学分" prop="credit">
        <el-input-number v-model="form.credit" :min="0.5" :max="10" :step="0.5" />
      </el-form-item>
      <el-form-item label="周学时" prop="weeklyHours">
        <el-input-number v-model="form.weeklyHours" :min="1" :max="8" />
      </el-form-item>
      <el-form-item label="类型">
        <el-radio-group v-model="form.type">
          <el-radio :value="0">必修</el-radio>
          <el-radio :value="1">选修</el-radio>
        </el-radio-group>
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
import { listCourses, createCourse, updateCourse, deleteCourse } from '../api/baseData'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const emptyForm = { id: null, code: '', name: '', credit: 2, weeklyHours: 2, type: 0 }
const form = reactive({ ...emptyForm })

const rules = {
  code: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  weeklyHours: [{ required: true, message: '请输入周学时', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    list.value = await listCourses()
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
    if (form.id) {
      await updateCourse(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createCourse(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除课程「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteCourse(row.id)
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
