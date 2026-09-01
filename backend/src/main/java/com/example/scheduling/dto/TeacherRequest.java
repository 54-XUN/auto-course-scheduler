package com.example.scheduling.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 教师新增/修改请求（含可教课程） */
@Getter
@Setter
public class TeacherRequest {

    @NotBlank(message = "教师编号不能为空")
    @Size(max = 32, message = "教师编号长度不能超过32")
    private String code;

    @NotBlank(message = "教师姓名不能为空")
    @Size(max = 64, message = "教师姓名长度不能超过64")
    private String name;

    @Min(0)
    @Max(1)
    private Integer gender;

    @Size(max = 20, message = "电话长度不能超过20")
    private String phone;

    @Size(max = 32, message = "职称长度不能超过32")
    private String title;

    /** 可教课程ID列表 */
    private List<Long> courseIds;
}
