import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { useAuthStore } from '../stores/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  withCredentials: true
})

request.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  // 登录后后端同时返回 token 与设置 Cookie；优先使用 Cookie，
  // 但在 Cookie 无法写入的预览/沙箱环境中使用 Authorization 头兜底
  if (authStore.token && authStore.token !== 'cookie') {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      const message = data?.message || '请求失败'
      if (status === 401) {
        const authStore = useAuthStore()
        authStore.clearSession()
        // 未登录时查询当前用户返回 401 是正常流程，不弹窗
        if (error.config.url !== '/auth/me') {
          ElMessage.error('登录已过期，请重新登录')
        }
      } else {
        ElMessage.error(message)
      }
      return Promise.reject(new Error(message))
    }
    ElMessage.error('网络异常，请检查后端服务是否启动')
    return Promise.reject(error)
  }
)

export default request
