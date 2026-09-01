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

/** 教师可教课程关联 */
@Entity
@Table(name = "teacher_course", uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(columnNames = {"teacherId", "courseId"})
})
@Getter
@Setter
@NoArgsConstructor
public class TeacherCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long teacherId;

    @NotNull
    private Long courseId;

    public TeacherCourse(Long teacherId, Long courseId) {
        this.teacherId = teacherId;
        this.courseId = courseId;
    }
}
