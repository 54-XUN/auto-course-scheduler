package com.example.scheduling.dto;

import lombok.Getter;
import lombok.Setter;

/** 排课结果列表项 */
@Getter
@Setter
public class ScheduleDTO {
    private Long id;
    private Long classId;
    private Long teacherId;
    private Long courseId;
    private Long classroomId;
    private Long timeSlotId;
    private String className;
    private String teacherName;
    private String courseName;
    private String classroomName;
    private Integer weekDay;
    private Integer section;
}
