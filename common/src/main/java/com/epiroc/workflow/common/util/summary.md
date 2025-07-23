线程池说明和使用：
这个线程池管理器包括以下功能：
单例模式 - 确保全局只有一个线程池实例
两种执行器：
普通线程池：用于执行一般任务
调度线程池：用于执行定时任务和周期性任务
核心方法：
execute(Runnable) - 执行无返回值任务
submit(Callable) - 执行有返回值任务
schedule(Runnable, delay, unit) - 延迟执行任务
scheduleAtFixedRate(...) - 周期性执行任务
Spring集成：
使用@Component注解，支持Spring自动管理
使用@PostConstruct和@PreDestroy进行生命周期管理
使用示例：
// 方法1：通过Spring注入
@Autowired
private ThreadPoolManager threadPoolManager;

// 方法2：直接获取单例实例
ThreadPoolManager threadPool = ThreadPoolManager.getInstance();

// 示例1：执行简单任务
threadPool.execute(() -> {
    System.out.println("执行任务...");
});

// 示例2：执行带返回值的任务
Future<String> future = threadPool.submit(() -> {
    return "任务结果";
});
String result = future.get(); // 获取结果，会阻塞

// 示例3：执行延迟任务
threadPool.schedule(() -> {
    System.out.println("延迟5秒后执行");
}, 5, TimeUnit.SECONDS);

// 示例4：执行周期性任务
threadPool.scheduleAtFixedRate(() -> {
    System.out.println("每10秒执行一次");
}, 0, 10, TimeUnit.SECONDS);

比较两个线程池实现，主要差异如下：

1. **功能完整性**：
   - `WorkflowThreadPool`更加完善，提供了更多功能和方法，如：
     - 更细致的异常处理
     - 完整的线程池关闭流程（带超时等待和强制关闭）
     - 更多类型的任务提交方式（支持Runnable+结果、Callable）
     - 更丰富的监控方法（getActiveCount、getCompletedTaskCount、getStatus）
     - 线程优先级设置
     - 定时任务的多种执行模式（固定速率和固定延迟）

2. **异常处理策略**：
   - `ThreadPoolManager`使用`CallerRunsPolicy`（拒绝策略更温和，由调用者线程执行任务）
   - `WorkflowThreadPool`使用自定义拒绝策略，记录日志后抛出异常

3. **线程池配置**：
   - `WorkflowThreadPool`允许核心线程超时（`allowCoreThreadTimeOut(true)`）
   - `WorkflowThreadPool`为调度线程池设置了和主线程池相同的拒绝策略

4. **代码风格**：
   - `WorkflowThreadPool`注释更详尽，包含更多方法说明和参数描述
   - `WorkflowThreadPool`在线程池状态监控方面更完善

5. **关闭处理**：
   - `WorkflowThreadPool`的关闭处理更严谨，包含了超时和强制关闭的机制
   - `ThreadPoolManager`的关闭处理相对简单

**哪个更好用？**

`WorkflowThreadPool`整体上更好用，原因是：

1. 功能更完整，提供了更多的线程池操作方法
2. 异常处理更严谨，对任务拒绝有明确的日志和异常
3. 关闭处理更安全，确保所有任务都能得到妥善处理
4. 提供了更丰富的监控信息，便于排查问题
5. 代码风格更规范，文档更详尽

不过，如果您的应用场景比较简单，不需要太多复杂功能，`ThreadPoolManager`代码更简洁，也足够使用。实际选择应该根据您的具体需求而定：

- 对稳定性和完整性要求高，选择`WorkflowThreadPool`
- 对简洁性和基本功能满足即可，选择`ThreadPoolManager`

两者命名上有些混淆（都有管理器的含义），建议选择其一使用，并保持命名上的一致性。

以下是WorkflowThreadPool的使用示例。这个示例展示了如何在Spring环境和非Spring环境中使用线程池，以及如何执行各种类型的任务：
package com.epiroc.workflow.util;

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
                Thread.sleep(1000);
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
                Thread.sleep(2000);
                return "任务处理结果";
            };

            // 提交任务并获取Future
            Future<String> future = threadPool.submit(task);

            // 等待任务完成并获取结果（最多等待5秒）
            return future.get(5, TimeUnit.SECONDS);
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
                Thread.sleep(100 * taskId);
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

这个示例展示了如何：
依赖注入 vs 单例获取：两种获取线程池实例的方式
执行不同类型的任务：
无返回值任务 (Runnable)
有返回值任务 (Callable)
批量任务处理
定时任务：
延迟执行
固定频率执行
固定延迟执行
异常处理：如何正确捕获线程中的异常
实际业务场景：模拟一个工作流处理的场景
线程池状态监控：获取线程池运行状态
在实际应用中，您可以根据业务需求调用不同的方法。例如：
// 在Controller或Service中使用
@Autowired
private ThreadPoolExample threadPoolExample;

// 处理异步任务
public void handleRequest() {
    // 提交简单任务
    threadPoolExample.executeSimpleTask();

    // 获取带返回值的任务结果
    String result = threadPoolExample.executeCallableTask();

    // 处理工作流
    threadPoolExample.processWorkflow("WF-001");

    // 获取线程池状态
    String status = threadPoolExample.getThreadPoolStatus();
    System.out.println("线程池状态: " + status);
}


`ThreadPoolHelper` 是一个线程池辅助工具类，主要作用如下：

1. **线程池管理**：
   - 提供了一个封装好的线程池实现，统一管理线程资源
   - 使用单例模式确保应用中只有一个线程池实例，避免资源浪费

2. **任务执行功能**：
   - 提供执行普通任务的方法 `execute(Runnable)`
   - 支持有返回值任务的执行 `submit(Callable)`
   - 支持延时任务执行 `schedule(Runnable, delay, unit)`
   - 支持定时周期性任务 `scheduleAtFixedRate()`

3. **线程资源优化**：
   - 通过合理配置线程池参数（核心线程数、最大线程数、队列大小等）优化线程资源使用
   - 使用 `CallerRunsPolicy` 拒绝策略，当线程池饱和时让调用者线程执行任务，避免任务丢失

4. **生命周期管理**：
   - 通过 `@PostConstruct` 在应用启动时初始化线程池
   - 通过 `@PreDestroy` 在应用关闭时安全关闭线程池，避免资源泄露

5. **Spring集成**：
   - 标记为 `@Component`，可以直接在 Spring 环境中使用依赖注入
   - 也提供了单例模式 `getInstance()` 方法，适用于非 Spring 环境

`ThreadPoolHelper` 与其他线程池类（如 `WorkflowThreadPool` 或 `ThreadPoolManager`）相比，是一个更轻量级、更简洁的实现，主要提供基础线程池功能，没有过多的复杂特性。它适用于以下场景：

- 需要异步执行一些任务但不需要太复杂功能的场景
- 希望简化线程池使用，提供简洁 API 的场景
- 对性能要求不是特别高，但需要可靠任务执行的场景

总之，`ThreadPoolHelper` 是一个简化版的线程池管理工具，提供了最常用的线程池功能，帮助开发者轻松实现异步任务和定时任务的执行，同时避免了直接使用 `ThreadPoolExecutor` 的复杂性。

