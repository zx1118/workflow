package com.epiroc.workflow;

import com.epiroc.workflow.common.convert.WfFlow2WfTaskParticipant;
import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.service.WfFlowService;
import com.epiroc.workflow.common.service.WorkflowService;
import com.epiroc.workflow.common.system.flow.FlowContext;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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

    @Resource
    private WfFlowService wfFlowService;


    @Test
    public void testSubmit() {
        FlowParam flowParam = new FlowParam();

        FlowContext flowContext = new FlowContext("2,0", wfFlowService);
        flowParam.setStage("1");
        flowParam.setWfProcessId(95);
        flowParam.setUnitId("1");
        flowParam.setAmount(new BigDecimal("6000"));
        Map<String, Object> testMap = flowContext.getFlowInfoResult(new HashMap<>(), flowParam);
        List<WfFlow> flowList = (List<WfFlow>) testMap.get("flowList");
        // 转换
        Map<String, Object> assigneeMap = new HashMap<>();
        List<WfTaskParticipant> flowResultList = WfFlow2WfTaskParticipant.getSubmitWfTaskParticipants(flowList, assigneeMap);
        OperateParam operateParam = new OperateParam();
        operateParam.setFlowList(flowResultList);
        operateParam.setWfProcessId(95);
        operateParam.setOrderStatus("TO_BE_SUBMIT");
        operateParam.setOperateType("SUBMIT");
        Map<String, Object> resultMap = workflowService.operateByHandler(operateParam);
    }

    @Test
    public void testDictDataLoad() {
        // 这个测试主要是验证字典数据是否正确加载，不会抛出异常即为成功
        System.out.println("字典数据加载测试完成");
    }

}
