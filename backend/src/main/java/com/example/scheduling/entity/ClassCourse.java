package com.example.scheduling.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 班级课程关联：定义每个班级需要上的课程 */
@Entity
@Table(name = "class_course", uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(columnNames = {"classId", "courseId"})
})
@Getter
@Setter
@NoArgsConstructor
public class ClassCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long classId;

    @NotNull
    private Long courseId;

    /** 该班级该课程的周学时（可覆盖课程默认值） */
    private Integer weeklyHours;

    public ClassCourse(Long classId, Long courseId, Integer weeklyHours) {
        this.classId = classId;
        this.courseId = courseId;
        this.weeklyHours = weeklyHours;
    }

    public ClassCourse(Long classId, Long courseId) {
        this.classId = classId;
        this.courseId = courseId;
        this.weeklyHours = null; // 使用课程默认周学时
    }
}