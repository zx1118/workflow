package com.epiroc.workflow.common.system.flow;

import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.service.WfFlowService;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;

import java.util.List;
import java.util.Map;

public class SetFlow extends FlowSuper<FlowParam> {

    private final WfFlowService wfFlowService;

    /**
     * 流程顺序
     */
    private Integer index;

    public SetFlow(IFlow flow, WfFlowService wfFlowService, Integer index) {
        super(flow);
        this.wfFlowService = wfFlowService;
        this.index = index;
    }

    @Override
    public List flow(List flowList, FlowParam param) {
        super.flow(flowList, param);
        // 业务处理
        param.setInx(index);
        param.setFlowType(WorkflowConstant.FLOW_TYPE_SET);
        List<Integer> wfFlowIdList = wfFlowService.getWfFlowIdList(param);
        flowList.addAll(wfFlowIdList);
        return flowList;
    }

    @Override
    public Map<String, Object> flowInfo(Map<String, Object> resultMap, FlowParam param) {
        super.flowInfo(resultMap, param);
        // 业务处理
        param.setInx(index);
        param.setFlowType(WorkflowConstant.FLOW_TYPE_SET);
        List<WfFlow> setFlowList = wfFlowService.getSetFlowDetail(param);
        Map<String, List<WfFlow>> flowInfoMap = (Map<String, List<WfFlow>>)resultMap.get("flowInfoMap");
        flowInfoMap.put("setFlowInfo" + "_" + index, setFlowList);
        List<WfFlow> flowList = (List<WfFlow>) resultMap.get("flowList");
        flowList.addAll(setFlowList);
        return resultMap;
    }

}
