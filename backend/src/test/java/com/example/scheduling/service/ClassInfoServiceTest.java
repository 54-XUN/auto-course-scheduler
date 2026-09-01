package com.example.scheduling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.ClassCourseRepository;
import com.example.scheduling.repository.ClassInfoRepository;
import com.example.scheduling.repository.ScheduleRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 班级服务业务校验测试：删除时清理班级课程关联 */
@ExtendWith(MockitoExtension.class)
class ClassInfoServiceTest {

    @Mock
    private ClassInfoRepository classInfoRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private ClassCourseRepository classCourseRepository;

    @InjectMocks
    private ClassInfoService classInfoService;

    @Test
    void delete_referencedBySchedule_blocked() {
        when(classInfoRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.existsByClassId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> classInfoService.delete(1L));
        assertEquals("该班级已被排课结果引用，请先清除相关排课", ex.getMessage());
        verify(classCourseRepository, never()).deleteByClassId(1L);
        verify(classInfoRepository, never()).deleteById(1L);
    }

    @Test
    void delete_notReferenced_removesAssociationsAndClass() {
        when(classInfoRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.existsByClassId(1L)).thenReturn(false);

        classInfoService.delete(1L);

        verify(classCourseRepository).deleteByClassId(1L);
        verify(classInfoRepository).deleteById(1L);
    }

    @Test
    void update_persistsAllFields() {
        ClassInfo existing = new ClassInfo();
        existing.setId(1L);
        existing.setCode("C001");
        existing.setName("高一(1)班");
        existing.setGrade("2025级");
        existing.setStudentCount(40);

        ClassInfo req = new ClassInfo();
        req.setCode("C001");
        req.setName("高一(1)班（新）");
        req.setGrade("2026级");
        req.setStudentCount(45);

        when(classInfoRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(classInfoRepository.existsByCodeAndIdNot("C001", 1L)).thenReturn(false);
        when(classInfoRepository.save(existing)).thenReturn(existing);

        ClassInfo result = classInfoService.update(1L, req);

        assertEquals("高一(1)班（新）", result.getName());
        assertEquals("2026级", result.getGrade());
        assertEquals(45, result.getStudentCount());
    }
}
