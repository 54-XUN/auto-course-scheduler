package com.example.scheduling.repository;

import com.example.scheduling.entity.Schedule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByClassId(Long classId);

    List<Schedule> findByTeacherId(Long teacherId);

    List<Schedule> findByClassroomId(Long classroomId);

    List<Schedule> findByTimeSlotId(Long timeSlotId);

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

    /**
     * 原子交换两条排课记录的时间段与教室，避免双 UPDATE 过程中唯一约束出现中间冲突。
     */
    @Modifying(clearAutomatically = true)
    @Query(value = """
        UPDATE schedule
        SET time_slot_id = CASE id
            WHEN :id1 THEN :slot1
            WHEN :id2 THEN :slot2
        END,
        classroom_id = CASE id
            WHEN :id1 THEN :room1
            WHEN :id2 THEN :room2
        END
        WHERE id IN (:id1, :id2)
        """, nativeQuery = true)
    void swapPositions(@Param("id1") Long id1,
                       @Param("slot1") Long slot1,
                       @Param("room1") Long room1,
                       @Param("id2") Long id2,
                       @Param("slot2") Long slot2,
                       @Param("room2") Long room2);
}
