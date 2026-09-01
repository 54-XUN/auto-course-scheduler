package com.example.scheduling.repository;

import com.example.scheduling.entity.Teacher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
