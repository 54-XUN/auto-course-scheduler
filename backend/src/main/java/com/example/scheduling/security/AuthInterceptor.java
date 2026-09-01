package com.example.scheduling.security;

import com.example.scheduling.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 认证和授权拦截器：除登录接口外要求有效JWT，管理接口要求ADMIN角色 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USERNAME = "auth.username";
    public static final String ATTR_ROLE = "auth.role";
    private static final String ADMIN_ROLE = "ADMIN";

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

        String token = jwtUtil.extractToken(request);
        if (token == null) {
            return reject(response);
        }
        Claims claims = jwtUtil.parse(token);
        if (claims == null) {
            return reject(response);
        }

        Object roleClaim = claims.get("role");
        String role = roleClaim != null ? roleClaim.toString() : null;
        request.setAttribute(ATTR_USERNAME, claims.getSubject());
        request.setAttribute(ATTR_ROLE, role);

        if (requiresAdmin(request) && !ADMIN_ROLE.equals(role)) {
            return rejectForbidden(response);
        }

        return true;
    }

    /**
     * 需要ADMIN角色的接口：
     * - 自动排课 POST /api/schedules/auto
     * - 基础数据的增、删、改（GET 查询除外）
     * - 手动调课 PUT /api/schedules/{id} 不需要 ADMIN
     */
    private boolean requiresAdmin(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equals(method) && "/api/schedules/auto".equals(uri)) {
            return true;
        }

        return isResourceWrite(method, uri);
    }

    private boolean isResourceWrite(String method, String uri) {
        if (!("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            return false;
        }
        String[] resources = {
            "/api/teachers", "/api/classes", "/api/courses",
            "/api/classrooms", "/api/time-slots", "/api/class-courses"
        };
        for (String base : resources) {
            if ("POST".equals(method) && base.equals(uri)) {
                return true;
            }
            if (("PUT".equals(method) || "DELETE".equals(method)) && uri.startsWith(base + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean reject(HttpServletResponse response) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(
                objectMapper.writeValueAsBytes(ApiResponse.error(401, "未登录或登录已过期")));
        return false;
    }

    private boolean rejectForbidden(HttpServletResponse response) throws Exception {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(
                objectMapper.writeValueAsBytes(ApiResponse.error(403, "无权访问该接口，需要管理员权限")));
        return false;
    }
}
