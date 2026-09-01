package com.example.scheduling.security;

import com.example.scheduling.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 认证和授权拦截器：除登录接口外要求有效JWT，管理接口要求ADMIN角色 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USERNAME = "auth.username";
    public static final String ATTR_ROLE = "auth.role";
    private static final String ADMIN_ROLE = "ADMIN";
    
    // 需要ADMIN角色的管理接口
    private static final List<String> ADMIN_PATHS = Arrays.asList(
        "/api/schedules/auto",      // 自动排课
        "/api/schedules/",          // 删除排课（DELETE方法）
        "/api/teachers/",           // 删除教师（DELETE方法）
        "/api/classes/",            // 删除班级（DELETE方法）
        "/api/courses/",            // 删除课程（DELETE方法）
        "/api/classrooms/",         // 删除教室（DELETE方法）
        "/api/time-slots/"          // 删除时间段（DELETE方法）
    );

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
        
        String role = String.valueOf(claims.get("role"));
        request.setAttribute(ATTR_USERNAME, claims.getSubject());
        request.setAttribute(ATTR_ROLE, role);
        
        // 检查管理接口的授权
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 检查是否是DELETE操作的管理接口
        if ("DELETE".equals(method) && isAdminPath(uri)) {
            if (!ADMIN_ROLE.equals(role)) {
                return rejectForbidden(response);
            }
        }
        
        // 检查自动排课等特殊管理接口
        if (isAdminPath(uri) && !ADMIN_ROLE.equals(role)) {
            return rejectForbidden(response);
        }
        
        return true;
    }
    
    private boolean isAdminPath(String uri) {
        return ADMIN_PATHS.stream().anyMatch(uri::startsWith);
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
