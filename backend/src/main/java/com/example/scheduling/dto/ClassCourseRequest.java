package com.example.scheduling.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** 班级课程关联新增/修改请求 */
@Getter
@Setter
public class ClassCourseRequest {

    @NotNull(message = "班级不能为空")
    private Long classId;

    @NotNull(message = "课程不能为空")
    private Long courseId;

    /** 自定义周学时，为空则使用课程默认周学时 */
    @Min(value = 1, message = "周学时至少为1")
    @Max(value = 8, message = "周学时最多为8")
    private Integer weeklyHours;
}
