import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
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
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
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
