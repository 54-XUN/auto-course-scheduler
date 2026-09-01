<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增教室</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="code" label="编号" width="100" />
      <el-table-column prop="name" label="教室名称" min-width="160" />
      <el-table-column prop="capacity" label="容量" width="100" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.type)" size="small">{{ typeName(row.type) }}</el-tag>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑教室' : '新增教室'" width="460px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="编号" prop="code">
        <el-input v-model="form.code" placeholder="如 R007" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="容量" prop="capacity">
        <el-input-number v-model="form.capacity" :min="1" :max="1000" />
      </el-form-item>
      <el-form-item label="类型">
        <el-radio-group v-model="form.type">
          <el-radio :value="0">普通</el-radio>
          <el-radio :value="1">实验室</el-radio>
          <el-radio :value="2">多媒体</el-radio>
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
import { listClassrooms, createClassroom, updateClassroom, deleteClassroom } from '../api/baseData'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const emptyForm = { id: null, code: '', name: '', capacity: 50, type: 0 }
const form = reactive({ ...emptyForm })

const rules = {
  code: [{ required: true, message: '请输入教室编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入教室名称', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}

function typeName(type) {
  return ['普通', '实验室', '多媒体'][type] ?? '未知'
}
function typeTag(type) {
  return ['info', 'warning', 'success'][type] ?? 'info'
}

async function load() {
  loading.value = true
  try {
    list.value = await listClassrooms()
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
      await updateClassroom(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createClassroom(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除教室「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteClassroom(row.id)
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
