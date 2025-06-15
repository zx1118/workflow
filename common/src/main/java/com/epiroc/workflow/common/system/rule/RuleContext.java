package com.epiroc.workflow.common.system.rule;

import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.service.WfRuleService;
import com.epiroc.workflow.common.system.rule.model.RulePair;
import com.epiroc.workflow.common.system.rule.model.RuleParam;

import java.util.List;
import java.util.Map;

public class RuleContext {

    private IRule rule;

    // 通过构造方法， 传入具体的审批流生成策略
    public RuleContext(List<RulePair> rulePairList, WfRuleService wfRuleService) {
        // 把用户本身的组织架构加载进来
        rule = new RuleOrganizationalStructure();
        if(oConvertUtils.isNotEmpty(rulePairList)){
            for (int i = 0;i < rulePairList.size();i++) {
                Integer ruleType = rulePairList.get(i).getRuleType();
                rule = RuleDecoratorFactory.createRule(ruleType, rule, wfRuleService, rulePairList.get(i).getWfRuleId());
            }
        }
    }

    public Map<String, Object> getResult(Map<String, Object> resultMap, RuleParam ruleParam, WfFlow wfFlow) {
        return rule.getParticipants(resultMap, ruleParam, wfFlow);
    }

}
