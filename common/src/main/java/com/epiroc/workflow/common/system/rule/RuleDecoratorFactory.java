package com.epiroc.workflow.common.system.rule;

import com.epiroc.workflow.common.service.WfRuleService;;

public class RuleDecoratorFactory {

    public static IRule createRule(Integer ruleType, IRule rule, WfRuleService wfRuleService, Integer wfRuleId) {
        switch (ruleType) {
            // wf_key_user 表
            case 1:
                rule = new RuleWfKeyUser(rule, wfRuleService, wfRuleId);
                break;
            // 数据库
            case 2:
                rule = new RuleDatabase(rule, wfRuleService, wfRuleId);
                break;
            case 3:
                rule = new RuleExternal(rule, wfRuleService, wfRuleId);
                break;
            default:
                break;
        }
        return rule;
    }

}
