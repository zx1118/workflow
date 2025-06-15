package com.epiroc.workflow.common.system.rule;

import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.service.WfRuleService;
import com.epiroc.workflow.common.system.rule.model.RuleParam;

import java.util.Map;

/**
 * 通过 wf_key_user 表获取流程参与人
 */
public class RuleWfKeyUser extends RuleSuper<RuleParam, WfFlow> {

    private final WfRuleService wfRuleService;

    private final Integer wfRuleId;

    public RuleWfKeyUser(IRule rule, WfRuleService wfRuleService, Integer wfRuleId) {
        super(rule);
        this.wfRuleService = wfRuleService;
        this.wfRuleId = wfRuleId;
    }

    /**
     * 获取流程参与人
     * @param resultMap
     * @param param
     * @param wfFlow
     * @return
     */
    @Override
    public Map<String, Object> getParticipants(Map<String, Object> resultMap, RuleParam param, WfFlow wfFlow) {
        super.getParticipants(resultMap, param, wfFlow);
        // 业务处理
        Map<String, Object> tempMap = wfRuleService.getWfKeyUserParticipants(param, wfRuleId);
        if(oConvertUtils.isNotEmpty(tempMap)){
            resultMap.putAll(tempMap);
        }
        return resultMap;
    }

}
