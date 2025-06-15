package com.epiroc.workflow.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工作流线程池管理器
 * <p>
 * 提供统一的线程池管理，支持：
 * 1. 普通任务执行
 * 2. 定时任务执行
 * 3. 异步任务执行并获取结果
 * </p>
 */
@Component
public class WorkflowThreadPool {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowThreadPool.class);

    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 10;

    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 50;

    /**
     * 线程空闲时间（秒）
     */
    private static final int KEEP_ALIVE_TIME = 60;

    /**
     * 队列容量
     */
    private static final int QUEUE_CAPACITY = 200;

    /**
     * 执行器服务
     */
    private ThreadPoolExecutor executor;

    /**
     * 定时任务执行器
     */
    private ScheduledThreadPoolExecutor scheduledExecutor;

    /**
     * 单例实例
     */
    private static WorkflowThreadPool instance;

    /**
     * 获取实例
     */
    public static WorkflowThreadPool getInstance() {
        if (instance == null) {
            synchronized (WorkflowThreadPool.class) {
                if (instance == null) {
                    instance = new WorkflowThreadPool();
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
                // 设置为非守护线程
                thread.setDaemon(false);
                // 设置线程优先级
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        };

        // 创建拒绝处理策略
        RejectedExecutionHandler rejectedExecutionHandler = (r, executor) -> {
            logger.warn("线程池已满，任务被拒绝执行: {}", r.toString());
            if (r instanceof Runnable) {
                // 记录被拒绝的任务
                logger.error("被拒绝的任务: {}", r.getClass().getName());
            }
            throw new RejectedExecutionException("线程池已满，任务被拒绝执行");
        };

        // 初始化线程池
        executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                rejectedExecutionHandler
        );

        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);

        // 初始化定时任务执行器
        scheduledExecutor = new ScheduledThreadPoolExecutor(
                CORE_POOL_SIZE / 2,
                r -> {
                    Thread thread = new Thread(r, "workflow-scheduled-thread");
                    thread.setDaemon(false);
                    return thread;
                },
                rejectedExecutionHandler
        );

        logger.info("工作流线程池初始化完成，核心线程数: {}, 最大线程数: {}", CORE_POOL_SIZE, MAX_POOL_SIZE);
    }

    /**
     * 关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        logger.info("正在关闭工作流线程池...");

        // 关闭线程池
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                // 等待所有任务完成
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    // 强制关闭
                    executor.shutdownNow();
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        logger.error("线程池未能完全关闭");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 关闭定时任务执行器
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("工作流线程池关闭完成");
    }

    /**
     * 提交任务
     *
     * @param task 任务
     */
    public void execute(Runnable task) {
        executor.execute(task);
    }

    /**
     * 提交有返回值的任务
     *
     * @param task 任务
     * @param <T>  返回值类型
     * @return Future对象
     */
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * 提交Runnable任务并指定返回值
     *
     * @param task   任务
     * @param result 返回值
     * @param <T>    返回值类型
     * @return Future对象
     */
    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(task, result);
    }

    /**
     * 提交Runnable任务
     *
     * @param task 任务
     * @return Future对象
     */
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    /**
     * 延迟执行任务
     *
     * @param task  任务
     * @param delay 延迟时间
     * @param unit  时间单位
     * @return ScheduledFuture对象
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }

    /**
     * 延迟执行有返回值的任务
     *
     * @param task  任务
     * @param delay 延迟时间
     * @param unit  时间单位
     * @param <T>   返回值类型
     * @return ScheduledFuture对象
     */
    public <T> ScheduledFuture<T> schedule(Callable<T> task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }

    /**
     * 以固定速率周期性执行任务
     *
     * @param task         任务
     * @param initialDelay 初始延迟时间
     * @param period       周期
     * @param unit         时间单位
     * @return ScheduledFuture对象
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * 以固定延迟周期性执行任务
     *
     * @param task         任务
     * @param initialDelay 初始延迟时间
     * @param delay        延迟时间
     * @param unit         时间单位
     * @return ScheduledFuture对象
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return scheduledExecutor.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    /**
     * 获取活跃线程数
     *
     * @return 活跃线程数
     */
    public int getActiveCount() {
        return executor.getActiveCount();
    }

    /**
     * 获取已完成任务数
     *
     * @return 已完成任务数
     */
    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    /**
     * 获取线程池状态
     *
     * @return 线程池状态摘要
     */
    public String getStatus() {
        return String.format(
                "ThreadPool Status: [核心线程数: %d, 当前线程数: %d, 活跃线程数: %d, 最大线程数: %d, 队列任务数: %d, 已完成任务数: %d]",
                executor.getCorePoolSize(),
                executor.getPoolSize(),
                executor.getActiveCount(),
                executor.getMaximumPoolSize(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount()
        );
    }
}