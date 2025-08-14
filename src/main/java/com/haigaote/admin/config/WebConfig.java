package com.haigaote.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 优化Web层的并发处理能力
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置异步支持
     * 设置异步请求的超时时间和默认任务执行器
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 设置异步请求超时时间（30秒）
        configurer.setDefaultTimeout(30000);
        
        // 可以设置默认的任务执行器（可选）
        // configurer.setTaskExecutor(asyncTaskExecutor());
    }
} 