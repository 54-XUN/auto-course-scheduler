package com.example.scheduling.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 排课结果：某班级的某门课，由某教师，在某教室、某时间段上课 */
@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long classId;

    private Long teacherId;

    private Long courseId;

    private Long classroomId;

    private Long timeSlotId;

    /** 第几周 */
    private Integer weekNumber;

    private LocalDateTime createdAt;
}
