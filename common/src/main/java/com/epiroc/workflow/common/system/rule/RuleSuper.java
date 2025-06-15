package com.epiroc.workflow.common.system.rule;

import java.util.Map;

/**
 * 算法接口：工作流审批人规则
 */
public class RuleSuper<T, M> implements IRule<T, M> {

    protected IRule component;

    public RuleSuper(IRule rule) {
        this.component = rule;
    }

    @Override
    public Map<String, Object> getParticipants(Map<String, Object> resultMap, T param, M m) {
        if (this.component != null) {
            // 若装饰对象存在，则调用其flow方法
            resultMap = this.component.getParticipants(resultMap, param, m);
        }
        return resultMap;
    }


}
