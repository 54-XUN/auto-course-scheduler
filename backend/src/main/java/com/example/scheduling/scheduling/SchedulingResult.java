package com.example.scheduling.scheduling;

import java.util.List;

/** 排课引擎执行结果 */
public record SchedulingResult(boolean success, List<Placement> placements, String message) {
}
