package com.epiroc.workflow.common.system.rule;

import java.util.Map;

public interface IRule<T, M> {

    public Map<String, Object> getParticipants(Map<String, Object> resultMap, T param, M wfFlow);

}
