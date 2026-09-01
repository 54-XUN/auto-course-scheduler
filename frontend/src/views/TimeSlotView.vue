<template>
  <el-card shadow="never">
    <el-alert
      title="时间段为系统预置数据（周一至周五 × 8 节），禁止删除，可修改节次时间标识"
      type="info"
      :closable="false"
      style="margin-bottom: 12px"
    />

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column label="星期" width="140">
        <template #default="{ row }">{{ dayName(row.weekDay) }}</template>
      </el-table-column>
      <el-table-column label="节次" width="120">
        <template #default="{ row }">第 {{ row.section }} 节</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="编辑时间段" width="400px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="星期" prop="weekDay">
        <el-select v-model="form.weekDay" style="width: 100%">
          <el-option v-for="d in 5" :key="d" :label="dayName(d)" :value="d" />
        </el-select>
      </el-form-item>
      <el-form-item label="节次" prop="section">
        <el-select v-model="form.section" style="width: 100%">
          <el-option v-for="s in 8" :key="s" :label="`第 ${s} 节`" :value="s" />
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
import { ElMessage } from 'element-plus'
import { listTimeSlots, updateTimeSlot } from '../api/baseData'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({ id: null, weekDay: 1, section: 1 })
const rules = {
  weekDay: [{ required: true, message: '请选择星期', trigger: 'change' }],
  section: [{ required: true, message: '请选择节次', trigger: 'change' }]
}

const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
function dayName(d) {
  return dayNames[d] || d
}

async function load() {
  loading.value = true
  try {
    list.value = await listTimeSlots()
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await updateTimeSlot(form.id, form)
    ElMessage.success('修改成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
