package com.epiroc.workflow.common.system.rule;

import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.service.WfRuleService;
import com.epiroc.workflow.common.system.rule.model.RuleParam;

import java.util.Map;

public class RuleDatabase extends RuleSuper<RuleParam, WfFlow> {

    private final WfRuleService wfRuleService;

    private final Integer wfRuleId;

    public RuleDatabase(IRule rule, WfRuleService wfRuleService, Integer wfRuleId) {
        super(rule);
        this.wfRuleService = wfRuleService;
        this.wfRuleId = wfRuleId;
    }

    @Override
    public Map<String, Object> getParticipants(Map<String, Object> resultMap, RuleParam param, WfFlow wfFlow) {
        super.getParticipants(resultMap, param, wfFlow);
        return resultMap;
    }

}
