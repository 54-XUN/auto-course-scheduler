import request from './request'

export const autoSchedule = () => request.post('/schedules/auto')
export const listSchedules = () => request.get('/schedules')
export const moveSchedule = (id, data) => request.put(`/schedules/${id}`, data)

export const classTimetable = (id) => request.get(`/timetables/class/${id}`)
export const teacherTimetable = (id) => request.get(`/timetables/teacher/${id}`)
export const classroomTimetable = (id) => request.get(`/timetables/classroom/${id}`)
