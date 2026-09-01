package com.example.scheduling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.scheduling.entity.Course;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.ClassCourseRepository;
import com.example.scheduling.repository.CourseRepository;
import com.example.scheduling.repository.ScheduleRepository;
import com.example.scheduling.repository.TeacherCourseRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 课程服务业务校验测试：删除时清理可教课程与班级课程关联 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private TeacherCourseRepository teacherCourseRepository;
    @Mock
    private ClassCourseRepository classCourseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void delete_referencedBySchedule_blocked() {
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.existsByCourseId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.delete(1L));
        assertEquals("该课程已被排课结果引用，请先清除相关排课", ex.getMessage());
        verify(teacherCourseRepository, never()).deleteByCourseId(1L);
        verify(classCourseRepository, never()).deleteByCourseId(1L);
        verify(courseRepository, never()).deleteById(1L);
    }

    @Test
    void delete_notReferenced_removesAssociationsAndCourse() {
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.existsByCourseId(1L)).thenReturn(false);

        courseService.delete(1L);

        verify(teacherCourseRepository).deleteByCourseId(1L);
        verify(classCourseRepository).deleteByCourseId(1L);
        verify(courseRepository).deleteById(1L);
    }

    @Test
    void update_persistsAllFields() {
        Course existing = new Course();
        existing.setId(1L);
        existing.setCode("K001");
        existing.setName("语文");
        existing.setCredit(new BigDecimal("2.0"));
        existing.setWeeklyHours(2);
        existing.setType(0);

        Course req = new Course();
        req.setCode("K001");
        req.setName("语文（新）");
        req.setCredit(new BigDecimal("3.0"));
        req.setWeeklyHours(3);
        req.setType(1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseRepository.existsByCodeAndIdNot("K001", 1L)).thenReturn(false);
        when(courseRepository.save(existing)).thenReturn(existing);

        Course result = courseService.update(1L, req);

        assertEquals("语文（新）", result.getName());
        assertEquals(new BigDecimal("3.0"), result.getCredit());
        assertEquals(3, result.getWeeklyHours());
        assertEquals(1, result.getType());
    }
}
