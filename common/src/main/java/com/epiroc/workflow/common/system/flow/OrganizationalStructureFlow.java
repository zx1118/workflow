package com.epiroc.workflow.common.system.flow;

import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.service.WfFlowService;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import com.epiroc.workflow.common.util.oConvertUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-23
 */
public class OrganizationalStructureFlow extends FlowSuper<FlowParam> {

    private final WfFlowService wfFlowService;

    /**
     * 流程顺序
     */
    private Integer index;

    public OrganizationalStructureFlow(IFlow flow,  WfFlowService wfFlowService, Integer index) {
        super(flow);
        this.wfFlowService = wfFlowService;
        this.index = index;
    }

    @Override
    public List flow(List flowList, FlowParam param) {
        super.flow(flowList, param);
        // 业务处理
        param.setInx(index);
        param.setFlowType(WorkflowConstant.FLOW_TYPE_FIXED);
        List<Integer> wfFlowIdList = wfFlowService.getWfFlowIdList(param);
        flowList.addAll(wfFlowIdList);
        return flowList;
    }

    @Override
    public Map<String, Object> flowInfo(Map<String, Object> resultMap, FlowParam param) {
        super.flowInfo(resultMap, param);
        // 业务处理
        param.setInx(index);
        param.setFlowType(WorkflowConstant.FLOW_TYPE_ORGANIZATIONAL_STRUCTURE);
        List<WfFlow> osFlowList = wfFlowService.getFlowDetail(param);
        if(oConvertUtils.listIsNotEmpty(osFlowList)){
            Map<String, List<WfFlow>> flowInfoMap = (Map<String, List<WfFlow>>)resultMap.get("flowInfoMap");
            flowInfoMap.put("osFlowInfo" + "_" + index, osFlowList);
            List<WfFlow> flowList = (List<WfFlow>) resultMap.get("flowList");
            flowList.addAll(osFlowList);
        }
        return resultMap;
    }
}
