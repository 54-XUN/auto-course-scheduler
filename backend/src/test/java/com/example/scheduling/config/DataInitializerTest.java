package com.example.scheduling.config;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.scheduling.repository.ClassCourseRepository;
import com.example.scheduling.repository.ClassInfoRepository;
import com.example.scheduling.repository.ClassroomRepository;
import com.example.scheduling.repository.CourseRepository;
import com.example.scheduling.repository.TeacherCourseRepository;
import com.example.scheduling.repository.TeacherRepository;
import com.example.scheduling.repository.TimeSlotRepository;
import com.example.scheduling.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

/** 数据初始化测试：前置数据部分缺失时不应抛出异常 */
@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private ClassInfoRepository classInfoRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private TeacherCourseRepository teacherCourseRepository;
    @Mock
    private ClassCourseRepository classCourseRepository;
    @Mock
    private ApplicationArguments args;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void run_missingPrerequisiteTeacher_skipsAssociationInit() {
        when(userRepository.count()).thenReturn(1L);
        when(timeSlotRepository.count()).thenReturn(1L);
        when(teacherRepository.count()).thenReturn(0L);
        when(teacherRepository.findByCode("T001")).thenReturn(Optional.empty());
        when(classInfoRepository.count()).thenReturn(1L);
        when(courseRepository.count()).thenReturn(1L);
        when(classroomRepository.count()).thenReturn(1L);
        when(teacherCourseRepository.count()).thenReturn(0L);
        when(classCourseRepository.count()).thenReturn(0L);

        dataInitializer.run(args);

        verify(teacherCourseRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
        verify(classCourseRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }
}
