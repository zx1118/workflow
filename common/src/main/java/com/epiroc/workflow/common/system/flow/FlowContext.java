package com.epiroc.workflow.common.system.flow;

import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.service.WfFlowService;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FlowContext {

    private IFlow flow;

    // 通过构造方法， 传入具体审批流生成策略
    public FlowContext(String flowTypes, WfFlowService wfFlowService) {
        // 固定流程，创建申请
        flow = new CreateRequestFlow();
        if(oConvertUtils.isNotEmpty(flowTypes)){
            List<String> flowTypeList = new ArrayList<>(Arrays.asList(flowTypes.split(",")));
            for (int i = 0;i < flowTypeList.size();i++) {
                String flowType = flowTypeList.get(i);
                flow = FlowDecoratorFactory.createFlow(flowType, flow, wfFlowService, i + 1);
            }
        }
    }

    public List getResult(List flowList, FlowParam flowParam) {
        return flow.flow(flowList, flowParam);
    }

    public Map<String, Object> getFlowInfoResult(Map<String, Object> resultMap, FlowParam flowParam) {
        return flow.flowInfo(resultMap, flowParam);
    }


}
