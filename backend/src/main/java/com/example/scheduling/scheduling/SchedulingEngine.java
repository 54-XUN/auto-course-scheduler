package com.example.scheduling.scheduling;

import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.entity.Classroom;
import com.example.scheduling.entity.Course;
import com.example.scheduling.entity.Teacher;
import com.example.scheduling.entity.TimeSlot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自动排课引擎：贪心 + 回溯。
 *
 * <p>流程：
 * <ol>
 *   <li>按约束优先级生成待排任务：可选教师少、可选教室少、周学时高的优先</li>
 *   <li>对每个任务生成候选 (时间段, 教师, 教室)：按连排得分与教师负载排序</li>
 *   <li>贪心放置，失败则回溯尝试其他候选</li>
 *   <li>探索步数超过预算视为无解，避免极端情况长时间计算</li>
 * </ol>
 */
public class SchedulingEngine {

    /** 单个任务（某班级某课程的一节课） */
    private record Task(Long classId, Long courseId) {
    }

    /** 候选位置 */
    private record Candidate(Long teacherId, Long classroomId, TimeSlot slot) {
    }

    /** 探索预算：防止病态实例下无限回溯 */
    private static final long SEARCH_BUDGET = 500_000L;

    private final List<ClassInfo> classes;
    private final List<Course> courses;
    private final List<Classroom> classrooms;
    private final List<TimeSlot> timeSlots;
    private final List<Teacher> teachers;
    /** courseId -> 可教该课程的教师ID集合 */
    private final Map<Long, Set<Long>> teachable;

    private final ConflictChecker checker;
    private final List<Placement> placements = new ArrayList<>();
    private long budget;

    public SchedulingEngine(List<Teacher> teachers,
                            List<ClassInfo> classes,
                            List<Course> courses,
                            List<Classroom> classrooms,
                            List<TimeSlot> timeSlots,
                            Map<Long, Set<Long>> teachable) {
        this.teachers = teachers;
        this.classes = classes;
        this.courses = courses;
        this.classrooms = classrooms;
        this.timeSlots = timeSlots;
        this.teachable = teachable;
        this.checker = new ConflictChecker(timeSlots, classes, classrooms, teachable);
    }

    public SchedulingResult run() {
        budget = SEARCH_BUDGET;
        placements.clear();
        List<Task> tasks = buildTasks();
        boolean ok = backtrack(0, tasks);
        if (ok) {
            return new SchedulingResult(true, List.copyOf(placements), "排课成功");
        }
        return new SchedulingResult(false, List.of(), "无可行解：请在检查教师可教课程、教室容量与时间资源后重试");
    }

    /** 任务排序：可选教师少 > 可选教室少 > 周学时高 */
    private List<Task> buildTasks() {
        List<Task> tasks = new ArrayList<>();
        for (ClassInfo ci : classes) {
            for (Course c : courses) {
                for (int i = 0; i < c.getWeeklyHours(); i++) {
                    tasks.add(new Task(ci.getId(), c.getId()));
                }
            }
        }
        Map<Long, Integer> roomCountByClass = new HashMap<>();
        for (ClassInfo ci : classes) {
            int n = (int) classrooms.stream()
                    .filter(r -> r.getCapacity() >= ci.getStudentCount())
                    .count();
            roomCountByClass.put(ci.getId(), n);
        }
        tasks.sort(Comparator
                .comparingInt((Task t) -> teachable.getOrDefault(t.courseId(), Set.of()).size())
                .thenComparingInt(t -> roomCountByClass.getOrDefault(t.classId(), 0))
                .thenComparing(Comparator.comparingInt((Task t) ->
                        courses.stream()
                                .filter(c -> c.getId().equals(t.courseId()))
                                .findFirst()
                                .map(Course::getWeeklyHours)
                                .orElse(0)).reversed())
                .thenComparing(Task::classId)
                .thenComparing(Task::courseId));
        return tasks;
    }

    private boolean backtrack(int index, List<Task> tasks) {
        if (index == tasks.size()) {
            return true;
        }
        Task task = tasks.get(index);
        List<Candidate> candidates = generateCandidates(task);
        for (Candidate cand : candidates) {
            if (budget-- <= 0) {
                return false;
            }
            Placement p = new Placement(task.classId(), cand.teacherId(), task.courseId(),
                    cand.classroomId(), cand.slot().getId());
            if (!checker.isPlaceable(p.classId(), p.teacherId(), p.courseId(), p.classroomId(), p.timeSlotId())) {
                continue;
            }
            checker.place(p);
            placements.add(p);
            if (backtrack(index + 1, tasks)) {
                return true;
            }
            placements.remove(placements.size() - 1);
            checker.unplace(p);
        }
        return false;
    }

    /**
     * 候选排序：时间段按连排得分降序优先，教师按负载升序（均衡），教室按容量升序（贴合）。
     */
    private List<Candidate> generateCandidates(Task task) {
        List<Teacher> canTeach = teachers.stream()
                .filter(t -> teachable.getOrDefault(task.courseId(), Set.of()).contains(t.getId()))
                .sorted(Comparator.comparingInt((Teacher t) -> checker.getTeacherLoad(t.getId()))
                        .thenComparing(Teacher::getId))
                .toList();

        ClassInfo ci = classes.stream().filter(c -> c.getId().equals(task.classId())).findFirst().orElseThrow();
        List<Classroom> fitRooms = classrooms.stream()
                .filter(r -> r.getCapacity() >= ci.getStudentCount())
                .sorted(Comparator.comparingInt(Classroom::getCapacity).thenComparing(Classroom::getId))
                .toList();
        if (canTeach.isEmpty() || fitRooms.isEmpty()) {
            return List.of();
        }

        List<TimeSlot> sortedSlots = new ArrayList<>(timeSlots);
        final Long classId = task.classId();
        final Long courseId = task.courseId();
        sortedSlots.sort(Comparator
                .comparingInt((TimeSlot s) ->
                        -checker.continuityScore(classId, courseId, s) * 1000
                                - (8 - s.getWeekDay()) * 10
                                - (9 - s.getSection()))
                .thenComparing(TimeSlot::getWeekDay)
                .thenComparing(TimeSlot::getSection));

        List<Candidate> candidates = new ArrayList<>();
        for (TimeSlot slot : sortedSlots) {
            for (Teacher teacher : canTeach) {
                for (Classroom room : fitRooms) {
                    candidates.add(new Candidate(teacher.getId(), room.getId(), slot));
                }
            }
        }
        return candidates;
    }
}
