package com.example.scheduling.scheduling;

import com.example.scheduling.IntegrationTest;
import com.example.scheduling.entity.*;
import com.example.scheduling.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class SchedulingEngineClassCourseTest extends IntegrationTest {

    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private ClassInfoRepository classInfoRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private TeacherCourseRepository teacherCourseRepository;
    @Autowired
    private ClassCourseRepository classCourseRepository;

    private List<Teacher> teachers;
    private List<ClassInfo> classes;
    private List<Course> courses;
    private List<Classroom> classrooms;
    private List<TimeSlot> timeSlots;

    @BeforeEach
    public void setup() {
        // 清理现有数据
        teacherCourseRepository.deleteAll();
        classCourseRepository.deleteAll();
        teacherRepository.deleteAll();
        classInfoRepository.deleteAll();
        courseRepository.deleteAll();
        classroomRepository.deleteAll();
        timeSlotRepository.deleteAll();

        // 创建基础数据
        teachers = createTeachers();
        classes = createClasses();
        courses = createCourses();
        classrooms = createClassrooms();
        timeSlots = createTimeSlots();
        createTeacherCourses();
    }

    private List<Teacher> createTeachers() {
        Teacher t1 = new Teacher();
        t1.setCode("T001");
        t1.setName("张老师");
        t1.setGender(1);
        t1.setPhone("13800000001");
        t1.setTitle("教授");
        teacherRepository.save(t1);

        Teacher t2 = new Teacher();
        t2.setCode("T002");
        t2.setName("李老师");
        t2.setGender(0);
        t2.setPhone("13800000002");
        t2.setTitle("副教授");
        teacherRepository.save(t2);

        return teacherRepository.findAll();
    }

    private List<ClassInfo> createClasses() {
        ClassInfo c1 = new ClassInfo();
        c1.setCode("C001");
        c1.setName("高一(1)班");
        c1.setGrade("2025级");
        c1.setStudentCount(40);
        classInfoRepository.save(c1);

        ClassInfo c2 = new ClassInfo();
        c2.setCode("C002");
        c2.setName("高一(2)班");
        c2.setGrade("2025级");
        c2.setStudentCount(45);
        classInfoRepository.save(c2);

        return classInfoRepository.findAll();
    }

    private List<Course> createCourses() {
        Course c1 = new Course();
        c1.setCode("K001");
        c1.setName("语文");
        c1.setCredit(new BigDecimal("3.0"));
        c1.setWeeklyHours(4);
        c1.setType(0);
        courseRepository.save(c1);

        Course c2 = new Course();
        c2.setCode("K002");
        c2.setName("数学");
        c2.setCredit(new BigDecimal("4.0"));
        c2.setWeeklyHours(5);
        c2.setType(0);
        courseRepository.save(c2);

        Course c3 = new Course();
        c3.setCode("K003");
        c3.setName("英语");
        c3.setCredit(new BigDecimal("2.5"));
        c3.setWeeklyHours(3);
        c3.setType(1);
        courseRepository.save(c3);

        return courseRepository.findAll();
    }

    private List<Classroom> createClassrooms() {
        Classroom r1 = new Classroom();
        r1.setCode("R001");
        r1.setName("教学楼101");
        r1.setCapacity(50);
        r1.setType(0);
        classroomRepository.save(r1);

        Classroom r2 = new Classroom();
        r2.setCode("R002");
        r2.setName("教学楼102");
        r2.setCapacity(60);
        r2.setType(0);
        classroomRepository.save(r2);

        return classroomRepository.findAll();
    }

    private List<TimeSlot> createTimeSlots() {
        for (int day = 1; day <= 5; day++) {
            for (int section = 1; section <= 4; section++) {
                TimeSlot slot = new TimeSlot();
                slot.setWeekDay(day);
                slot.setSection(section);
                timeSlotRepository.save(slot);
            }
        }
        return timeSlotRepository.findAll();
    }

    private void createTeacherCourses() {
        Course chinese = courseRepository.findByCode("K001").orElseThrow();
        Course math = courseRepository.findByCode("K002").orElseThrow();
        Course english = courseRepository.findByCode("K003").orElseThrow();

        Teacher t1 = teacherRepository.findByCode("T001").orElseThrow();
        Teacher t2 = teacherRepository.findByCode("T002").orElseThrow();

        teacherCourseRepository.save(new TeacherCourse(t1.getId(), chinese.getId()));
        teacherCourseRepository.save(new TeacherCourse(t1.getId(), math.getId()));
        teacherCourseRepository.save(new TeacherCourse(t2.getId(), english.getId()));
        teacherCourseRepository.save(new TeacherCourse(t2.getId(), math.getId()));
    }

    @Test
    public void testSchedulingWithClassCourseMapping() {
        // 设置班级-课程对应关系：高一(1)班只上语文和数学，不上英语
        ClassInfo class1 = classInfoRepository.findByCode("C001").orElseThrow();
        Course chinese = courseRepository.findByCode("K001").orElseThrow();
        Course math = courseRepository.findByCode("K002").orElseThrow();

        classCourseRepository.save(new ClassCourse(class1.getId(), chinese.getId(), 4));
        classCourseRepository.save(new ClassCourse(class1.getId(), math.getId(), 5));
        // 注意：没有为高一(1)班设置英语课程

        // 构建班级-课程映射
        Map<Long, List<ClassCourse>> classCourses = new HashMap<>();
        for (ClassInfo ci : classes) {
            List<ClassCourse> coursesForClass = classCourseRepository.findByClassId(ci.getId());
            classCourses.put(ci.getId(), coursesForClass);
        }

        // 构建教师可教课程映射
        Map<Long, Set<Long>> teachable = new HashMap<>();
        for (TeacherCourse tc : teacherCourseRepository.findAll()) {
            teachable.computeIfAbsent(tc.getCourseId(), k -> new HashSet<>()).add(tc.getTeacherId());
        }

        // 创建排课引擎
        SchedulingEngine engine = new SchedulingEngine(teachers, classes, courses, 
                classrooms, timeSlots, teachable, classCourses);

        // 运行排课
        SchedulingResult result = engine.run();

        // 验证排课结果
        assertTrue(result.success(), "排课应该成功");
        
        List<Placement> placements = result.placements();
        assertFalse(placements.isEmpty(), "排课结果不应为空");

        // 验证高一(1)班没有英语课
        List<Placement> class1Placements = placements.stream()
                .filter(p -> p.classId().equals(class1.getId()))
                .toList();

        assertFalse(class1Placements.isEmpty(), "高一(1)班应该有排课");
        
        Course englishCourse = courseRepository.findByCode("K003").orElseThrow();
        boolean hasEnglish = class1Placements.stream()
                .anyMatch(p -> p.courseId().equals(englishCourse.getId()));
        
        assertFalse(hasEnglish, "高一(1)班不应该有英语课");
    }

    @Test
    public void testSchedulingWithCustomWeeklyHours() {
        // 设置班级-课程对应关系，并自定义周学时
        ClassInfo class2 = classInfoRepository.findByCode("C002").orElseThrow();
        Course chinese = courseRepository.findByCode("K001").orElseThrow();
        
        // 课程默认周学时是4，这里自定义为2
        classCourseRepository.save(new ClassCourse(class2.getId(), chinese.getId(), 2));

        // 构建班级-课程映射
        Map<Long, List<ClassCourse>> classCourses = new HashMap<>();
        for (ClassInfo ci : classes) {
            List<ClassCourse> coursesForClass = classCourseRepository.findByClassId(ci.getId());
            classCourses.put(ci.getId(), coursesForClass);
        }

        // 构建教师可教课程映射
        Map<Long, Set<Long>> teachable = new HashMap<>();
        for (TeacherCourse tc : teacherCourseRepository.findAll()) {
            teachable.computeIfAbsent(tc.getCourseId(), k -> new HashSet<>()).add(tc.getTeacherId());
        }

        // 创建排课引擎
        SchedulingEngine engine = new SchedulingEngine(teachers, classes, courses, 
                classrooms, timeSlots, teachable, classCourses);

        // 运行排课
        SchedulingResult result = engine.run();

        // 验证排课结果
        assertTrue(result.success(), "排课应该成功");

        // 验证高一(2)班的语文课只有2节
        List<Placement> class2Placements = result.placements().stream()
                .filter(p -> p.classId().equals(class2.getId()) && p.courseId().equals(chinese.getId()))
                .toList();

        assertEquals(2, class2Placements.size(), "高一(2)班的语文课应该只有2节");
    }

    @Test
    public void testSchedulingWithNoClassCourseMapping() {
        // 不设置任何班级-课程对应关系
        
        // 构建空的班级-课程映射
        Map<Long, List<ClassCourse>> classCourses = new HashMap<>();

        // 构建教师可教课程映射
        Map<Long, Set<Long>> teachable = new HashMap<>();
        for (TeacherCourse tc : teacherCourseRepository.findAll()) {
            teachable.computeIfAbsent(tc.getCourseId(), k -> new HashSet<>()).add(tc.getTeacherId());
        }

        // 创建排课引擎
        SchedulingEngine engine = new SchedulingEngine(teachers, classes, courses, 
                classrooms, timeSlots, teachable, classCourses);

        // 运行排课
        SchedulingResult result = engine.run();

        // 验证排课结果应该为空或成功但没有排课
        assertTrue(result.success(), "排课引擎应该成功运行");
        assertTrue(result.placements().isEmpty(), "没有班级-课程映射时应该没有排课结果");
    }
}