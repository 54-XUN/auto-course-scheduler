package com.example.scheduling.service;

import com.example.scheduling.dto.LoginRequest;
import com.example.scheduling.dto.LoginResponse;
import com.example.scheduling.entity.User;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.UserRepository;
import com.example.scheduling.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }
}
