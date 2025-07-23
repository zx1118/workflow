package com.epiroc.workflow.common.system.flow;

import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;

import java.util.*;

public class CreateRequestFlow implements IFlow<FlowParam> {

    @Override
    public List flow(List flowList, FlowParam param) {
        flowList.add(WorkflowConstant.CREATE_REQUEST_ID);
        return flowList;
    }

    @Override
    public Map<String, Object> flowInfo(Map<String, Object> resultMap, FlowParam param) {
        Map<String, List<WfFlow>> flowInfoMap = new LinkedHashMap<>();
        List<WfFlow> flowList = new ArrayList<>();
        if (WorkflowConstant.ADD_CREATE_REQUEST_STAGE.equals(param.getStage())) {
            WfFlow wfFlow = new WfFlow();
            wfFlow.setId(1);
            wfFlow.setFlowType("-1");
            wfFlow.setSortOrder(WorkflowConstant.FLOW_SORT_ORDER_CONSTANT);
            wfFlow.setName("Create Request");
            wfFlow.setDisplayName("创建申请");
            wfFlow.setField("REQUESTER");
            wfFlow.setRuleType(0);
            wfFlow.setWfRuleId(0);
            wfFlow.setRemarks(param.getComment());
            flowList.add(wfFlow);
            flowInfoMap.put("CreateRequestFlowInfo", new ArrayList<>(Arrays.asList(wfFlow)));
        }
        resultMap.put("flowInfoMap", flowInfoMap);
        resultMap.put("flowList", flowList);
        return resultMap;
    }

}
