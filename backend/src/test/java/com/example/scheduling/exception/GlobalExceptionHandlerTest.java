package com.example.scheduling.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.scheduling.dto.ApiResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/** 全局异常处理器测试：所有异常统一返回对应 HTTP 状态码 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusiness_returnsCustomStatus() {
        BusinessException ex = new BusinessException(404, "未找到");
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusiness(ex);
        assertEquals(404, response.getStatusCode().value());
        assertEquals(404, response.getBody().getCode());
    }

    @Test
    void handleValidation_returns400Status() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "field", "字段不能为空");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals(400, response.getBody().getCode());
    }

    @Test
    void handleOther_returns500Status() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleOther(new RuntimeException("boom"));
        assertEquals(500, response.getStatusCode().value());
        assertEquals(500, response.getBody().getCode());
    }
}
