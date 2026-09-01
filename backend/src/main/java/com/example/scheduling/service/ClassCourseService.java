package com.example.scheduling.service;

import com.example.scheduling.dto.ClassCourseDTO;
import com.example.scheduling.dto.ClassCourseRequest;
import com.example.scheduling.entity.ClassCourse;
import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.entity.Course;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.ClassCourseRepository;
import com.example.scheduling.repository.ClassInfoRepository;
import com.example.scheduling.repository.CourseRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassCourseService {

    private final ClassCourseRepository classCourseRepository;
    private final ClassInfoRepository classInfoRepository;
    private final CourseRepository courseRepository;

    public ClassCourseService(ClassCourseRepository classCourseRepository,
                              ClassInfoRepository classInfoRepository,
                              CourseRepository courseRepository) {
        this.classCourseRepository = classCourseRepository;
        this.classInfoRepository = classInfoRepository;
        this.courseRepository = courseRepository;
    }

    public List<ClassCourseDTO> list() {
        List<ClassCourse> list = classCourseRepository.findAll();
        Map<Long, ClassInfo> classMap = classInfoRepository.findAll().stream()
                .collect(Collectors.toMap(ClassInfo::getId, Function.identity()));
        Map<Long, Course> courseMap = courseRepository.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        return list.stream()
                .sorted(Comparator.comparing((ClassCourse cc) -> classMap.getOrDefault(cc.getClassId(), new ClassInfo()).getCode())
                        .thenComparing(cc -> courseMap.getOrDefault(cc.getCourseId(), new Course()).getCode()))
                .map(cc -> toDTO(cc, classMap.get(cc.getClassId()), courseMap.get(cc.getCourseId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassCourseDTO create(ClassCourseRequest req) {
        validate(req);
        if (classCourseRepository.existsByClassIdAndCourseId(req.getClassId(), req.getCourseId())) {
            throw new BusinessException("该班级已配置此课程");
        }
        ClassCourse cc = new ClassCourse(req.getClassId(), req.getCourseId(), req.getWeeklyHours());
        return toDTO(classCourseRepository.save(cc));
    }

    @Transactional
    public ClassCourseDTO update(Long id, ClassCourseRequest req) {
        ClassCourse cc = classCourseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "班级课程关联不存在"));
        validate(req);
        if (!cc.getClassId().equals(req.getClassId()) || !cc.getCourseId().equals(req.getCourseId())) {
            if (classCourseRepository.existsByClassIdAndCourseId(req.getClassId(), req.getCourseId())) {
                throw new BusinessException("该班级已配置此课程");
            }
        }
        cc.setClassId(req.getClassId());
        cc.setCourseId(req.getCourseId());
        cc.setWeeklyHours(req.getWeeklyHours());
        return toDTO(classCourseRepository.save(cc));
    }

    @Transactional
    public void delete(Long id) {
        if (!classCourseRepository.existsById(id)) {
            throw new BusinessException(404, "班级课程关联不存在");
        }
        classCourseRepository.deleteById(id);
    }

    private void validate(ClassCourseRequest req) {
        if (!classInfoRepository.existsById(req.getClassId())) {
            throw new BusinessException("班级不存在");
        }
        if (!courseRepository.existsById(req.getCourseId())) {
            throw new BusinessException("课程不存在");
        }
    }

    private ClassCourseDTO toDTO(ClassCourse cc) {
        ClassInfo ci = classInfoRepository.findById(cc.getClassId()).orElse(null);
        Course c = courseRepository.findById(cc.getCourseId()).orElse(null);
        return toDTO(cc, ci, c);
    }

    private ClassCourseDTO toDTO(ClassCourse cc, ClassInfo ci, Course c) {
        ClassCourseDTO dto = new ClassCourseDTO();
        dto.setId(cc.getId());
        dto.setClassId(cc.getClassId());
        dto.setClassName(ci != null ? ci.getName() : "-");
        dto.setCourseId(cc.getCourseId());
        dto.setCourseName(c != null ? c.getName() : "-");
        dto.setWeeklyHours(cc.getWeeklyHours());
        return dto;
    }
}
