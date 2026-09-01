package com.example.scheduling.service;

import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.ClassCourseRepository;
import com.example.scheduling.repository.ClassInfoRepository;
import com.example.scheduling.repository.ScheduleRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassInfoService {

    private final ClassInfoRepository classInfoRepository;
    private final ScheduleRepository scheduleRepository;
    private final ClassCourseRepository classCourseRepository;

    public ClassInfoService(ClassInfoRepository classInfoRepository,
                            ScheduleRepository scheduleRepository,
                            ClassCourseRepository classCourseRepository) {
        this.classInfoRepository = classInfoRepository;
        this.scheduleRepository = scheduleRepository;
        this.classCourseRepository = classCourseRepository;
    }

    public List<ClassInfo> list() {
        return classInfoRepository.findAll().stream()
                .sorted(Comparator.comparing(ClassInfo::getCode))
                .toList();
    }

    @Transactional
    public ClassInfo create(ClassInfo info) {
        classInfoRepository.findByCode(info.getCode()).ifPresent(c -> {
            throw new BusinessException("班级编号已存在：" + info.getCode());
        });
        return classInfoRepository.save(info);
    }

    @Transactional
    public ClassInfo update(Long id, ClassInfo req) {
        ClassInfo info = classInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "班级不存在"));
        if (classInfoRepository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new BusinessException("班级编号已存在：" + req.getCode());
        }
        info.setCode(req.getCode());
        info.setName(req.getName());
        info.setGrade(req.getGrade());
        info.setStudentCount(req.getStudentCount());
        return classInfoRepository.save(info);
    }

    @Transactional
    public void delete(Long id) {
        if (!classInfoRepository.existsById(id)) {
            throw new BusinessException(404, "班级不存在");
        }
        if (scheduleRepository.existsByClassId(id)) {
            throw new BusinessException("该班级已被排课结果引用，请先清除相关排课");
        }
        classCourseRepository.deleteByClassId(id);
        classInfoRepository.deleteById(id);
    }
}
