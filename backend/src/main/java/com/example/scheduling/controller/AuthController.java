package com.example.scheduling.controller;

import com.example.scheduling.dto.ApiResponse;
import com.example.scheduling.dto.LoginRequest;
import com.example.scheduling.dto.LoginResponse;
import com.example.scheduling.dto.UserInfoResponse;
import com.example.scheduling.security.JwtUtil;
import com.example.scheduling.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);
        Cookie cookie = new Cookie("token", loginResponse.getToken());
        cookie.setHttpOnly(true);
        // 本地开发可设为 false，生产环境建议配合 HTTPS 设为 true
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(12 * 3600); // 12 小时与 JWT 过期时间对齐
        response.addCookie(cookie);
        return ApiResponse.ok(loginResponse);
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me(HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);
        Claims claims = token != null ? jwtUtil.parse(token) : null;
        if (claims == null) {
            return ApiResponse.error(401, "未登录或登录已过期");
        }
        Object roleClaim = claims.get("role");
        String role = roleClaim != null ? roleClaim.toString() : null;
        return ApiResponse.ok(new UserInfoResponse(claims.getSubject(), role));
    }
}
