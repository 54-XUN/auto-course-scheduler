package com.example.scheduling.dto;

import lombok.Getter;
import lombok.Setter;

/** 统一响应格式 */
@Getter
@Setter
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
