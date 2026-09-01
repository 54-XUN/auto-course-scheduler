import request from './request'

export const listTeachers = () => request.get('/teachers')
export const createTeacher = (data) => request.post('/teachers', data)
export const updateTeacher = (id, data) => request.put(`/teachers/${id}`, data)
export const deleteTeacher = (id) => request.delete(`/teachers/${id}`)

export const listClasses = () => request.get('/classes')
export const createClass = (data) => request.post('/classes', data)
export const updateClass = (id, data) => request.put(`/classes/${id}`, data)
export const deleteClass = (id) => request.delete(`/classes/${id}`)

export const listCourses = () => request.get('/courses')
export const createCourse = (data) => request.post('/courses', data)
export const updateCourse = (id, data) => request.put(`/courses/${id}`, data)
export const deleteCourse = (id) => request.delete(`/courses/${id}`)

export const listClassrooms = () => request.get('/classrooms')
export const createClassroom = (data) => request.post('/classrooms', data)
export const updateClassroom = (id, data) => request.put(`/classrooms/${id}`, data)
export const deleteClassroom = (id) => request.delete(`/classrooms/${id}`)

export const listTimeSlots = () => request.get('/time-slots')
export const updateTimeSlot = (id, data) => request.put(`/time-slots/${id}`, data)
export const deleteTimeSlot = (id) => request.delete(`/time-slots/${id}`)
