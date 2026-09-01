package com.example.scheduling.controller;

import com.example.scheduling.dto.ApiResponse;
import com.example.scheduling.entity.Course;
import com.example.scheduling.service.CourseService;
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
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<List<Course>> list() {
        return ApiResponse.ok(courseService.list());
    }

    @PostMapping
    public ApiResponse<Course> create(@Valid @RequestBody Course course) {
        return ApiResponse.ok(courseService.create(course));
    }

    @PutMapping("/{id}")
    public ApiResponse<Course> update(@PathVariable Long id, @Valid @RequestBody Course req) {
        return ApiResponse.ok(courseService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ApiResponse.ok();
    }
}
