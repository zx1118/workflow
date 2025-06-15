package com.epiroc.workflow;

import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 工作流测试
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-14
 */
@SpringBootTest
public class WorkflowTest {

    @Resource
    private WorkflowService workflowService;

    @Test
    public void testSubmit() {
        OperateParam operateParam = new OperateParam();
        operateParam.setWfProcessId(95);
        operateParam.setOrderStatus("TO_BE_SUBMIT");
        operateParam.setOperateType("SUBMIT");
        workflowService.operateByHandler(operateParam);
    }

}
