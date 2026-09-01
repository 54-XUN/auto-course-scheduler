package com.example.scheduling;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** 集成测试基类：启用 test profile，使用 H2 内存数据库 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTest {
}