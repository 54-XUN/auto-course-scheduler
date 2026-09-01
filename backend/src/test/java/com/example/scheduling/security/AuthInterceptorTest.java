package com.example.scheduling.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 认证拦截器测试：缺失 role claim 时请求属性为 null 而非字符串 "null" */
@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Claims claims;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @Test
    void preHandle_missingRoleClaim_attributeIsNull() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/courses");
        when(request.getAttribute(anyString())).thenAnswer(inv -> attributes.get(inv.getArgument(0)));
        doAnswer(inv -> {
            attributes.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(request).setAttribute(anyString(), any());

        when(jwtUtil.extractToken(request)).thenReturn("token");
        when(jwtUtil.parse("token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user");
        when(claims.get("role")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals("user", request.getAttribute(AuthInterceptor.ATTR_USERNAME));
        assertNull(request.getAttribute(AuthInterceptor.ATTR_ROLE));
    }
}
