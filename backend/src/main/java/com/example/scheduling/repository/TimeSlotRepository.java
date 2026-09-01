package com.example.scheduling.repository;

import com.example.scheduling.entity.TimeSlot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findAllByOrderByWeekDayAscSectionAsc();

    boolean existsByWeekDayAndSection(Integer weekDay, Integer section);
}
