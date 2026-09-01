package com.example.scheduling.repository;

import com.example.scheduling.entity.ClassCourse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassCourseRepository extends JpaRepository<ClassCourse, Long> {

    /**
     * 查询班级需要上的所有课程
     */
    @Query("SELECT cc FROM ClassCourse cc WHERE cc.classId = :classId")
    List<ClassCourse> findByClassId(@Param("classId") Long classId);

    /**
     * 查询课程被哪些班级需要
     */
    @Query("SELECT cc FROM ClassCourse cc WHERE cc.courseId = :courseId")
    List<ClassCourse> findByCourseId(@Param("courseId") Long courseId);

    /**
     * 查询指定班级的课程关联
     */
    Optional<ClassCourse> findByClassIdAndCourseId(Long classId, Long courseId);

    /**
     * 检查班级是否需要上某课程
     */
    boolean existsByClassIdAndCourseId(Long classId, Long courseId);

    /**
     * 删除指定班级的所有课程关联
     */
    void deleteByClassId(Long classId);

    /**
     * 删除指定课程的所有班级关联
     */
    void deleteByCourseId(Long courseId);
}