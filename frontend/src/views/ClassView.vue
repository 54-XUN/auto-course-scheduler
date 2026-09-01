<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增班级</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="code" label="编号" width="120" />
      <el-table-column prop="name" label="班级名称" min-width="160" />
      <el-table-column prop="grade" label="年级" width="120" />
      <el-table-column prop="studentCount" label="人数" width="100" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑班级' : '新增班级'" width="460px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="编号" prop="code">
        <el-input v-model="form.code" placeholder="如 C005" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" placeholder="如 高一(3)班" />
      </el-form-item>
      <el-form-item label="年级" prop="grade">
        <el-input v-model="form.grade" placeholder="如 2025级" />
      </el-form-item>
      <el-form-item label="人数" prop="studentCount">
        <el-input-number v-model="form.studentCount" :min="1" :max="500" />
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
import { listClasses, createClass, updateClass, deleteClass } from '../api/baseData'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const emptyForm = { id: null, code: '', name: '', grade: '', studentCount: 40 }
const form = reactive({ ...emptyForm })

const rules = {
  code: [{ required: true, message: '请输入班级编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
  studentCount: [{ required: true, message: '请输入人数', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    list.value = await listClasses()
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
      await updateClass(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createClass(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除班级「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteClass(row.id)
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
