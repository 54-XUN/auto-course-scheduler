package com.example.scheduling.controller;

import com.example.scheduling.dto.ApiResponse;
import com.example.scheduling.dto.AutoScheduleResponse;
import com.example.scheduling.dto.ScheduleDTO;
import com.example.scheduling.dto.ScheduleMoveRequest;
import com.example.scheduling.service.ScheduleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /** 触发自动排课 */
    @PostMapping("/auto")
    public ApiResponse<AutoScheduleResponse> autoSchedule() {
        return ApiResponse.ok(scheduleService.autoSchedule());
    }

    /** 查询排课结果 */
    @GetMapping
    public ApiResponse<List<ScheduleDTO>> list() {
        return ApiResponse.ok(scheduleService.list());
    }

    /** 手动调课 */
    @PutMapping("/{id}")
    public ApiResponse<ScheduleDTO> move(@PathVariable Long id, @Valid @RequestBody ScheduleMoveRequest req) {
        return ApiResponse.ok(scheduleService.move(id, req));
    }
}
