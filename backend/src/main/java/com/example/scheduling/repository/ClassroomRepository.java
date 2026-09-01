package com.example.scheduling.repository;

import com.example.scheduling.entity.Classroom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
