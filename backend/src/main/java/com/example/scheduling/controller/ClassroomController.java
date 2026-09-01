package com.example.scheduling.controller;

import com.example.scheduling.dto.ApiResponse;
import com.example.scheduling.entity.Classroom;
import com.example.scheduling.service.ClassroomService;
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
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping
    public ApiResponse<List<Classroom>> list() {
        return ApiResponse.ok(classroomService.list());
    }

    @PostMapping
    public ApiResponse<Classroom> create(@Valid @RequestBody Classroom classroom) {
        return ApiResponse.ok(classroomService.create(classroom));
    }

    @PutMapping("/{id}")
    public ApiResponse<Classroom> update(@PathVariable Long id, @Valid @RequestBody Classroom req) {
        return ApiResponse.ok(classroomService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        classroomService.delete(id);
        return ApiResponse.ok();
    }
}
