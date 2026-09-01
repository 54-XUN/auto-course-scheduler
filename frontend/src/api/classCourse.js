import request from './request'

export const listClassCourses = () => request.get('/class-courses')
export const createClassCourse = (data) => request.post('/class-courses', data)
export const updateClassCourse = (id, data) => request.put(`/class-courses/${id}`, data)
export const deleteClassCourse = (id) => request.delete(`/class-courses/${id}`)
