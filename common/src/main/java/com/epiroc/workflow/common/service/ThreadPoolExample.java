package com.epiroc.workflow.common.service;

import com.epiroc.workflow.common.util.WorkflowThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * WorkflowThreadPool使用示例
 */
@Service
public class ThreadPoolExample {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolExample.class);

    // 方式1：Spring依赖注入
    @Autowired
    private WorkflowThreadPool threadPool;

    /**
     * 简单任务执行示例
     */
    public void executeSimpleTask() {
        // 方式2：直接获取单例实例（非Spring环境）
        WorkflowThreadPool pool = WorkflowThreadPool.getInstance();

        // 示例1：执行无返回值任务
        pool.execute(() -> {
            logger.info("正在执行简单任务...");
            try {
                // 模拟耗时操作
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.info("简单任务执行完成");
        });

        // 输出线程池状态
        logger.info(pool.getStatus());
    }

    /**
     * 带返回值任务执行示例
     */
    public String executeCallableTask() {
        try {
            // 创建有返回值的任务
            Callable<String> task = () -> {
                logger.info("正在执行带返回值的任务...");
                // 模拟耗时处理
                Thread.sleep(5000);
                logger.info("带返回值任务执行完成");
                return "任务处理结果";
            };

            // 提交任务并获取Future
            Future<String> future = threadPool.submit(task);

            // 等待任务完成并获取结果（最多等待5秒）
            return future.get(5000, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("任务执行异常", e);
            return "执行失败: " + e.getMessage();
        }
    }

    /**
     * 批量任务执行示例
     */
    public List<String> executeBatchTasks(int taskCount) {
        List<Future<String>> futures = new ArrayList<>();
        List<String> results = new ArrayList<>();

        // 提交多个任务
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            futures.add(threadPool.submit(() -> {
                logger.info("执行任务 {}", taskId);
                // 模拟不同任务的执行时间
                Thread.sleep(1000 * taskId);
                return "任务" + taskId + "完成";
            }));
        }

        // 收集所有任务结果
        for (Future<String> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                results.add("任务失败: " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * 定时任务示例
     */
    public void scheduleTask() {
        // 示例1：延迟执行任务
        threadPool.schedule(() -> {
            logger.info("延迟3秒后执行的任务");
        }, 3, TimeUnit.SECONDS);

        // 示例2：固定频率执行任务（每2秒执行一次，延迟1秒开始）
        threadPool.scheduleAtFixedRate(() -> {
            logger.info("以固定频率执行的任务");
        }, 1, 2, TimeUnit.SECONDS);

        // 示例3：固定延迟执行任务（上一次执行完成后再等待3秒执行下一次）
        threadPool.scheduleWithFixedDelay(() -> {
            logger.info("以固定延迟执行的任务");
            try {
                // 模拟任务执行时间
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    /**
     * 异常处理示例
     */
    public void handleExceptionTask() {
        try {
            threadPool.submit(() -> {
                logger.info("执行可能抛出异常的任务");
                // 模拟任务异常
                throw new RuntimeException("任务执行失败");
            });
        } catch (Exception e) {
            // 注意：这里捕获不到任务内部的异常，因为异常发生在另一个线程
            logger.error("提交任务时发生异常", e);
        }

        // 正确处理任务内部异常的方式
        Future<String> future = threadPool.submit(() -> {
            try {
                // 可能抛出异常的代码
                if (Math.random() > 0.5) {
                    throw new RuntimeException("随机异常");
                }
                return "任务成功";
            } catch (Exception e) {
                logger.error("任务内部捕获异常", e);
                // 重新抛出异常，让Future.get()能够捕获到
                throw e;
            }
        });

        try {
            // 通过Future.get()获取可能的异常
            String result = future.get();
            logger.info("任务结果: {}", result);
        } catch (Exception e) {
            logger.error("通过Future捕获任务异常", e);
        }
    }

    /**
     * 综合示例：在服务类中使用
     */
    public void processWorkflow(String workflowId) {
        logger.info("开始处理工作流: {}", workflowId);

        // 1. 提交预处理任务
        Future<Boolean> preprocessFuture = threadPool.submit(() -> {
            logger.info("预处理工作流: {}", workflowId);
            // 模拟预处理逻辑
            Thread.sleep(1000);
            return true;
        });

        try {
            // 2. 等待预处理完成
            Boolean preprocessResult = preprocessFuture.get();

            if (preprocessResult) {
                // 3. 并行处理多个步骤
                List<Future<?>> stepFutures = new ArrayList<>();

                // 提交步骤1
                stepFutures.add(threadPool.submit(() -> {
                    logger.info("执行工作流步骤1: {}", workflowId);
                    Thread.sleep(2000);
                    return "步骤1完成";
                }));

                // 提交步骤2
                stepFutures.add(threadPool.submit(() -> {
                    logger.info("执行工作流步骤2: {}", workflowId);
                    Thread.sleep(1500);
                    return "步骤2完成";
                }));

                // 等待所有步骤完成
                for (Future<?> future : stepFutures) {
                    future.get();
                }

                // 4. 提交最终处理（延迟执行，模拟确认步骤）
                threadPool.schedule(() -> {
                    logger.info("最终确认工作流: {}", workflowId);
                }, 1, TimeUnit.SECONDS);

                logger.info("工作流处理安排完成: {}", workflowId);
            } else {
                logger.warn("工作流预处理失败: {}", workflowId);
            }
        } catch (Exception e) {
            logger.error("工作流处理异常: " + workflowId, e);
        }
    }

    /**
     * 监控线程池状态
     */
    public String getThreadPoolStatus() {
        return threadPool.getStatus();
    }
}