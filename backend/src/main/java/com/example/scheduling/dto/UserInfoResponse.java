package com.example.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** 当前登录用户信息（不含 token） */
@Getter
@Setter
@AllArgsConstructor
public class UserInfoResponse {
    private String username;
    private String role;
}
