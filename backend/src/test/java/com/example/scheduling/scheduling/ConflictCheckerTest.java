package com.example.scheduling.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.entity.Classroom;
import com.example.scheduling.entity.TimeSlot;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** 冲突检测规则单元测试（覆盖 TECH.md 12.2 关键测试用例） */
class ConflictCheckerTest {

    private static final long SLOT_MON_1 = 1L;
    private static final long SLOT_MON_2 = 2L;
    private static final long SLOT_TUE_1 = 6L;

    private ConflictChecker checker;

    @BeforeEach
    void setUp() {
        TimeSlot mon1 = slot(SLOT_MON_1, 1, 1);
        TimeSlot mon2 = slot(SLOT_MON_2, 1, 2);
        TimeSlot tue1 = slot(SLOT_TUE_1, 2, 1);

        ClassInfo smallClass = classInfo(100L, 30);
        Classroom room50 = classroom(200L, 50);

        checker = new ConflictChecker(
                List.of(mon1, mon2, tue1),
                List.of(smallClass),
                List.of(room50),
                Map.of(300L, Set.of(400L))); // 课程300只能由教师400教
    }

    @Test
    void teacherConflict_sameSlotSameTeacher_rejected() {
        Placement p = new Placement(100L, 400L, 300L, 200L, SLOT_MON_1);
        checker.place(p);
        assertFalse(checker.teacherFree(SLOT_MON_1, 400L));
        assertTrue(checker.teacherFree(SLOT_MON_2, 400L));
    }

    @Test
    void classConflict_sameSlotSameClass_rejected() {
        Placement p = new Placement(100L, 400L, 300L, 200L, SLOT_MON_1);
        checker.place(p);
        assertFalse(checker.classFree(SLOT_MON_1, 100L));
        assertTrue(checker.classFree(SLOT_MON_2, 100L));
    }

    @Test
    void classroomConflict_sameSlotSameRoom_rejected() {
        Placement p = new Placement(100L, 400L, 300L, 200L, SLOT_MON_1);
        checker.place(p);
        assertFalse(checker.classroomFree(SLOT_MON_1, 200L));
        assertTrue(checker.classroomFree(SLOT_MON_2, 200L));
    }

    @Test
    void capacity_classCountExceedsRoom_rejected() {
        Classroom smallRoom = classroom(201L, 20);
        ConflictChecker strict = new ConflictChecker(
                List.of(slot(SLOT_MON_1, 1, 1)),
                List.of(classInfo(100L, 30)),
                List.of(smallRoom),
                Map.of());
        assertFalse(strict.capacityOk(100L, 201L));
    }

    @Test
    void capacity_classCountFitsRoom_accepted() {
        assertTrue(checker.capacityOk(100L, 200L));
    }

    @Test
    void canTeach_teacherNotInList_rejected() {
        assertFalse(checker.canTeach(999L, 300L));
        assertTrue(checker.canTeach(400L, 300L));
    }

    @Test
    void unplace_restoresAvailability() {
        Placement p = new Placement(100L, 400L, 300L, 200L, SLOT_MON_1);
        checker.place(p);
        checker.unplace(p);
        assertTrue(checker.isPlaceable(100L, 400L, 300L, 200L, SLOT_MON_1));
    }

    @Test
    void continuityScore_adjacentSlot_scoresHigherThanOtherDay() {
        Placement p = new Placement(100L, 400L, 300L, 200L, SLOT_MON_1);
        checker.place(p);
        TimeSlot mon2 = slot(SLOT_MON_2, 1, 2);
        TimeSlot tue1 = slot(SLOT_TUE_1, 2, 1);
        assertTrue(checker.continuityScore(100L, 300L, mon2) > checker.continuityScore(100L, 300L, tue1));
    }

    private TimeSlot slot(long id, int day, int section) {
        TimeSlot s = new TimeSlot();
        s.setId(id);
        s.setWeekDay(day);
        s.setSection(section);
        return s;
    }

    private ClassInfo classInfo(long id, int count) {
        ClassInfo c = new ClassInfo();
        c.setId(id);
        c.setCode("C" + id);
        c.setName("班级" + id);
        c.setStudentCount(count);
        return c;
    }

    private Classroom classroom(long id, int capacity) {
        Classroom r = new Classroom();
        r.setId(id);
        r.setCode("R" + id);
        r.setName("教室" + id);
        r.setCapacity(capacity);
        return r;
    }
}
