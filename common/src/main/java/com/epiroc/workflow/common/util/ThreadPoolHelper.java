package com.epiroc.workflow.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工作流线程池辅助类
 */
@Component
public class ThreadPoolHelper {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolHelper.class);

    // 核心线程数
    private static final int CORE_POOL_SIZE = 10;

    // 最大线程数
    private static final int MAX_POOL_SIZE = 50;

    // 线程空闲时间（秒）
    private static final int KEEP_ALIVE_TIME = 60;

    // 队列容量
    private static final int QUEUE_CAPACITY = 200;

    // 执行器服务
    private ThreadPoolExecutor executor;

    // 定时任务执行器
    private ScheduledThreadPoolExecutor scheduledExecutor;

    // 单例实例
    private static ThreadPoolHelper instance;

    /**
     * 获取实例
     */
    public static ThreadPoolHelper getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolHelper.class) {
                if (instance == null) {
                    instance = new ThreadPoolHelper();
                    instance.init();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化线程池
     */
    @PostConstruct
    public void init() {
        // 创建工作队列
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        // 创建线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "workflow-thread-" + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        };

        // 初始化线程池
        executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 初始化定时任务执行器
        scheduledExecutor = new ScheduledThreadPoolExecutor(
                5,
                r -> {
                    Thread thread = new Thread(r, "workflow-scheduled-thread");
                    thread.setDaemon(false);
                    return thread;
                }
        );

        logger.info("工作流线程池初始化完成");
    }

    /**
     * 关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
    }

    /**
     * 执行任务
     */
    public void execute(Runnable task) {
        executor.execute(task);
    }

    /**
     * 提交任务
     */
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * 提交任务
     */
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    /**
     * 延迟执行任务
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }

    /**
     * 周期性执行任务
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }
}