package com.example.scheduling.scheduling;

/** 一条排课结果 */
public record Placement(Long classId, Long teacherId, Long courseId, Long classroomId, Long timeSlotId) {
}
