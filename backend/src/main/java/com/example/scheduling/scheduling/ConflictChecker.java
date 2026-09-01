package com.example.scheduling.scheduling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 冲突检测器：维护"时间段 → 占用集合"的排课状态，提供约束校验与放置/回退。
 *
 * <p>规则：
 * <ul>
 *   <li>教师不冲突：同一教师同一时间只能上一节课</li>
 *   <li>班级不冲突：同一班级同一时间只能上一节课</li>
 *   <li>教室不冲突：同一教室同一时间只能被一个班使用</li>
 *   <li>教室容量：班级人数 ≤ 教室容量</li>
 *   <li>教师可教课程：教师必须在课程可教列表中</li>
 * </ul>
 */
public class ConflictChecker {

    /** slotId -> 占用的 teacherId 集合 */
    private final Map<Long, Set<Long>> slotTeachers = new HashMap<>();
    /** slotId -> 占用的 classId 集合 */
    private final Map<Long, Set<Long>> slotClasses = new HashMap<>();
    /** slotId -> 占用的 classroomId 集合 */
    private final Map<Long, Set<Long>> slotClassrooms = new HashMap<>();
    /** teacherId -> 已排课时数 */
    private final Map<Long, Integer> teacherLoad = new HashMap<>();
    /** "classId:courseId" -> 已排的 timeSlotId 集合（用于连排评分） */
    private final Map<String, Set<Long>> pairSlots = new HashMap<>();

    private final Map<Long, com.example.scheduling.entity.TimeSlot> slotById;
    private final Map<Long, com.example.scheduling.entity.ClassInfo> classById;
    private final Map<Long, com.example.scheduling.entity.Classroom> classroomById;
    private final Map<Long, Set<Long>> teachable;

    public ConflictChecker(List<com.example.scheduling.entity.TimeSlot> slots,
                           List<com.example.scheduling.entity.ClassInfo> classes,
                           List<com.example.scheduling.entity.Classroom> classrooms,
                           Map<Long, Set<Long>> teachable) {
        this.slotById = new HashMap<>();
        slots.forEach(s -> slotById.put(s.getId(), s));
        this.classById = new HashMap<>();
        classes.forEach(c -> classById.put(c.getId(), c));
        this.classroomById = new HashMap<>();
        classrooms.forEach(c -> classroomById.put(c.getId(), c));
        this.teachable = teachable;
    }

    // ---------- 硬约束检测 ----------

    public boolean teacherFree(Long timeSlotId, Long teacherId) {
        return !slotTeachers.getOrDefault(timeSlotId, Set.of()).contains(teacherId);
    }

    public boolean classFree(Long timeSlotId, Long classId) {
        return !slotClasses.getOrDefault(timeSlotId, Set.of()).contains(classId);
    }

    public boolean classroomFree(Long timeSlotId, Long classroomId) {
        return !slotClassrooms.getOrDefault(timeSlotId, Set.of()).contains(classroomId);
    }

    public boolean capacityOk(Long classId, Long classroomId) {
        com.example.scheduling.entity.ClassInfo ci = classById.get(classId);
        com.example.scheduling.entity.Classroom cr = classroomById.get(classroomId);
        return ci != null && cr != null && ci.getStudentCount() <= cr.getCapacity();
    }

    public boolean canTeach(Long teacherId, Long courseId) {
        return teachable.getOrDefault(courseId, Set.of()).contains(teacherId);
    }

    /** 综合校验：某 (教师, 班级, 教室, 时间段) 组合是否可放置 */
    public boolean isPlaceable(Long classId, Long teacherId, Long courseId, Long classroomId, Long timeSlotId) {
        return teacherFree(timeSlotId, teacherId)
                && classFree(timeSlotId, classId)
                && classroomFree(timeSlotId, classroomId)
                && capacityOk(classId, classroomId)
                && canTeach(teacherId, courseId);
    }

    // ---------- 状态维护 ----------

    public void place(Placement p) {
        slotTeachers.computeIfAbsent(p.timeSlotId(), k -> new HashSet<>()).add(p.teacherId());
        slotClasses.computeIfAbsent(p.timeSlotId(), k -> new HashSet<>()).add(p.classId());
        slotClassrooms.computeIfAbsent(p.timeSlotId(), k -> new HashSet<>()).add(p.classroomId());
        teacherLoad.merge(p.teacherId(), 1, Integer::sum);
        pairKey(p.classId(), p.courseId()).add(p.timeSlotId());
    }

    public void unplace(Placement p) {
        Set<Long> ts = slotTeachers.get(p.timeSlotId());
        if (ts != null) {
            ts.remove(p.teacherId());
        }
        Set<Long> cs = slotClasses.get(p.timeSlotId());
        if (cs != null) {
            cs.remove(p.classId());
        }
        Set<Long> rs = slotClassrooms.get(p.timeSlotId());
        if (rs != null) {
            rs.remove(p.classroomId());
        }
        teacherLoad.merge(p.teacherId(), -1, Integer::sum);
        pairKey(p.classId(), p.courseId()).remove(p.timeSlotId());
    }

    public int getTeacherLoad(Long teacherId) {
        return teacherLoad.getOrDefault(teacherId, 0);
    }

    /** 连排得分：与已排的同(班级,课程)节次相邻/同日优先 */
    public int continuityScore(Long classId, Long courseId, com.example.scheduling.entity.TimeSlot slot) {
        Set<Long> placed = pairSlots.getOrDefault(pairKeyStr(classId, courseId), Set.of());
        if (placed.isEmpty()) {
            return 0;
        }
        int score = 0;
        Long prev = findSlotId(slot.getWeekDay(), slot.getSection() - 1);
        Long next = findSlotId(slot.getWeekDay(), slot.getSection() + 1);
        if (prev != null && placed.contains(prev)) {
            score += 100;
        }
        if (next != null && placed.contains(next)) {
            score += 100;
        }
        boolean sameDay = placed.stream().anyMatch(id -> {
            com.example.scheduling.entity.TimeSlot s = slotById.get(id);
            return s != null && s.getWeekDay().equals(slot.getWeekDay());
        });
        if (sameDay) {
            score += 20;
        }
        return score;
    }

    /** 已排的指定 (班级, 课程) 的全部时间段 */
    public Set<Long> placedSlots(Long classId, Long courseId) {
        return new HashSet<>(pairSlots.getOrDefault(pairKeyStr(classId, courseId), Set.of()));
    }

    private Set<Long> pairKey(Long classId, Long courseId) {
        return pairSlots.computeIfAbsent(pairKeyStr(classId, courseId), k -> new HashSet<>());
    }

    private static String pairKeyStr(Long classId, Long courseId) {
        return classId + ":" + courseId;
    }

    private Long findSlotId(int weekDay, int section) {
        return slotById.values().stream()
                .filter(s -> s.getWeekDay() == weekDay && s.getSection() == section)
                .map(com.example.scheduling.entity.TimeSlot::getId)
                .findFirst()
                .orElse(null);
    }
}
