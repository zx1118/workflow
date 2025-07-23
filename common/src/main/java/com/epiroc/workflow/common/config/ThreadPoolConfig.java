package com.epiroc.workflow.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置类
 * 支持Spring的@Async注解使用
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);

    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 10;

    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 50;

    /**
     * 线程存活时间（秒）
     */
    private static final int KEEP_ALIVE_TIME = 60;

    /**
     * 队列容量
     */
    private static final int QUEUE_CAPACITY = 200;

    /**
     * 线程名前缀
     */
    private static final String THREAD_NAME_PREFIX = "workflow-async-";

    /**
     * 配置默认的线程池任务执行器
     * 用于处理@Async注解标记的方法
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);

        // 最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);

        // 线程存活时间
        executor.setKeepAliveSeconds(KEEP_ALIVE_TIME);

        // 队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);

        // 线程名前缀
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);

        // 拒绝策略：调用者线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 初始化线程池
        executor.initialize();

        logger.info("线程池初始化完成 [核心线程数: {}, 最大线程数: {}, 队列容量: {}]",
                CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);

        return executor;
    }

    /**
     * 配置异常处理器，处理@Async方法执行时抛出的未捕获异常
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    /**
     * 异步方法的异常处理器
     */
    public static class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        private static final Logger log = LoggerFactory.getLogger(AsyncExceptionHandler.class);

        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error("异步任务执行异常 - 方法: {}.{}(), 参数: {}, 异常: {}",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName(),
                    params,
                    ex.getMessage(),
                    ex);
        }
    }

    /**
     * 提供原生的ThreadPoolExecutor，用于直接使用
     */
    @Bean(name = "workflowExecutor")
    public ThreadPoolExecutor threadPoolExecutor() {
        // 创建线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "workflow-pool-" + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                if (thread.getPriority() != Thread.NORM_PRIORITY) {
                    thread.setPriority(Thread.NORM_PRIORITY);
                }
                return thread;
            }
        };

        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);

        return executor;
    }

    /**
     * 提供定时任务执行器
     */
    @Bean(name = "scheduledExecutor")
    public ScheduledThreadPoolExecutor scheduledThreadPoolExecutor() {
        return new ScheduledThreadPoolExecutor(
                5,
                r -> {
                    Thread thread = new Thread(r, "workflow-scheduled");
                    thread.setDaemon(false);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}