package com.example.scheduling.dto;

import lombok.Getter;
import lombok.Setter;

/** 班级课程关联响应（含班级与课程名称） */
@Getter
@Setter
public class ClassCourseDTO {
    private Long id;
    private Long classId;
    private String className;
    private Long courseId;
    private String courseName;
    private Integer weeklyHours;
}
