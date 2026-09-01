package com.example.scheduling.service;

import com.example.scheduling.entity.Course;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.CourseRepository;
import com.example.scheduling.repository.ScheduleRepository;
import com.example.scheduling.repository.TeacherCourseRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final ScheduleRepository scheduleRepository;
    private final TeacherCourseRepository teacherCourseRepository;

    public CourseService(CourseRepository courseRepository,
                         ScheduleRepository scheduleRepository,
                         TeacherCourseRepository teacherCourseRepository) {
        this.courseRepository = courseRepository;
        this.scheduleRepository = scheduleRepository;
        this.teacherCourseRepository = teacherCourseRepository;
    }

    public List<Course> list() {
        return courseRepository.findAll().stream()
                .sorted(Comparator.comparing(Course::getCode))
                .toList();
    }

    @Transactional
    public Course create(Course course) {
        courseRepository.findByCode(course.getCode()).ifPresent(c -> {
            throw new BusinessException("课程编号已存在：" + course.getCode());
        });
        return courseRepository.save(course);
    }

    @Transactional
    public Course update(Long id, Course req) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "课程不存在"));
        if (courseRepository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new BusinessException("课程编号已存在：" + req.getCode());
        }
        course.setCode(req.getCode());
        course.setName(req.getName());
        course.setCredit(req.getCredit());
        course.setWeeklyHours(req.getWeeklyHours());
        course.setType(req.getType());
        return courseRepository.save(course);
    }

    @Transactional
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new BusinessException(404, "课程不存在");
        }
        if (scheduleRepository.existsByCourseId(id)) {
            throw new BusinessException("该课程已被排课结果引用，请先清除相关排课");
        }
        teacherCourseRepository.deleteByCourseId(id);
        courseRepository.deleteById(id);
    }
}
