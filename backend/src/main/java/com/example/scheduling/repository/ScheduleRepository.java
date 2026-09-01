package com.example.scheduling.repository;

import com.example.scheduling.entity.Schedule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByClassId(Long classId);

    List<Schedule> findByTeacherId(Long teacherId);

    List<Schedule> findByClassroomId(Long classroomId);

    boolean existsByTeacherId(Long teacherId);

    boolean existsByClassId(Long classId);

    boolean existsByCourseId(Long courseId);

    boolean existsByClassroomId(Long classroomId);

    boolean existsByTimeSlotId(Long timeSlotId);

    boolean existsByTimeSlotIdAndTeacherIdAndIdNot(Long timeSlotId, Long teacherId, Long id);

    Optional<Schedule> findByTimeSlotIdAndTeacherIdAndIdNot(Long timeSlotId, Long teacherId, Long id);

    boolean existsByTimeSlotIdAndClassIdAndIdNot(Long timeSlotId, Long classId, Long id);

    Optional<Schedule> findByTimeSlotIdAndClassIdAndIdNot(Long timeSlotId, Long classId, Long id);

    boolean existsByTimeSlotIdAndClassroomIdAndIdNot(Long timeSlotId, Long classroomId, Long id);

    Optional<Schedule> findByTimeSlotIdAndClassroomIdAndIdNot(Long timeSlotId, Long classroomId, Long id);
}
