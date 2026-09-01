package com.example.scheduling.controller;

import com.example.scheduling.dto.ApiResponse;
import com.example.scheduling.dto.ClassCourseDTO;
import com.example.scheduling.dto.ClassCourseRequest;
import com.example.scheduling.service.ClassCourseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/class-courses")
public class ClassCourseController {

    private final ClassCourseService classCourseService;

    public ClassCourseController(ClassCourseService classCourseService) {
        this.classCourseService = classCourseService;
    }

    @GetMapping
    public ApiResponse<List<ClassCourseDTO>> list() {
        return ApiResponse.ok(classCourseService.list());
    }

    @PostMapping
    public ApiResponse<ClassCourseDTO> create(@Valid @RequestBody ClassCourseRequest req) {
        return ApiResponse.ok(classCourseService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<ClassCourseDTO> update(@PathVariable Long id, @Valid @RequestBody ClassCourseRequest req) {
        return ApiResponse.ok(classCourseService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        classCourseService.delete(id);
        return ApiResponse.ok();
    }
}
