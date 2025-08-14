package com.haigaote.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 管理后台Spring Boot启动类
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
@SpringBootApplication
@MapperScan("com.haigaote.admin.mapper")
@EnableScheduling
public class AdminApplication {

    /**
     * 应用程序入口点
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
} 