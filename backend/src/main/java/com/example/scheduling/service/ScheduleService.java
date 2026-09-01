package com.example.scheduling.service;

import com.example.scheduling.dto.AutoScheduleResponse;
import com.example.scheduling.dto.ScheduleDTO;
import com.example.scheduling.dto.ScheduleMoveRequest;
import com.example.scheduling.entity.ClassCourse;
import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.entity.Classroom;
import com.example.scheduling.entity.Course;
import com.example.scheduling.entity.Schedule;
import com.example.scheduling.entity.Teacher;
import com.example.scheduling.entity.TeacherCourse;
import com.example.scheduling.entity.TimeSlot;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.ClassCourseRepository;
import com.example.scheduling.repository.ClassInfoRepository;
import com.example.scheduling.repository.ClassroomRepository;
import com.example.scheduling.repository.CourseRepository;
import com.example.scheduling.repository.ScheduleRepository;
import com.example.scheduling.repository.TeacherCourseRepository;
import com.example.scheduling.repository.TeacherRepository;
import com.example.scheduling.repository.TimeSlotRepository;
import com.example.scheduling.scheduling.Placement;
import com.example.scheduling.scheduling.SchedulingEngine;
import com.example.scheduling.scheduling.SchedulingResult;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TeacherRepository teacherRepository;
    private final ClassInfoRepository classInfoRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TeacherCourseRepository teacherCourseRepository;
    private final ClassCourseRepository classCourseRepository;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           TeacherRepository teacherRepository,
                           ClassInfoRepository classInfoRepository,
                           CourseRepository courseRepository,
                           ClassroomRepository classroomRepository,
                           TimeSlotRepository timeSlotRepository,
                           TeacherCourseRepository teacherCourseRepository,
                           ClassCourseRepository classCourseRepository) {
        this.scheduleRepository = scheduleRepository;
        this.teacherRepository = teacherRepository;
        this.classInfoRepository = classInfoRepository;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.teacherCourseRepository = teacherCourseRepository;
        this.classCourseRepository = classCourseRepository;
    }

    /** 自动排课：校验数据完备性 → 事务内清空旧结果 → 贪心+回溯求解 → 写入 */
    @Transactional
    public AutoScheduleResponse autoSchedule() {
        List<Teacher> teachers = teacherRepository.findAll();
        List<ClassInfo> classes = classInfoRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();
        List<TimeSlot> timeSlots = timeSlotRepository.findAll();

        if (classes.isEmpty() || courses.isEmpty() || classrooms.isEmpty() || timeSlots.isEmpty()) {
            throw new BusinessException("基础数据不完整，请先维护班级、课程、教室与时间段数据");
        }

        Map<Long, Set<Long>> teachable = new HashMap<>();
        for (TeacherCourse tc : teacherCourseRepository.findAll()) {
            teachable.computeIfAbsent(tc.getCourseId(), k -> new HashSet<>()).add(tc.getTeacherId());
        }
        
        // 构建班级-课程映射
        Map<Long, List<ClassCourse>> classCourses = new HashMap<>();
        for (ClassInfo ci : classes) {
            List<ClassCourse> coursesForClass = classCourseRepository.findByClassId(ci.getId());
            classCourses.put(ci.getId(), coursesForClass);
        }
        
        for (Course course : courses) {
            if (teachable.getOrDefault(course.getId(), Set.of()).isEmpty()) {
                throw new BusinessException("课程「" + course.getName() + "」没有任何教师可教，请先在教师管理中设置可教课程");
            }
        }
        for (ClassInfo ci : classes) {
            boolean fit = classrooms.stream().anyMatch(r -> r.getCapacity() >= ci.getStudentCount());
            if (!fit) {
                throw new BusinessException("班级「" + ci.getName() + "」（" + ci.getStudentCount()
                        + "人）没有容量足够的教室");
            }
        }

        SchedulingEngine engine = new SchedulingEngine(teachers, classes, courses, classrooms, timeSlots, teachable, classCourses);
        SchedulingResult result = engine.run();
        if (!result.success()) {
            throw new BusinessException(409, result.message());
        }

        scheduleRepository.deleteAllInBatch();
        for (Placement p : result.placements()) {
            Schedule s = new Schedule();
            s.setClassId(p.classId());
            s.setTeacherId(p.teacherId());
            s.setCourseId(p.courseId());
            s.setClassroomId(p.classroomId());
            s.setTimeSlotId(p.timeSlotId());
            s.setWeekNumber(1);
            s.setCreatedAt(LocalDateTime.now());
            scheduleRepository.save(s);
        }
        return new AutoScheduleResponse(result.placements().size(), "排课成功，共安排 " + result.placements().size() + " 节课");
    }

    /** 全部排课结果（含关联名称） */
    public List<ScheduleDTO> list() {
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

        return scheduleRepository.findAll().stream()
                .map(s -> {
                    ScheduleDTO dto = new ScheduleDTO();
                    dto.setId(s.getId());
                    dto.setClassId(s.getClassId());
                    dto.setTeacherId(s.getTeacherId());
                    dto.setCourseId(s.getCourseId());
                    dto.setClassroomId(s.getClassroomId());
                    dto.setTimeSlotId(s.getTimeSlotId());
                    ClassInfo ci = classMap.get(s.getClassId());
                    Teacher t = teacherMap.get(s.getTeacherId());
                    Course c = courseMap.get(s.getCourseId());
                    Classroom r = roomMap.get(s.getClassroomId());
                    TimeSlot ts = slotMap.get(s.getTimeSlotId());
                    dto.setClassName(ci != null ? ci.getName() : "-");
                    dto.setTeacherName(t != null ? t.getName() : "-");
                    dto.setCourseName(c != null ? c.getName() : "-");
                    dto.setClassroomName(r != null ? r.getName() : "-");
                    if (ts != null) {
                        dto.setWeekDay(ts.getWeekDay());
                        dto.setSection(ts.getSection());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /** 手动调课：更新时间段与教室，强制冲突二次校验 */
    @Transactional
    public ScheduleDTO move(Long id, ScheduleMoveRequest req) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "排课记录不存在"));

        TimeSlot slot = timeSlotRepository.findById(req.getTimeSlotId())
                .orElseThrow(() -> new BusinessException(404, "目标时间段不存在"));
        Classroom room = classroomRepository.findById(req.getClassroomId())
                .orElseThrow(() -> new BusinessException(404, "目标教室不存在"));

        ClassInfo ci = classInfoRepository.findById(s.getClassId())
                .orElseThrow(() -> new BusinessException("班级数据缺失"));
        if (ci.getStudentCount() > room.getCapacity()) {
            throw new BusinessException("教室容量不足：班级 " + ci.getStudentCount() + " 人 > 教室容量 " + room.getCapacity() + " 人");
        }

        if (scheduleRepository.existsByTimeSlotIdAndTeacherIdAndIdNot(slot.getId(), s.getTeacherId(), id)) {
            throw new BusinessException("调课冲突：该教师在此时间段已有课程");
        }
        if (scheduleRepository.existsByTimeSlotIdAndClassIdAndIdNot(slot.getId(), s.getClassId(), id)) {
            throw new BusinessException("调课冲突：该班级在此时间段已有课程");
        }
        if (scheduleRepository.existsByTimeSlotIdAndClassroomIdAndIdNot(slot.getId(), room.getId(), id)) {
            throw new BusinessException("调课冲突：该教室在此时间段已被占用");
        }

        s.setTimeSlotId(slot.getId());
        s.setClassroomId(room.getId());
        scheduleRepository.save(s);

        return findById(id);
    }
    
    /** 根据ID查询排课结果（含关联名称） */
    public ScheduleDTO findById(Long id) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "排课记录不存在"));
        
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

        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(s.getId());
        dto.setClassId(s.getClassId());
        dto.setTeacherId(s.getTeacherId());
        dto.setCourseId(s.getCourseId());
        dto.setClassroomId(s.getClassroomId());
        dto.setTimeSlotId(s.getTimeSlotId());
        ClassInfo ci = classMap.get(s.getClassId());
        Teacher t = teacherMap.get(s.getTeacherId());
        Course c = courseMap.get(s.getCourseId());
        Classroom r = roomMap.get(s.getClassroomId());
        TimeSlot ts = slotMap.get(s.getTimeSlotId());
        dto.setClassName(ci != null ? ci.getName() : "-");
        dto.setTeacherName(t != null ? t.getName() : "-");
        dto.setCourseName(c != null ? c.getName() : "-");
        dto.setClassroomName(r != null ? r.getName() : "-");
        if (ts != null) {
            dto.setWeekDay(ts.getWeekDay());
            dto.setSection(ts.getSection());
        }
        return dto;
    }
}
