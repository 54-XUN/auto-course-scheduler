package com.example.scheduling.repository;

import com.example.scheduling.entity.TeacherCourse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherCourseRepository extends JpaRepository<TeacherCourse, Long> {

    List<TeacherCourse> findByTeacherId(Long teacherId);

    List<TeacherCourse> findByCourseId(Long courseId);

    void deleteByTeacherId(Long teacherId);

    void deleteByCourseId(Long courseId);

    boolean existsByTeacherIdAndCourseId(Long teacherId, Long courseId);
}
