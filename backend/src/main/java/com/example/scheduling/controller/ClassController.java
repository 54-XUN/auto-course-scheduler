package com.example.scheduling.controller;

import com.example.scheduling.dto.ApiResponse;
import com.example.scheduling.entity.ClassInfo;
import com.example.scheduling.service.ClassInfoService;
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
@RequestMapping("/api/classes")
public class ClassController {

    private final ClassInfoService classInfoService;

    public ClassController(ClassInfoService classInfoService) {
        this.classInfoService = classInfoService;
    }

    @GetMapping
    public ApiResponse<List<ClassInfo>> list() {
        return ApiResponse.ok(classInfoService.list());
    }

    @PostMapping
    public ApiResponse<ClassInfo> create(@Valid @RequestBody ClassInfo info) {
        return ApiResponse.ok(classInfoService.create(info));
    }

    @PutMapping("/{id}")
    public ApiResponse<ClassInfo> update(@PathVariable Long id, @Valid @RequestBody ClassInfo req) {
        return ApiResponse.ok(classInfoService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        classInfoService.delete(id);
        return ApiResponse.ok();
    }
}
