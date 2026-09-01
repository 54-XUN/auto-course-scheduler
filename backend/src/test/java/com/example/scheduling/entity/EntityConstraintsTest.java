package com.example.scheduling.entity;

import com.example.scheduling.IntegrationTest;
import com.example.scheduling.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class EntityConstraintsTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private TeacherCourseRepository teacherCourseRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private ClassInfoRepository classInfoRepository;
    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        // 清理所有表
        teacherCourseRepository.deleteAll();
        scheduleRepository.deleteAll();
        userRepository.deleteAll();
        courseRepository.deleteAll();
        teacherRepository.deleteAll();
        classInfoRepository.deleteAll();
        classroomRepository.deleteAll();
        timeSlotRepository.deleteAll();
        
        // 创建基础数据
        createBaseData();
    }
    
    private void createBaseData() {
        Teacher teacher = new Teacher();
        teacher.setCode("T001");
        teacher.setName("张老师");
        teacher.setGender(1);
        teacher.setPhone("13800000001");
        teacher.setTitle("教授");
        teacherRepository.save(teacher);
        
        ClassInfo classInfo = new ClassInfo();
        classInfo.setCode("C001");
        classInfo.setName("高一(1)班");
        classInfo.setGrade("2025级");
        classInfo.setStudentCount(40);
        classInfoRepository.save(classInfo);
        
        Course course = new Course();
        course.setCode("K001");
        course.setName("语文");
        course.setCredit(java.math.BigDecimal.valueOf(3.0));
        course.setWeeklyHours(4);
        course.setType(0);
        courseRepository.save(course);
        
        Classroom classroom = new Classroom();
        classroom.setCode("R001");
        classroom.setName("教学楼101");
        classroom.setCapacity(50);
        classroom.setType(0);
        classroomRepository.save(classroom);
        
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setWeekDay(1);
        timeSlot.setSection(1);
        timeSlotRepository.save(timeSlot);
    }

    @Test
    public void testUserUsernameUniqueConstraint() {
        User user1 = new User();
        user1.setUsername("testuser");
        user1.setPassword("password123");
        user1.setRole("ADMIN");
        userRepository.save(user1);
        
        User user2 = new User();
        user2.setUsername("testuser");
        user2.setPassword("different123");
        user2.setRole("USER");
        
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(user2));
    }

    @Test
    public void testCourseCodeUniqueConstraint() {
        Course course1 = new Course();
        course1.setCode("MATH");
        course1.setName("数学");
        course1.setCredit(java.math.BigDecimal.valueOf(4.0));
        course1.setWeeklyHours(5);
        course1.setType(0);
        courseRepository.save(course1);
        
        Course course2 = new Course();
        course2.setCode("MATH");
        course2.setName("数学基础");
        course2.setCredit(java.math.BigDecimal.valueOf(2.0));
        course2.setWeeklyHours(2);
        course2.setType(1);
        
        assertThrows(DataIntegrityViolationException.class, () -> courseRepository.save(course2));
    }

    @Test
    public void testTeacherCourseUniqueConstraint() {
        Teacher teacher = teacherRepository.findByCode("T001").orElseThrow();
        Course course = courseRepository.findByCode("K001").orElseThrow();
        
        TeacherCourse tc1 = new TeacherCourse(teacher.getId(), course.getId());
        teacherCourseRepository.save(tc1);
        
        TeacherCourse tc2 = new TeacherCourse(teacher.getId(), course.getId());
        
        assertThrows(DataIntegrityViolationException.class, () -> teacherCourseRepository.save(tc2));
    }

    @Test
    public void testScheduleUniqueConstraints() {
        Teacher teacher = teacherRepository.findByCode("T001").orElseThrow();
        ClassInfo classInfo = classInfoRepository.findByCode("C001").orElseThrow();
        Course course = courseRepository.findByCode("K001").orElseThrow();
        Classroom classroom = classroomRepository.findByCode("R001").orElseThrow();
        TimeSlot timeSlot = timeSlotRepository.findAll().get(0);
        
        Schedule schedule1 = new Schedule();
        schedule1.setClassId(classInfo.getId());
        schedule1.setTeacherId(teacher.getId());
        schedule1.setCourseId(course.getId());
        schedule1.setClassroomId(classroom.getId());
        schedule1.setTimeSlotId(timeSlot.getId());
        schedule1.setWeekNumber(1);
        schedule1.setCreatedAt(java.time.LocalDateTime.now());
        scheduleRepository.save(schedule1);
        
        // 测试班级+时间段唯一约束
        Schedule schedule2 = new Schedule();
        schedule2.setClassId(classInfo.getId());
        schedule2.setTeacherId(teacher.getId());
        schedule2.setCourseId(course.getId());
        schedule2.setClassroomId(classroom.getId());
        schedule2.setTimeSlotId(timeSlot.getId());
        schedule2.setWeekNumber(1);
        schedule2.setCreatedAt(java.time.LocalDateTime.now());
        
        assertThrows(DataIntegrityViolationException.class, () -> scheduleRepository.save(schedule2));
    }

    @Test
    public void testScheduleNotNullConstraints() {
        Schedule schedule = new Schedule();
        
        // 不设置必填字段
        assertThrows(ConstraintViolationException.class, () -> entityManager.persist(schedule));
    }

    @Test
    public void testScheduleTeacherTimeSlotUniqueConstraint() {
        Teacher teacher = teacherRepository.findByCode("T001").orElseThrow();
        ClassInfo classInfo = classInfoRepository.findByCode("C001").orElseThrow();
        Course course = courseRepository.findByCode("K001").orElseThrow();
        Classroom classroom = classroomRepository.findByCode("R001").orElseThrow();
        TimeSlot timeSlot = timeSlotRepository.findAll().get(0);
        
        // 创建另一个班级和教室
        ClassInfo classInfo2 = new ClassInfo();
        classInfo2.setCode("C002");
        classInfo2.setName("高一(2)班");
        classInfo2.setGrade("2025级");
        classInfo2.setStudentCount(45);
        classInfoRepository.save(classInfo2);
        
        Classroom classroom2 = new Classroom();
        classroom2.setCode("R002");
        classroom2.setName("教学楼102");
        classroom2.setCapacity(50);
        classroom2.setType(0);
        classroomRepository.save(classroom2);
        
        Schedule schedule1 = new Schedule();
        schedule1.setClassId(classInfo.getId());
        schedule1.setTeacherId(teacher.getId());
        schedule1.setCourseId(course.getId());
        schedule1.setClassroomId(classroom.getId());
        schedule1.setTimeSlotId(timeSlot.getId());
        schedule1.setWeekNumber(1);
        schedule1.setCreatedAt(java.time.LocalDateTime.now());
        scheduleRepository.save(schedule1);
        
        // 测试教师+时间段唯一约束（同教师不同班级和教室）
        Schedule schedule2 = new Schedule();
        schedule2.setClassId(classInfo2.getId());
        schedule2.setTeacherId(teacher.getId()); // 同教师
        schedule2.setCourseId(course.getId());
        schedule2.setClassroomId(classroom2.getId()); // 不同教室
        schedule2.setTimeSlotId(timeSlot.getId()); // 同时间段
        schedule2.setWeekNumber(1);
        schedule2.setCreatedAt(java.time.LocalDateTime.now());
        
        assertThrows(DataIntegrityViolationException.class, () -> scheduleRepository.save(schedule2));
    }
}