package com.epiroc.workflow.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

/**
 * 异步服务示例
 * 演示如何使用配置的线程池和@Async注解
 */
@Service
public class AsyncService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

    /**
     * 通过@Qualifier注入指定名称的线程池
     */
    @Autowired
    @Qualifier("workflowExecutor")
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    @Qualifier("scheduledExecutor")
    private ScheduledThreadPoolExecutor scheduledExecutor;

    @PostConstruct
    public void init() {
        logger.info("AsyncService初始化完成，线程池核心线程数: {}", threadPoolExecutor.getCorePoolSize());
    }

    /**
     * 使用@Async注解的异步方法
     * 默认使用配置的taskExecutor
     */
    @Async
    public void processAsync(String taskId) {
        logger.info("开始异步处理任务: {}, 线程: {}", taskId, Thread.currentThread().getName());
        try {
            // 模拟耗时操作
            Thread.sleep(2000);
            logger.info("异步任务处理完成: {}", taskId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("异步任务处理中断: {}", taskId);
        }
    }

    /**
     * 带返回值的异步方法
     * 指定使用taskExecutor线程池
     */
    @Async("taskExecutor")
    public CompletableFuture<String> processAsyncWithResult(String taskId) {
        logger.info("开始带返回值的异步任务: {}, 线程: {}", taskId, Thread.currentThread().getName());
        try {
            // 模拟复杂处理
            Thread.sleep(5000);
            System.out.println("wait");
            return CompletableFuture.completedFuture("任务 " + taskId + " 处理结果");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("带返回值的异步任务中断: {}", taskId);
        }
        return CompletableFuture.completedFuture("任务 " + taskId + " 处理结果");
    }

    /**
     * 直接使用注入的ThreadPoolExecutor
     */
    public void executeWithPoolExecutor(String taskId) {
        threadPoolExecutor.execute(() -> {
            logger.info("使用ThreadPoolExecutor执行任务: {}, 线程: {}",
                    taskId, Thread.currentThread().getName());
            try {
                Thread.sleep(1500);
                logger.info("ThreadPoolExecutor任务完成: {}", taskId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 记录线程池状态
        logger.info("线程池状态 - 活跃线程: {}, 已完成任务: {}, 队列大小: {}",
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getCompletedTaskCount(),
                threadPoolExecutor.getQueue().size());
    }

    /**
     * 使用ScheduledThreadPoolExecutor执行定时任务
     */
    public void scheduleTask(String taskId, long delaySeconds) {
        logger.info("安排定时任务: {}, 延迟: {}秒", taskId, delaySeconds);

        scheduledExecutor.schedule(() -> {
            logger.info("执行定时任务: {}, 线程: {}",
                    taskId, Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
                logger.info("定时任务完成: {}", taskId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }

    /**
     * 演示带异常处理的异步方法
     */
    @Async
    public void processAsyncWithException(String taskId, boolean shouldFail) {
        logger.info("执行可能会失败的异步任务: {}", taskId);
        try {
            Thread.sleep(1000);
            if (shouldFail) {
                throw new RuntimeException("任务故意失败: " + taskId);
            }
            logger.info("异步任务成功完成: {}", taskId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}