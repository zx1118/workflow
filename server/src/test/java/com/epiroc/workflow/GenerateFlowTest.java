package com.epiroc.workflow;

import com.epiroc.workflow.common.system.flow.FlowContext;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import com.epiroc.workflow.common.service.WfFlowService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作流生成测试
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-15
 */
@SpringBootTest
public class GenerateFlowTest {

    @Resource
    private WfFlowService wfFlowService;

    @Test
    public void testFlowInfo(){
        FlowParam flowParam = new FlowParam();

        FlowContext flowContext = new FlowContext("2,0", wfFlowService);
        flowParam.setStage("1");
        flowParam.setWfProcessId(95);
        flowParam.setUnitId("1");
        flowParam.setAmount(new BigDecimal("6000"));
        Map<String, Object> testMap = flowContext.getFlowInfoResult(new HashMap<>(), flowParam);
        int a = 1;
    }


}
