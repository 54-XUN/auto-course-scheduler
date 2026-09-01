package com.example.scheduling.controller;

import com.example.scheduling.dto.ApiResponse;
import com.example.scheduling.dto.TimetableDTO;
import com.example.scheduling.service.TimetableService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 课表三视角查询（Excel 导出由前端 xlsx 库完成） */
@RestController
@RequestMapping("/api/timetables")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping("/class/{id}")
    public ApiResponse<TimetableDTO> classTimetable(@PathVariable Long id) {
        return ApiResponse.ok(timetableService.classTimetable(id));
    }

    @GetMapping("/teacher/{id}")
    public ApiResponse<TimetableDTO> teacherTimetable(@PathVariable Long id) {
        return ApiResponse.ok(timetableService.teacherTimetable(id));
    }

    @GetMapping("/classroom/{id}")
    public ApiResponse<TimetableDTO> classroomTimetable(@PathVariable Long id) {
        return ApiResponse.ok(timetableService.classroomTimetable(id));
    }
}
