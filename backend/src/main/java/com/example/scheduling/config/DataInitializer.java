package com.example.scheduling.config;

import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.entity.Classroom;
import com.example.scheduling.entity.Course;
import com.example.scheduling.entity.Teacher;
import com.example.scheduling.entity.TeacherCourse;
import com.example.scheduling.entity.TimeSlot;
import com.example.scheduling.entity.User;
import com.example.scheduling.repository.ClassInfoRepository;
import com.example.scheduling.repository.ClassroomRepository;
import com.example.scheduling.repository.CourseRepository;
import com.example.scheduling.repository.TeacherCourseRepository;
import com.example.scheduling.repository.TeacherRepository;
import com.example.scheduling.repository.TimeSlotRepository;
import com.example.scheduling.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预置数据初始化（幂等：仅在对应表为空时插入）。
 * 排课结果表 schedule 不预置数据，由排课算法产生。
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TeacherRepository teacherRepository;
    private final ClassInfoRepository classInfoRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final TeacherCourseRepository teacherCourseRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(UserRepository userRepository,
                           TimeSlotRepository timeSlotRepository,
                           TeacherRepository teacherRepository,
                           ClassInfoRepository classInfoRepository,
                           CourseRepository courseRepository,
                           ClassroomRepository classroomRepository,
                           TeacherCourseRepository teacherCourseRepository) {
        this.userRepository = userRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.teacherRepository = teacherRepository;
        this.classInfoRepository = classInfoRepository;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.teacherCourseRepository = teacherCourseRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initAdmin();
        initTimeSlots();
        initTeachers();
        initClasses();
        initCourses();
        initClassrooms();
        initTeachable();
    }

    private void initAdmin() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("123456"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }

    private void initTimeSlots() {
        if (timeSlotRepository.count() > 0) {
            return;
        }
        for (int day = 1; day <= 5; day++) {
            for (int section = 1; section <= 8; section++) {
                TimeSlot slot = new TimeSlot();
                slot.setWeekDay(day);
                slot.setSection(section);
                timeSlotRepository.save(slot);
            }
        }
    }

    private void initTeachers() {
        if (teacherRepository.count() > 0) {
            return;
        }
        saveTeacher("T001", "张伟", 1, "13800000001", "教授");
        saveTeacher("T002", "李娜", 0, "13800000002", "副教授");
        saveTeacher("T003", "王强", 1, "13800000003", "讲师");
        saveTeacher("T004", "赵敏", 0, "13800000004", "副教授");
        saveTeacher("T005", "刘洋", 1, "13800000005", "讲师");
    }

    private void saveTeacher(String code, String name, int gender, String phone, String title) {
        Teacher t = new Teacher();
        t.setCode(code);
        t.setName(name);
        t.setGender(gender);
        t.setPhone(phone);
        t.setTitle(title);
        teacherRepository.save(t);
    }

    private void initClasses() {
        if (classInfoRepository.count() > 0) {
            return;
        }
        saveClass("C001", "高一(1)班", "2025级", 40);
        saveClass("C002", "高一(2)班", "2025级", 45);
        saveClass("C003", "高二(1)班", "2024级", 42);
        saveClass("C004", "高二(2)班", "2024级", 38);
    }

    private void saveClass(String code, String name, String grade, int count) {
        ClassInfo c = new ClassInfo();
        c.setCode(code);
        c.setName(name);
        c.setGrade(grade);
        c.setStudentCount(count);
        classInfoRepository.save(c);
    }

    private void initCourses() {
        if (courseRepository.count() > 0) {
            return;
        }
        saveCourse("K001", "语文", "3.0", 4, 0);
        saveCourse("K002", "数学", "3.0", 4, 0);
        saveCourse("K003", "英语", "2.5", 3, 0);
        saveCourse("K004", "物理", "2.5", 3, 0);
        saveCourse("K005", "化学", "2.0", 2, 0);
        saveCourse("K006", "音乐鉴赏", "1.0", 2, 1);
    }

    private void saveCourse(String code, String name, String credit, int weeklyHours, int type) {
        Course c = new Course();
        c.setCode(code);
        c.setName(name);
        c.setCredit(new BigDecimal(credit));
        c.setWeeklyHours(weeklyHours);
        c.setType(type);
        courseRepository.save(c);
    }

    private void initClassrooms() {
        if (classroomRepository.count() > 0) {
            return;
        }
        saveClassroom("R001", "教学楼101", 50, 0);
        saveClassroom("R002", "教学楼102", 50, 0);
        saveClassroom("R003", "教学楼201", 60, 0);
        saveClassroom("R004", "教学楼202", 60, 0);
        saveClassroom("R005", "实验楼实验室", 45, 1);
        saveClassroom("R006", "多媒体报告厅", 60, 2);
    }

    private void saveClassroom(String code, String name, int capacity, int type) {
        Classroom r = new Classroom();
        r.setCode(code);
        r.setName(name);
        r.setCapacity(capacity);
        r.setType(type);
        classroomRepository.save(r);
    }

    /**
     * 可教课程关联：
     * - 语文/数学仅 1 名教师可教（紧约束，验证算法优先级排序）
     * - 英语 2 名教师，物理/化学 1 名（实验室课），音乐 1 名（多媒体）
     */
    private void initTeachable() {
        if (teacherCourseRepository.count() > 0) {
            return;
        }
        Teacher zhang = teacherRepository.findByCode("T001").orElseThrow();
        Teacher li = teacherRepository.findByCode("T002").orElseThrow();
        Teacher wang = teacherRepository.findByCode("T003").orElseThrow();
        Teacher zhao = teacherRepository.findByCode("T004").orElseThrow();
        Teacher liu = teacherRepository.findByCode("T005").orElseThrow();

        Course chinese = courseRepository.findByCode("K001").orElseThrow();
        Course math = courseRepository.findByCode("K002").orElseThrow();
        Course english = courseRepository.findByCode("K003").orElseThrow();
        Course physics = courseRepository.findByCode("K004").orElseThrow();
        Course chemistry = courseRepository.findByCode("K005").orElseThrow();
        Course music = courseRepository.findByCode("K006").orElseThrow();

        List<TeacherCourse> associations = List.of(
                new TeacherCourse(zhang.getId(), chinese.getId()),
                new TeacherCourse(li.getId(), math.getId()),
                new TeacherCourse(wang.getId(), english.getId()),
                new TeacherCourse(liu.getId(), english.getId()),
                new TeacherCourse(zhao.getId(), physics.getId()),
                new TeacherCourse(zhao.getId(), chemistry.getId()),
                new TeacherCourse(liu.getId(), music.getId()));
        teacherCourseRepository.saveAll(associations);
    }
}
