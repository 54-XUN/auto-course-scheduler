package com.example.scheduling.controller;

import com.example.scheduling.dto.ApiResponse;
import com.example.scheduling.entity.TimeSlot;
import com.example.scheduling.service.TimeSlotService;
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
@RequestMapping("/api/time-slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping
    public ApiResponse<List<TimeSlot>> list() {
        return ApiResponse.ok(timeSlotService.list());
    }

    @PostMapping
    public ApiResponse<TimeSlot> create(@Valid @RequestBody TimeSlot req) {
        return ApiResponse.ok(timeSlotService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<TimeSlot> update(@PathVariable Long id, @Valid @RequestBody TimeSlot req) {
        return ApiResponse.ok(timeSlotService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        timeSlotService.delete(id);
        return ApiResponse.ok();
    }
}
