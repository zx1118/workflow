package com.epiroc.workflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.epiroc.workflow.common.common.WorkflowResult;
import com.epiroc.workflow.common.entity.ScParam;
import com.epiroc.workflow.common.entity.form.TaskForm;
import com.epiroc.workflow.common.entity.form.WfSubmitForm;
import com.epiroc.workflow.common.service.WfTaskService;
import com.epiroc.workflow.common.util.AsyncService;
import org.springframework.web.bind.annotation.*;
import com.epiroc.workflow.common.service.WorkflowService;
import com.epiroc.workflow.common.entity.param.BatchOperateParam;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 测试 Controller
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-15
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private WorkflowService workflowService;

    @Resource
    private WfTaskService wfTaskService;

    @GetMapping("/detail")
    public WorkflowResult<Object> detail(@RequestParam Integer orderId) {
        return WorkflowResult.ok(workflowService.detail(orderId, ScParam.class.getName()));
    }

    @PostMapping("/submit")
    public WorkflowResult<Object> submit(@RequestBody WfSubmitForm wfSubmitForm) {
        return workflowService.submit(wfSubmitForm);
    }

    /**
     * 分页查询待办任务
     * @param taskForm 查询条件
     * @return 分页结果
     */
    @PostMapping("/pending-tasks")
    public WorkflowResult queryPendingTasks(@RequestBody TaskForm taskForm) {
        try {
            // 验证必要参数
            if (taskForm.getCurrentUserId() == null && taskForm.getCurrentUserEmail() == null) {
                return WorkflowResult.error("当前用户ID或邮箱不能为空");
            }
            
            IPage<Map<String, Object>> result = wfTaskService.queryPendingTasks(taskForm);
            return WorkflowResult.ok(result);
        } catch (Exception e) {
            return WorkflowResult.error("查询待办任务失败: " + e.getMessage());
        }
    }

    /**
     * 查询待办任务列表（不分页）
     * @param taskForm 查询条件
     * @return 任务列表
     */
    @PostMapping("/pending-list")
    public WorkflowResult queryPendingTasksList(@RequestBody TaskForm taskForm) {
        try {
            // 验证必要参数
            if (taskForm.getCurrentUserId() == null && taskForm.getCurrentUserEmail() == null) {
                return WorkflowResult.error("当前用户ID或邮箱不能为空");
            }
            
            IPage<Map<String, Object>> result = wfTaskService.pending(taskForm);
            return WorkflowResult.ok(result);
        } catch (Exception e) {
            return WorkflowResult.error("查询待办任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 批量提交测试
     * @param batchParam 批量操作参数
     * @return 批量操作结果
     */
    @PostMapping("/batch-submit")
    public Map<String, Object> batchSubmit(@RequestBody BatchOperateParam batchParam) {
        return workflowService.batchSubmit(batchParam);
    }

    @Resource
    private AsyncService asyncService;


    @GetMapping("/async")
    public String test() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = asyncService.processAsyncWithResult("Test");
//        System.out.println(future.get());
        return "success";
    }

}
