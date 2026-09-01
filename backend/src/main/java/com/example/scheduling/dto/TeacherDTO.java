package com.example.scheduling.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 教师响应（含可教课程） */
@Getter
@Setter
public class TeacherDTO {
    private Long id;
    private String code;
    private String name;
    private Integer gender;
    private String phone;
    private String title;
    private List<Long> courseIds;
    private List<String> courseNames;
}
