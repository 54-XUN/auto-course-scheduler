package com.example.scheduling.repository;

import com.example.scheduling.entity.ClassInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {
    Optional<ClassInfo> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
