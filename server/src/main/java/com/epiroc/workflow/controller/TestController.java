package com.epiroc.workflow.controller;

import com.epiroc.workflow.common.common.Result;
import com.epiroc.workflow.common.entity.ScParam;
import com.epiroc.workflow.common.entity.form.WfSubmitForm;
import org.springframework.web.bind.annotation.*;
import com.epiroc.workflow.common.service.WorkflowService;

import javax.annotation.Resource;

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

    @GetMapping("/detail")
    public Result<Object> detail(@RequestParam Integer orderId) {
        return Result.ok(workflowService.detail(orderId, ScParam.class.getName()));
    }

    @PostMapping("/submit")
    public Result<Object> submit(@RequestBody WfSubmitForm wfSubmitForm) {
        return workflowService.submit(wfSubmitForm);
    }




}
