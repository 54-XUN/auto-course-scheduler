package com.example.scheduling.service;

import com.example.scheduling.dto.TeacherDTO;
import com.example.scheduling.dto.TeacherRequest;
import com.example.scheduling.entity.Course;
import com.example.scheduling.entity.Teacher;
import com.example.scheduling.entity.TeacherCourse;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.CourseRepository;
import com.example.scheduling.repository.ScheduleRepository;
import com.example.scheduling.repository.TeacherCourseRepository;
import com.example.scheduling.repository.TeacherRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherCourseRepository teacherCourseRepository;
    private final CourseRepository courseRepository;
    private final ScheduleRepository scheduleRepository;

    public TeacherService(TeacherRepository teacherRepository,
                          TeacherCourseRepository teacherCourseRepository,
                          CourseRepository courseRepository,
                          ScheduleRepository scheduleRepository) {
        this.teacherRepository = teacherRepository;
        this.teacherCourseRepository = teacherCourseRepository;
        this.courseRepository = courseRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public List<TeacherDTO> list() {
        Map<Long, Course> courseMap = courseRepository.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        Map<Long, List<TeacherCourse>> byTeacher = teacherCourseRepository.findAll().stream()
                .collect(Collectors.groupingBy(TeacherCourse::getTeacherId));
        return teacherRepository.findAll().stream()
                .sorted((a, b) -> a.getCode().compareToIgnoreCase(b.getCode()))
                .map(t -> toDTO(t, byTeacher.getOrDefault(t.getId(), List.of()), courseMap))
                .collect(Collectors.toList());
    }

    @Transactional
    public TeacherDTO create(TeacherRequest req) {
        teacherRepository.findByCode(req.getCode()).ifPresent(t -> {
            throw new BusinessException("教师编号已存在：" + req.getCode());
        });
        validateCourseIds(req.getCourseIds());

        Teacher teacher = new Teacher();
        apply(teacher, req);
        teacher = teacherRepository.save(teacher);
        saveTeachable(teacher.getId(), req.getCourseIds());
        return toDTO(teacher, loadAssociations(teacher.getId()), courseMap());
    }

    @Transactional
    public TeacherDTO update(Long id, TeacherRequest req) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "教师不存在"));
        if (teacherRepository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new BusinessException("教师编号已存在：" + req.getCode());
        }
        validateCourseIds(req.getCourseIds());

        apply(teacher, req);
        teacherRepository.save(teacher);
        teacherCourseRepository.deleteByTeacherId(id);
        saveTeachable(id, req.getCourseIds());
        return toDTO(teacher, loadAssociations(id), courseMap());
    }

    @Transactional
    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new BusinessException(404, "教师不存在");
        }
        if (scheduleRepository.existsByTeacherId(id)) {
            throw new BusinessException("该教师已被排课结果引用，请先清除相关排课");
        }
        teacherCourseRepository.deleteByTeacherId(id);
        teacherRepository.deleteById(id);
    }

    private void apply(Teacher teacher, TeacherRequest req) {
        teacher.setCode(req.getCode());
        teacher.setName(req.getName());
        teacher.setGender(req.getGender());
        teacher.setPhone(req.getPhone());
        teacher.setTitle(req.getTitle());
    }

    private void saveTeachable(Long teacherId, List<Long> courseIds) {
        if (courseIds == null) {
            return;
        }
        Set<Long> unique = new HashSet<>(courseIds);
        for (Long courseId : unique) {
            if (!teacherCourseRepository.existsByTeacherIdAndCourseId(teacherId, courseId)) {
                teacherCourseRepository.save(new TeacherCourse(teacherId, courseId));
            }
        }
    }

    private void validateCourseIds(List<Long> courseIds) {
        if (courseIds == null) {
            return;
        }
        for (Long courseId : new HashSet<>(courseIds)) {
            if (!courseRepository.existsById(courseId)) {
                throw new BusinessException("课程不存在：id=" + courseId);
            }
        }
    }

    private Map<Long, Course> courseMap() {
        return courseRepository.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
    }

    private List<TeacherCourse> loadAssociations(Long teacherId) {
        return teacherCourseRepository.findByTeacherId(teacherId);
    }

    private TeacherDTO toDTO(Teacher t, List<TeacherCourse> associations, Map<Long, Course> courseMap) {
        TeacherDTO dto = new TeacherDTO();
        dto.setId(t.getId());
        dto.setCode(t.getCode());
        dto.setName(t.getName());
        dto.setGender(t.getGender());
        dto.setPhone(t.getPhone());
        dto.setTitle(t.getTitle());
        List<Long> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (TeacherCourse tc : associations) {
            Course c = courseMap.get(tc.getCourseId());
            if (c != null) {
                ids.add(c.getId());
                names.add(c.getName());
            }
        }
        dto.setCourseIds(ids);
        dto.setCourseNames(names);
        return dto;
    }
}
