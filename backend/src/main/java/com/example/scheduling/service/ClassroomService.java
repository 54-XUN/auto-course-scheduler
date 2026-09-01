package com.example.scheduling.service;

import com.example.scheduling.entity.Classroom;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.ClassroomRepository;
import com.example.scheduling.repository.ScheduleRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ScheduleRepository scheduleRepository;

    public ClassroomService(ClassroomRepository classroomRepository, ScheduleRepository scheduleRepository) {
        this.classroomRepository = classroomRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public List<Classroom> list() {
        return classroomRepository.findAll().stream()
                .sorted(Comparator.comparing(Classroom::getCode))
                .toList();
    }

    @Transactional
    public Classroom create(Classroom classroom) {
        classroomRepository.findByCode(classroom.getCode()).ifPresent(c -> {
            throw new BusinessException("教室编号已存在：" + classroom.getCode());
        });
        return classroomRepository.save(classroom);
    }

    @Transactional
    public Classroom update(Long id, Classroom req) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "教室不存在"));
        if (classroomRepository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new BusinessException("教室编号已存在：" + req.getCode());
        }
        classroom.setCode(req.getCode());
        classroom.setName(req.getName());
        classroom.setCapacity(req.getCapacity());
        classroom.setType(req.getType());
        return classroomRepository.save(classroom);
    }

    @Transactional
    public void delete(Long id) {
        if (!classroomRepository.existsById(id)) {
            throw new BusinessException(404, "教室不存在");
        }
        if (scheduleRepository.existsByClassroomId(id)) {
            throw new BusinessException("该教室已被排课结果引用，请先清除相关排课");
        }
        classroomRepository.deleteById(id);
    }
}
