package com.epiroc.workflow.common.system.flow;

import com.alibaba.fastjson.JSONObject;
import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.service.WfFlowService;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import com.epiroc.workflow.common.util.oConvertUtils;
import org.apache.commons.text.StringSubstitutor;

import java.util.List;
import java.util.Map;

/**
 * 条件流程
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-07-21
 */
public class ConditionFlow extends FlowSuper<FlowParam> {

    private final WfFlowService wfFlowService;

    /**
     * 流程顺序
     */
    private Integer index;

    public ConditionFlow(IFlow flow,  WfFlowService wfFlowService, Integer index) {
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
        param.setFlowType(WorkflowConstant.FLOW_TYPE_CONDITION);
        List<WfFlow> conditionFlowList = wfFlowService.getFlowDetail(param);
        // 条件处理
        for(int i = conditionFlowList.size() - 1; i >= 0; i--){
            WfFlow wfFlow = conditionFlowList.get(i);
            if(!oConvertUtils.isEmpty(wfFlow.getConditions())){
                // 条件处理
                String condition = wfFlow.getConditions();
                JSONObject jsonObject = JSONObject.parseObject(condition);
                if("sql".equals(jsonObject.getString("type"))){
                    String sql = jsonObject.getString("condition");
                    StringSubstitutor sub = new StringSubstitutor(param.getParam());
                    sql = sub.replace(sql);
                    List<Map<String, Object>> result = wfFlowService.executeSql(sql);
                    if(result.isEmpty()){
                        conditionFlowList.remove(i);
                    }
                }
            }
        }
        if(oConvertUtils.listIsNotEmpty(conditionFlowList)){
            Map<String, List<WfFlow>> flowInfoMap = (Map<String, List<WfFlow>>)resultMap.get("flowInfoMap");
            flowInfoMap.put("conditionFlowInfo"+"_"+index, conditionFlowList);
            List<WfFlow> flowList = (List<WfFlow>) resultMap.get("flowList");
            flowList.addAll(conditionFlowList);
        }
        return resultMap;
    }

}
