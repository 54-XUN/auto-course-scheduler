package com.example.scheduling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.scheduling.dto.TeacherRequest;
import com.example.scheduling.entity.Teacher;
import com.example.scheduling.entity.TeacherCourse;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.CourseRepository;
import com.example.scheduling.repository.ScheduleRepository;
import com.example.scheduling.repository.TeacherCourseRepository;
import com.example.scheduling.repository.TeacherRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 教师服务业务校验测试：编号唯一、可教课程关联、删除保护 */
@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private TeacherCourseRepository teacherCourseRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private TeacherService teacherService;

    @Test
    void create_duplicateCode_throws() {
        TeacherRequest req = request("T001");
        when(teacherRepository.findByCode("T001")).thenReturn(Optional.of(new Teacher()));

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.create(req));
        assertEquals("教师编号已存在：T001", ex.getMessage());
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void create_withCourseIds_savesAssociations() {
        TeacherRequest req = request("T009");
        req.setCourseIds(List.of(1L, 2L));
        long savedTeacherId = 5L;
        when(teacherRepository.findByCode("T009")).thenReturn(Optional.empty());
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(2L)).thenReturn(true);
        when(teacherRepository.save(any())).thenAnswer(inv -> {
            Teacher t = inv.getArgument(0);
            t.setId(savedTeacherId);
            return t;
        });
        when(teacherCourseRepository.existsByTeacherIdAndCourseId(anyLong(), anyLong())).thenReturn(false);
        when(courseRepository.findAll()).thenReturn(List.of());

        teacherService.create(req);

        ArgumentCaptor<TeacherCourse> captor = ArgumentCaptor.forClass(TeacherCourse.class);
        verify(teacherCourseRepository, times(2)).save(captor.capture());
        List<Long> savedCourseIds = captor.getAllValues().stream()
                .map(TeacherCourse::getCourseId)
                .sorted()
                .toList();
        assertEquals(List.of(1L, 2L), savedCourseIds);
        captor.getAllValues().forEach(tc -> assertEquals(savedTeacherId, tc.getTeacherId()));
    }

    @Test
    void create_unknownCourseId_throws() {
        TeacherRequest req = request("T009");
        req.setCourseIds(List.of(99L));
        when(teacherRepository.findByCode("T009")).thenReturn(Optional.empty());
        when(courseRepository.existsById(99L)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.create(req));
        assertEquals("课程不存在：id=99", ex.getMessage());
    }

    @Test
    void delete_referencedBySchedule_blocked() {
        when(teacherRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.existsByTeacherId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.delete(1L));
        assertEquals("该教师已被排课结果引用，请先清除相关排课", ex.getMessage());
        verify(teacherRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_notReferenced_removesAssociationsAndTeacher() {
        when(teacherRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.existsByTeacherId(1L)).thenReturn(false);

        teacherService.delete(1L);

        verify(teacherCourseRepository).deleteByTeacherId(1L);
        verify(teacherRepository).deleteById(1L);
    }

    private TeacherRequest request(String code) {
        TeacherRequest req = new TeacherRequest();
        req.setCode(code);
        req.setName("张老师");
        req.setGender(1);
        req.setPhone("13800000000");
        req.setTitle("讲师");
        return req;
    }
}
