package com.example.scheduling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.scheduling.dto.ScheduleDTO;
import com.example.scheduling.dto.ScheduleMoveRequest;
import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.entity.Classroom;
import com.example.scheduling.entity.Course;
import com.example.scheduling.entity.Schedule;
import com.example.scheduling.entity.Teacher;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/** 排课服务业务校验测试：手动调课交换、自动排课前校验 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private ClassInfoRepository classInfoRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private TeacherCourseRepository teacherCourseRepository;
    @Mock
    private ClassCourseRepository classCourseRepository;

    @Spy
    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void autoSchedule_emptyTeachers_throws() {
        when(teacherRepository.findAll()).thenReturn(List.of());
        when(classInfoRepository.findAll()).thenReturn(List.of(new ClassInfo()));
        when(courseRepository.findAll()).thenReturn(List.of(new Course()));
        when(classroomRepository.findAll()).thenReturn(List.of(new Classroom()));
        when(timeSlotRepository.findAll()).thenReturn(List.of(new TimeSlot()));

        BusinessException ex = assertThrows(BusinessException.class, () -> scheduleService.autoSchedule());
        assertEquals(409, ex.getCode());
        assertTrue(ex.getMessage().contains("教师"));
    }

    @Test
    void move_singleConflictDifferentClassroom_swapsAtomically() {
        // 源排课：class=10, teacher=20, classroom=40, timeSlot=50
        Schedule source = new Schedule();
        source.setId(1L);
        source.setClassId(10L);
        source.setTeacherId(20L);
        source.setCourseId(30L);
        source.setClassroomId(40L);
        source.setTimeSlotId(50L);

        // 目标排课：class=11, teacher=21, classroom=41, timeSlot=50（同一时段不同教室）
        Schedule target = new Schedule();
        target.setId(2L);
        target.setClassId(11L);
        target.setTeacherId(21L);
        target.setCourseId(31L);
        target.setClassroomId(41L);
        target.setTimeSlotId(50L);

        TimeSlot targetSlot = new TimeSlot();
        targetSlot.setId(50L);
        targetSlot.setWeekDay(1);
        targetSlot.setSection(1);

        Classroom targetRoom = new Classroom();
        targetRoom.setId(41L);
        targetRoom.setCapacity(50);

        ClassInfo sourceClass = new ClassInfo();
        sourceClass.setId(10L);
        sourceClass.setStudentCount(40);

        ClassInfo targetClass = new ClassInfo();
        targetClass.setId(11L);
        targetClass.setStudentCount(35);

        Classroom sourceRoom = new Classroom();
        sourceRoom.setId(40L);
        sourceRoom.setCapacity(50);

        ScheduleMoveRequest req = new ScheduleMoveRequest();
        req.setTimeSlotId(50L);
        req.setClassroomId(41L);

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(source));
        when(timeSlotRepository.findById(50L)).thenReturn(Optional.of(targetSlot));
        when(classroomRepository.findById(41L)).thenReturn(Optional.of(targetRoom));
        when(classInfoRepository.findById(10L)).thenReturn(Optional.of(sourceClass));
        // 仅教室冲突命中目标排课
        when(scheduleRepository.findByTimeSlotIdAndTeacherIdAndIdNot(50L, 20L, 1L)).thenReturn(Optional.empty());
        when(scheduleRepository.findByTimeSlotIdAndClassIdAndIdNot(50L, 10L, 1L)).thenReturn(Optional.empty());
        when(scheduleRepository.findByTimeSlotIdAndClassroomIdAndIdNot(50L, 41L, 1L)).thenReturn(Optional.of(target));
        when(scheduleRepository.findById(2L)).thenReturn(Optional.of(target));
        when(classInfoRepository.findById(11L)).thenReturn(Optional.of(targetClass));
        when(classroomRepository.findById(40L)).thenReturn(Optional.of(sourceRoom));
        when(scheduleRepository.findByTimeSlotId(50L)).thenReturn(List.of(source, target));

        doReturn(new ScheduleDTO()).when(scheduleService).findById(1L);

        scheduleService.move(1L, req);

        // 验证进行了原子交换：source -> (50, 41)，target -> (50, 40)
        verify(scheduleRepository).swapPositions(1L, 50L, 41L, 2L, 50L, 40L);
    }

    @Test
    void move_targetRoomTooSmallForSourceClass_throws() {
        Schedule source = new Schedule();
        source.setId(1L);
        source.setClassId(10L);
        source.setTeacherId(20L);
        source.setCourseId(30L);
        source.setClassroomId(40L);
        source.setTimeSlotId(50L);

        TimeSlot targetSlot = new TimeSlot();
        targetSlot.setId(60L);

        Classroom targetRoom = new Classroom();
        targetRoom.setId(41L);
        targetRoom.setCapacity(30);

        ClassInfo sourceClass = new ClassInfo();
        sourceClass.setId(10L);
        sourceClass.setStudentCount(40);

        ScheduleMoveRequest req = new ScheduleMoveRequest();
        req.setTimeSlotId(60L);
        req.setClassroomId(41L);

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(source));
        when(timeSlotRepository.findById(60L)).thenReturn(Optional.of(targetSlot));
        when(classroomRepository.findById(41L)).thenReturn(Optional.of(targetRoom));
        when(classInfoRepository.findById(10L)).thenReturn(Optional.of(sourceClass));

        BusinessException ex = assertThrows(BusinessException.class, () -> scheduleService.move(1L, req));
        assertTrue(ex.getMessage().contains("容量不足"));
    }
}
