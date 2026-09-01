package com.example.scheduling.service;

import com.example.scheduling.dto.TimetableDTO;
import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.entity.Classroom;
import com.example.scheduling.entity.Course;
import com.example.scheduling.entity.Schedule;
import com.example.scheduling.entity.Teacher;
import com.example.scheduling.entity.TimeSlot;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.ClassInfoRepository;
import com.example.scheduling.repository.ClassroomRepository;
import com.example.scheduling.repository.CourseRepository;
import com.example.scheduling.repository.ScheduleRepository;
import com.example.scheduling.repository.TeacherRepository;
import com.example.scheduling.repository.TimeSlotRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 课表查询：班级 / 教师 / 教室 三个视角 */
@Service
public class TimetableService {

    private final ScheduleRepository scheduleRepository;
    private final TeacherRepository teacherRepository;
    private final ClassInfoRepository classInfoRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final TimeSlotRepository timeSlotRepository;

    public TimetableService(ScheduleRepository scheduleRepository,
                            TeacherRepository teacherRepository,
                            ClassInfoRepository classInfoRepository,
                            CourseRepository courseRepository,
                            ClassroomRepository classroomRepository,
                            TimeSlotRepository timeSlotRepository) {
        this.scheduleRepository = scheduleRepository;
        this.teacherRepository = teacherRepository;
        this.classInfoRepository = classInfoRepository;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional(readOnly = true)
    public TimetableDTO classTimetable(Long classId) {
        ClassInfo ci = classInfoRepository.findById(classId)
                .orElseThrow(() -> new BusinessException(404, "班级不存在"));
        return build("class", classId, ci.getName(), ScheduleRepository::findByClassId);
    }

    @Transactional(readOnly = true)
    public TimetableDTO teacherTimetable(Long teacherId) {
        Teacher t = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessException(404, "教师不存在"));
        return build("teacher", teacherId, t.getName(), ScheduleRepository::findByTeacherId);
    }

    @Transactional(readOnly = true)
    public TimetableDTO classroomTimetable(Long classroomId) {
        Classroom r = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException(404, "教室不存在"));
        return build("classroom", classroomId, r.getName(), ScheduleRepository::findByClassroomId);
    }

    private interface ScheduleFetcher {
        List<Schedule> fetch(ScheduleRepository repo, Long id);
    }

    private TimetableDTO build(String viewType, Long viewId, String viewName, ScheduleFetcher fetcher) {
        TimetableDTO dto = new TimetableDTO();
        dto.setViewType(viewType);
        dto.setViewId(viewId);
        dto.setViewName(viewName);

        List<Schedule> schedules = fetcher.fetch(scheduleRepository, viewId);
        if (schedules.isEmpty()) {
            return dto;
        }

        Map<Long, Teacher> teacherMap = teacherRepository.findAll().stream()
                .collect(Collectors.toMap(Teacher::getId, Function.identity()));
        Map<Long, ClassInfo> classMap = classInfoRepository.findAll().stream()
                .collect(Collectors.toMap(ClassInfo::getId, Function.identity()));
        Map<Long, Course> courseMap = courseRepository.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        Map<Long, Classroom> roomMap = classroomRepository.findAll().stream()
                .collect(Collectors.toMap(Classroom::getId, Function.identity()));
        Map<Long, TimeSlot> slotMap = timeSlotRepository.findAll().stream()
                .collect(Collectors.toMap(TimeSlot::getId, Function.identity()));

        for (Schedule s : schedules) {
            TimeSlot ts = slotMap.get(s.getTimeSlotId());
            if (ts == null) {
                continue;
            }
            TimetableDTO.CellDTO cell = new TimetableDTO.CellDTO();
            cell.setWeekDay(ts.getWeekDay());
            cell.setSection(ts.getSection());
            cell.setScheduleId(s.getId());
            cell.setCourseId(s.getCourseId());
            cell.setClassId(s.getClassId());
            cell.setTeacherId(s.getTeacherId());
            cell.setClassroomId(s.getClassroomId());
            Course c = courseMap.get(s.getCourseId());
            cell.setCourseName(c != null ? c.getName() : "-");
            Teacher t = teacherMap.get(s.getTeacherId());
            cell.setTeacherName(t != null ? t.getName() : "-");
            ClassInfo ci = classMap.get(s.getClassId());
            cell.setClassName(ci != null ? ci.getName() : "-");
            Classroom r = roomMap.get(s.getClassroomId());
            cell.setClassroomName(r != null ? r.getName() : "-");
            dto.getCells().add(cell);
        }
        dto.getCells().sort((a, b) -> {
            int byDay = a.getWeekDay().compareTo(b.getWeekDay());
            return byDay != 0 ? byDay : a.getSection().compareTo(b.getSection());
        });
        return dto;
    }
}
