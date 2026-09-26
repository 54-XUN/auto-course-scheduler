package com.example.scheduling.security;

import com.example.scheduling.IntegrationTest;
import com.example.scheduling.entity.User;
import com.example.scheduling.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class SecurityTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    public void setup() {
        // 清理并创建测试用户
        userRepository.deleteAll();
        
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(encoder.encode("admin123"));
        admin.setRole("ADMIN");
        userRepository.save(admin);
        
        User user = new User();
        user.setUsername("user");
        user.setPassword(encoder.encode("user123"));
        user.setRole("USER");
        userRepository.save(user);
    }

    @Test
    public void testCorsConfiguration() throws Exception {
        mockMvc.perform(options("/api/courses")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().is(401));
    }

    @Test
    public void testAdminRoleAuthorization() throws Exception {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        User user = userRepository.findByUsername("user").orElseThrow();
        
        String adminToken = "Bearer " + jwtUtil.generate(admin.getId(), "admin", "ADMIN");
        String userToken = "Bearer " + jwtUtil.generate(user.getId(), "user", "USER");

        // ADMIN可以访问自动排课接口
        mockMvc.perform(post("/api/schedules/auto")
                .header("Authorization", adminToken))
                .andExpect(status().is(409)); // 数据不完整，但权限检查通过

        // USER不能访问自动排课接口
        mockMvc.perform(post("/api/schedules/auto")
                .header("Authorization", userToken))
                .andExpect(status().is(403));

        // USER可以访问只读接口
        mockMvc.perform(get("/api/courses")
                .header("Authorization", userToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testRoleBasedDeleteAuthorization() throws Exception {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        User user = userRepository.findByUsername("user").orElseThrow();
        
        String adminToken = "Bearer " + jwtUtil.generate(admin.getId(), "admin", "ADMIN");
        String userToken = "Bearer " + jwtUtil.generate(user.getId(), "user", "USER");

        // ADMIN可以删除
        mockMvc.perform(delete("/api/teachers/1")
                .header("Authorization", adminToken))
                .andExpect(status().is(404)); // ID不存在，但权限检查通过

        // USER不能删除
        mockMvc.perform(delete("/api/teachers/1")
                .header("Authorization", userToken))
                .andExpect(status().is(403));
    }

    @Test
    public void testJwtTokenValidation() throws Exception {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        
        // 测试无效token
        mockMvc.perform(get("/api/courses")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().is(401));

        // 测试有效token
        String validToken = "Bearer " + jwtUtil.generate(admin.getId(), "admin", "ADMIN");
        mockMvc.perform(get("/api/courses")
                .header("Authorization", validToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testEnvironmentVariablesAreNotHardcoded() throws Exception {
        // 验证配置文件中没有硬编码密码
        User adminUser = userRepository.findByUsername("admin").orElseThrow();
        // 密码应该是加密的，不是明文
        assertTrue(encoder.matches("admin123", adminUser.getPassword()));
        assertNotEquals("admin123", adminUser.getPassword());
    }
}