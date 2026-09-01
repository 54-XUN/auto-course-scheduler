package com.example.scheduling.security;

import com.example.scheduling.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 认证拦截器：除登录接口外全部要求有效 JWT */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USERNAME = "auth.username";
    public static final String ATTR_ROLE = "auth.role";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return reject(response);
        }
        Claims claims = jwtUtil.parse(header.substring(7));
        if (claims == null) {
            return reject(response);
        }
        request.setAttribute(ATTR_USERNAME, claims.getSubject());
        request.setAttribute(ATTR_ROLE, String.valueOf(claims.get("role")));
        return true;
    }

    private boolean reject(HttpServletResponse response) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(
                objectMapper.writeValueAsBytes(ApiResponse.error(401, "未登录或登录已过期")));
        return false;
    }
}
