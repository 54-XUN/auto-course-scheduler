import { defineStore } from 'pinia'
import { login as loginApi } from '../api/auth'
import router from '../router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || ''
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    async login(username, password) {
      const data = await loginApi({ username, password })
      this.token = data.token
      this.username = data.username
      this.role = data.role
      localStorage.setItem('token', data.token)
      localStorage.setItem('username', data.username)
      localStorage.setItem('role', data.role)
    },
    logout() {
      this.token = ''
      this.username = ''
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      router.push('/login')
    }
  }
})
