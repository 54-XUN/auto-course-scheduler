package com.example.scheduling.repository;

import com.example.scheduling.entity.Course;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
