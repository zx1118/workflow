package com.epiroc.workflow.common.system.rule;

import java.util.Map;

public class RuleOrganizationalStructure<T, M> implements IRule<T, M> {

    @Override
    public Map<String, Object> getParticipants(Map<String, Object> resultMap, T param, M m) {
        return resultMap;
    }

}
