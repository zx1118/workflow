package com.epiroc.workflow.common.service.impl;

import com.epiroc.workflow.common.common.WorkflowResult;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.entity.form.GetFlowForm;
import com.epiroc.workflow.common.service.WfFlowService;
import com.epiroc.workflow.common.service.WfProcessService;
import com.epiroc.workflow.common.service.WfRuleService;
import com.epiroc.workflow.common.service.WorkFlowSetService;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.system.flow.FlowContext;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import com.epiroc.workflow.common.system.rule.RuleContext;
import com.epiroc.workflow.common.system.rule.model.RulePair;
import com.epiroc.workflow.common.system.rule.model.RuleParam;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkFlowSetServiceImpl implements WorkFlowSetService, WorkflowConstant, CommonConstant {

    @Resource
    private WfProcessService wfProcessService;

    @Resource
    private WfFlowService wfFlowService;

    @Resource
    private WfRuleService wfRuleService;

    /**
     * 获取流程配置信息
     * @param getFlowForm
     * @return
     */
    @Override
    public WorkflowResult getFlowConfig(GetFlowForm getFlowForm) {
        // 获取流程定义
        WfProcess wfProcess = wfProcessService.getById(getFlowForm.getWfProcessId());
        if (oConvertUtils.isEmpty(wfProcess)) {
            return WorkflowResult.error("流程定义不存在");
        }
        FlowContext flowContext = new FlowContext(wfProcess.getFlowTypes(), wfFlowService);
        // 流程参数
        FlowParam flowParam = oConvertUtils.entityToModel(getFlowForm, FlowParam.class);
        // 获取流程信息
        Map<String, Object> flowInfoMap = flowContext.getFlowInfoResult(new HashMap<>(), flowParam);
        if (oConvertUtils.isEmpty(flowInfoMap)) {
            return WorkflowResult.error("流程信息获取失败");
        }
        List<WfFlow> flowList = (List<WfFlow>) flowInfoMap.get("flowList");
        // 流程中包含的规则
        List<RulePair> rulePairList = flowList.stream()
                .filter(wfFlow -> wfFlow.getRuleType() != -1)
                .map(wfFlow -> new RulePair(wfFlow.getWfRuleId(), wfFlow.getRuleType()))
                .distinct()
                .collect(Collectors.toList());
        // 流程参与者参数
        RuleParam ruleParam = oConvertUtils.entityToModel(flowParam, RuleParam.class);
        // 规则 Context
        RuleContext ruleContext = new RuleContext(rulePairList, wfRuleService);
        // 添加流程审批人信息
        Map<String, Object> assigneeMap = new HashMap<>();
        assigneeMap = ruleContext.getResult(assigneeMap, ruleParam, null);
        for (int i = flowList.size() - 1;i >= 0;i--) {
            WfFlow wfFlow = flowList.get(i);
            if(!FLOW_TYPE_FIXED.equals(wfFlow.getFlowType())){   // 固定流程不要设置审批人;其他类型流程设置审批人
                String field = wfFlow.getField();
                if(assigneeMap.containsKey(field)){
                    String guid = assigneeMap.get(field + UNIT_SHORT_LINE_UNDER + STRING_NAME_GUID).toString();
                    String email = assigneeMap.get(field + UNIT_SHORT_LINE_UNDER + STRING_EMAIL).toString();
                    String name = assigneeMap.get(field).toString();
                    wfFlow.setOperator(name);
                    wfFlow.setOperatorId(guid);
                    wfFlow.setOperatorEmail(email);
                }
            }
        }
        return WorkflowResult.ok(flowList);
    }

}
