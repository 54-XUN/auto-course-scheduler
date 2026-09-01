package com.example.scheduling.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** 手动调课请求：调整时间段和/或教室 */
@Getter
@Setter
public class ScheduleMoveRequest {

    @NotNull(message = "目标时间段不能为空")
    private Long timeSlotId;

    @NotNull(message = "目标教室不能为空")
    private Long classroomId;
}
