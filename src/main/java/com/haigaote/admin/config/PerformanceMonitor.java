package com.haigaote.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * 性能监控组件
 * 定期监控线程池状态和性能指标
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
@Component
public class PerformanceMonitor {

    @Autowired
    @Qualifier("dbAsyncExecutor")
    private Executor dbAsyncExecutor;

    @Autowired
    @Qualifier("generalAsyncExecutor")
    private Executor generalAsyncExecutor;

    /**
     * 每5分钟监控一次线程池状态
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300,000毫秒
    public void monitorThreadPools() {
        monitorExecutor("数据库异步线程池", dbAsyncExecutor);
        monitorExecutor("通用异步线程池", generalAsyncExecutor);
    }

    /**
     * 监控单个线程池执行器
     */
    private void monitorExecutor(String name, Executor executor) {
        if (executor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
            
            int activeCount = taskExecutor.getActiveCount();
            int poolSize = taskExecutor.getPoolSize();
            int corePoolSize = taskExecutor.getCorePoolSize();
            int maxPoolSize = taskExecutor.getMaxPoolSize();
            int queueSize = taskExecutor.getThreadPoolExecutor().getQueue().size();
            long completedTaskCount = taskExecutor.getThreadPoolExecutor().getCompletedTaskCount();
            
            System.out.println(String.format(
                "[性能监控] %s - 活跃线程: %d, 池大小: %d/%d (最大: %d), 队列: %d, 已完成任务: %d",
                name, activeCount, poolSize, corePoolSize, maxPoolSize, queueSize, completedTaskCount
            ));
        }
    }

    /**
     * 手动触发性能报告（可通过接口调用）
     */
    public String getPerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 性能监控报告 ===\n");
        
        if (dbAsyncExecutor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) dbAsyncExecutor;
            report.append("数据库异步线程池:\n");
            report.append(String.format("  - 活跃线程: %d\n", executor.getActiveCount()));
            report.append(String.format("  - 池大小: %d (核心: %d, 最大: %d)\n", 
                executor.getPoolSize(), executor.getCorePoolSize(), executor.getMaxPoolSize()));
            report.append(String.format("  - 队列任务: %d\n", executor.getThreadPoolExecutor().getQueue().size()));
            report.append(String.format("  - 已完成任务: %d\n", executor.getThreadPoolExecutor().getCompletedTaskCount()));
        }
        
        return report.toString();
    }
} 