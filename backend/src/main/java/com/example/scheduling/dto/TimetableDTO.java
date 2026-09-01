package com.example.scheduling.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 课表数据（三个视角通用） */
@Getter
@Setter
public class TimetableDTO {

    /** 视角：class / teacher / classroom */
    private String viewType;
    private Long viewId;
    private String viewName;

    /** 非空课表单元格 */
    private List<CellDTO> cells = new ArrayList<>();

    @Getter
    @Setter
    public static class CellDTO {
        private Integer weekDay;
        private Integer section;
        private Long scheduleId;
        private Long courseId;
        private Long classId;
        private Long teacherId;
        private Long classroomId;
        private String courseName;
        private String teacherName;
        private String className;
        private String classroomName;
    }
}
