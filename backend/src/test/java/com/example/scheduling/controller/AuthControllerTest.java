package com.example.scheduling.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.example.scheduling.dto.ApiResponse;
import com.example.scheduling.dto.UserInfoResponse;
import com.example.scheduling.security.JwtUtil;
import com.example.scheduling.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 认证控制器测试：缺失 role claim 时返回 null 而非字符串 "null" */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Claims claims;

    @InjectMocks
    private AuthController authController;

    @Test
    void me_missingRoleClaim_returnsNullRole() {
        when(jwtUtil.extractToken(request)).thenReturn("token");
        when(jwtUtil.parse("token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("admin");
        when(claims.get("role")).thenReturn(null);

        ApiResponse<UserInfoResponse> response = authController.me(request);

        assertEquals(200, response.getCode());
        assertEquals("admin", response.getData().getUsername());
        assertNull(response.getData().getRole());
    }

    @Test
    void me_withRoleClaim_returnsRole() {
        when(jwtUtil.extractToken(request)).thenReturn("token");
        when(jwtUtil.parse("token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("admin");
        when(claims.get("role")).thenReturn("ADMIN");

        ApiResponse<UserInfoResponse> response = authController.me(request);

        assertEquals(200, response.getCode());
        assertEquals("ADMIN", response.getData().getRole());
    }
}
