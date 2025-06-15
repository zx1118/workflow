package com.epiroc.workflow.common.service;

import com.epiroc.workflow.common.system.rule.model.RuleParam;

import java.util.Map;

public interface WfRuleService {

    Map<String, Object> getWfKeyUserParticipants(RuleParam param, Integer wfRuleId);

}
