package com.example.scheduling.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.entity.Classroom;
import com.example.scheduling.entity.Course;
import com.example.scheduling.entity.Teacher;
import com.example.scheduling.entity.TimeSlot;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** 排课算法单元测试（贪心+回溯、周学时守恒、连排优先、无解场景） */
class SchedulingEngineTest {

    @Test
    void feasibleInstance_allConstraintsSatisfied() {
        // 2 名教师 × 2 门课 × 1 个班级 × 3 间教室 × 40 时间段
        List<Teacher> teachers = List.of(teacher(1L, "T1"), teacher(2L, "T2"));
        List<ClassInfo> classes = List.of(classInfo(10L, 30));
        List<Course> courses = List.of(course(100L, "数学", 4), course(101L, "英语", 2));
        List<Classroom> rooms = List.of(room(200L, 40), room(201L, 40), room(202L, 40));
        List<TimeSlot> slots = buildSlots(5, 8);
        Map<Long, Set<Long>> teachable = new HashMap<>();
        teachable.put(100L, Set.of(1L));
        teachable.put(101L, Set.of(2L));

        SchedulingResult result = new SchedulingEngine(teachers, classes, courses, rooms, slots, teachable).run();

        assertTrue(result.success(), "应能找到可行解: " + result.message());
        assertEquals(6, result.placements().size(), "周学时守恒：4 + 2 = 6 节");

        // 无重复 (班级,时间段)
        Set<String> classSlots = new HashSet<>();
        Set<String> teacherSlots = new HashSet<>();
        Set<String> roomSlots = new HashSet<>();
        for (Placement p : result.placements()) {
            assertTrue(classSlots.add(p.classId() + "@" + p.timeSlotId()), "班级时间段不可重复");
            assertTrue(teacherSlots.add(p.teacherId() + "@" + p.timeSlotId()), "教师时间段不可重复");
            assertTrue(roomSlots.add(p.classroomId() + "@" + p.timeSlotId()), "教室时间段不可重复");
            assertTrue(teachable.get(p.courseId()).contains(p.teacherId()), "教师必须可教该课程");
        }

        // 容量约束
        assertTrue(rooms.stream().allMatch(r -> r.getCapacity() >= 30));

        // 连排优先：数学 4 节应至少存在一组同日相邻
        Set<Long> mathSlots = new HashSet<>();
        result.placements().stream()
                .filter(p -> p.courseId().equals(100L))
                .forEach(p -> mathSlots.add(p.timeSlotId()));
        assertTrue(hasAdjacentSameDay(mathSlots, slots), "连排约束：数学应存在同日相邻节次");
    }

    @Test
    void multiClass_teachersNotDoubleBooked() {
        List<Teacher> teachers = List.of(teacher(1L, "T1"));
        List<ClassInfo> classes = List.of(classInfo(10L, 30), classInfo(11L, 30));
        List<Course> courses = List.of(course(100L, "数学", 4));
        List<Classroom> rooms = List.of(room(200L, 40), room(201L, 40));
        List<TimeSlot> slots = buildSlots(5, 8);
        Map<Long, Set<Long>> teachable = Map.of(100L, Set.of(1L));

        SchedulingResult result = new SchedulingEngine(teachers, classes, courses, rooms, slots, teachable).run();

        assertTrue(result.success());
        assertEquals(8, result.placements().size());
        Set<String> teacherSlots = new HashSet<>();
        result.placements().forEach(p ->
                assertTrue(teacherSlots.add(p.teacherId() + "@" + p.timeSlotId()), "同一教师不能同时上两节课"));
    }

    @Test
    void infeasibleInstance_returnsFailure() {
        // 班级 50 人，仅 1 间容量 30 的教室 → 容量约束无解
        List<Teacher> teachers = List.of(teacher(1L, "T1"));
        List<ClassInfo> classes = List.of(classInfo(10L, 50));
        List<Course> courses = List.of(course(100L, "数学", 2));
        List<Classroom> rooms = List.of(room(200L, 30));
        List<TimeSlot> slots = buildSlots(5, 8);
        Map<Long, Set<Long>> teachable = Map.of(100L, Set.of(1L));

        SchedulingResult result = new SchedulingEngine(teachers, classes, courses, rooms, slots, teachable).run();

        assertFalse(result.success(), "资源不足应返回无解");
        assertTrue(result.placements().isEmpty());
    }

    @Test
    void tightInstance_backtrackingFindsSolution() {
        // 单教师 8 学时课程（每天最多排 1 节同课程不限制，但每天 8 节可用），2 个班级 4 学时 → 回溯需要绕开冲突
        List<Teacher> teachers = List.of(teacher(1L, "T1"));
        List<ClassInfo> classes = List.of(classInfo(10L, 30), classInfo(11L, 30));
        List<Course> courses = List.of(course(100L, "数学", 4));
        List<Classroom> rooms = List.of(room(200L, 40));
        List<TimeSlot> slots = buildSlots(2, 4); // 仅 8 个时间段
        Map<Long, Set<Long>> teachable = Map.of(100L, Set.of(1L));

        SchedulingResult result = new SchedulingEngine(teachers, classes, courses, rooms, slots, teachable).run();

        assertTrue(result.success(), "8 个时段排 8 节课应可行: " + result.message());
        assertEquals(8, result.placements().size());
    }

    private boolean hasAdjacentSameDay(Set<Long> slotIds, List<TimeSlot> slots) {
        Map<Long, TimeSlot> byId = new HashMap<>();
        slots.forEach(s -> byId.put(s.getId(), s));
        for (Long id : slotIds) {
            TimeSlot s = byId.get(id);
            TimeSlot next = byId.values().stream()
                    .filter(o -> o.getWeekDay().equals(s.getWeekDay())
                            && o.getSection() == s.getSection() + 1)
                    .findFirst().orElse(null);
            if (next != null && slotIds.contains(next.getId())) {
                return true;
            }
        }
        return false;
    }

    private List<TimeSlot> buildSlots(int days, int sectionsPerDay) {
        List<TimeSlot> slots = new ArrayList<>();
        long id = 1;
        for (int d = 1; d <= days; d++) {
            for (int s = 1; s <= sectionsPerDay; s++) {
                TimeSlot ts = new TimeSlot();
                ts.setId(id++);
                ts.setWeekDay(d);
                ts.setSection(s);
                slots.add(ts);
            }
        }
        return slots;
    }

    private Teacher teacher(long id, String code) {
        Teacher t = new Teacher();
        t.setId(id);
        t.setCode(code);
        t.setName("教师" + code);
        return t;
    }

    private ClassInfo classInfo(long id, int count) {
        ClassInfo c = new ClassInfo();
        c.setId(id);
        c.setCode("C" + id);
        c.setName("班级" + id);
        c.setStudentCount(count);
        return c;
    }

    private Course course(long id, String name, int weeklyHours) {
        Course c = new Course();
        c.setId(id);
        c.setCode("K" + id);
        c.setName(name);
        c.setCredit(new BigDecimal("2.0"));
        c.setWeeklyHours(weeklyHours);
        c.setType(0);
        return c;
    }

    private Classroom room(long id, int capacity) {
        Classroom r = new Classroom();
        r.setId(id);
        r.setCode("R" + id);
        r.setName("教室" + id);
        r.setCapacity(capacity);
        r.setType(0);
        return r;
    }
}
