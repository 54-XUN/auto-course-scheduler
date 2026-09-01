import { defineStore } from 'pinia'
import { login as loginApi, getMe } from '../api/auth'
import router from '../router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    username: '',
    role: ''
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
    },
    clearSession() {
      this.token = ''
      this.username = ''
      this.role = ''
    },
    logout() {
      this.clearSession()
      router.push('/login')
    },
    async init() {
      try {
        const data = await getMe()
        this.username = data.username
        this.role = data.role
        this.token = 'cookie'
      } catch {
        this.token = ''
        this.username = ''
        this.role = ''
      }
    }
  }
})
