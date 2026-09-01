package com.example.scheduling.controller;

import com.example.scheduling.dto.ApiResponse;
import com.example.scheduling.dto.TeacherDTO;
import com.example.scheduling.dto.TeacherRequest;
import com.example.scheduling.service.TeacherService;
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
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ApiResponse<List<TeacherDTO>> list() {
        return ApiResponse.ok(teacherService.list());
    }

    @PostMapping
    public ApiResponse<TeacherDTO> create(@Valid @RequestBody TeacherRequest req) {
        return ApiResponse.ok(teacherService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<TeacherDTO> update(@PathVariable Long id, @Valid @RequestBody TeacherRequest req) {
        return ApiResponse.ok(teacherService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ApiResponse.ok();
    }
}
