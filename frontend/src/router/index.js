import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../views/LayoutView.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/DashboardView.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'teachers',
        name: 'teachers',
        component: () => import('../views/TeacherView.vue'),
        meta: { title: '教师管理', icon: 'User' }
      },
      {
        path: 'classes',
        name: 'classes',
        component: () => import('../views/ClassView.vue'),
        meta: { title: '班级管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'courses',
        name: 'courses',
        component: () => import('../views/CourseView.vue'),
        meta: { title: '课程管理', icon: 'Reading' }
      },
      {
        path: 'classrooms',
        name: 'classrooms',
        component: () => import('../views/ClassroomView.vue'),
        meta: { title: '教室管理', icon: 'School' }
      },
      {
        path: 'time-slots',
        name: 'timeSlots',
        component: () => import('../views/TimeSlotView.vue'),
        meta: { title: '时间段管理', icon: 'Clock' }
      },
      {
        path: 'schedules',
        name: 'schedules',
        component: () => import('../views/ScheduleView.vue'),
        meta: { title: '排课管理', icon: 'Calendar' }
      },
      {
        path: 'timetable',
        name: 'timetable',
        component: () => import('../views/TimetableView.vue'),
        meta: { title: '课表查询', icon: 'Grid' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/'
  }
  document.title = to.meta.title ? `${to.meta.title} - 自动排课系统` : '自动排课系统'
  return true
})

export default router
