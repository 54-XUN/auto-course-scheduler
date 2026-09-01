package com.example.scheduling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.scheduling.dto.ClassCourseDTO;
import com.example.scheduling.entity.ClassCourse;
import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.entity.Course;
import com.example.scheduling.repository.ClassCourseRepository;
import com.example.scheduling.repository.ClassInfoRepository;
import com.example.scheduling.repository.CourseRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 班级课程关联服务测试：列表排序对缺失的班级/课程不抛 NPE */
@ExtendWith(MockitoExtension.class)
class ClassCourseServiceTest {

    @Mock
    private ClassCourseRepository classCourseRepository;
    @Mock
    private ClassInfoRepository classInfoRepository;
    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ClassCourseService classCourseService;

    @Test
    void list_missingClassOrCourse_doesNotThrow() {
        ClassInfo c1 = new ClassInfo();
        c1.setId(1L);
        c1.setCode("C001");
        c1.setName("高一(1)班");

        Course k1 = new Course();
        k1.setId(10L);
        k1.setCode("K001");
        k1.setName("语文");

        ClassCourse cc1 = new ClassCourse(1L, 10L, 4);
        cc1.setId(100L);
        // classId=99 与 courseId=99 在对应表中不存在
        ClassCourse cc2 = new ClassCourse(99L, 99L, 2);
        cc2.setId(101L);

        when(classCourseRepository.findAll()).thenReturn(List.of(cc2, cc1));
        when(classInfoRepository.findAll()).thenReturn(List.of(c1));
        when(courseRepository.findAll()).thenReturn(List.of(k1));

        List<ClassCourseDTO> result = classCourseService.list();

        assertEquals(2, result.size());
        // 缺失记录排在前面，返回占位符
        assertEquals("-", result.get(0).getClassName());
        assertEquals("-", result.get(0).getCourseName());
        // 有效记录正常展示名称
        assertEquals("高一(1)班", result.get(1).getClassName());
        assertEquals("语文", result.get(1).getCourseName());
    }

    @Test
    void list_sortedByClassCodeAndCourseCode() {
        ClassInfo c1 = new ClassInfo();
        c1.setId(1L);
        c1.setCode("C002");
        c1.setName("c2");
        ClassInfo c2 = new ClassInfo();
        c2.setId(2L);
        c2.setCode("C001");
        c2.setName("c1");

        Course k1 = new Course();
        k1.setId(10L);
        k1.setCode("K002");
        k1.setName("k2");
        Course k2 = new Course();
        k2.setId(20L);
        k2.setCode("K001");
        k2.setName("k1");

        ClassCourse cc1 = new ClassCourse(1L, 10L, 4);
        cc1.setId(100L);
        ClassCourse cc2 = new ClassCourse(2L, 20L, 3);
        cc2.setId(101L);

        when(classCourseRepository.findAll()).thenReturn(List.of(cc1, cc2));
        when(classInfoRepository.findAll()).thenReturn(List.of(c1, c2));
        when(courseRepository.findAll()).thenReturn(List.of(k1, k2));

        List<ClassCourseDTO> result = classCourseService.list();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getClassName().compareTo(result.get(1).getClassName()) <= 0,
                "结果应按班级编号排序");
    }
}
