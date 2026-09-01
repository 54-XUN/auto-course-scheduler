package com.example.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** 自动排课结果汇总 */
@Getter
@Setter
@AllArgsConstructor
public class AutoScheduleResponse {
    private int total;
    private String message;
}
