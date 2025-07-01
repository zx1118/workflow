package com.epiroc.workflow.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.entity.WfRule;
import com.epiroc.workflow.common.mapper.WfRuleMapper;
import com.epiroc.workflow.common.service.WfRuleService;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.system.rule.model.RuleParam;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WfRuleServiceImpl implements WfRuleService, WorkflowConstant, CommonConstant {

    @Autowired
    private WfRuleMapper wfRuleMapper;

    @Override
    public Map<String, Object> getWfKeyUserParticipants(RuleParam param, Integer wfRuleId) {
        Map<String, Object> resultMap = new HashMap<>();
        WfRule wfRule = wfRuleMapper.selectById(wfRuleId);
        if (wfRule != null) {
            // param转换为Map
            Map<String, Object> paramMap = JSON.parseObject(JSON.toJSONString(param), Map.class);

            String executeSql = wfRule.getExecuteSql();
            // 替换查询条件中的变量
            StringSubstitutor sub = new StringSubstitutor(paramMap);
            executeSql = sub.replace(executeSql);
            List<Map<String, Object>> keyUserList = wfRuleMapper.executeSql(executeSql);
            if (oConvertUtils.listIsNotEmpty(keyUserList)) {
                for(Map<String, Object> keyUser : keyUserList){
                    resultMap.put(keyUser.get("field").toString(), keyUser.get("name").toString());
                    resultMap.put(keyUser.get("field").toString() + UNIT_SHORT_LINE_UNDER + STRING_NAME_GUID, keyUser.get("guid").toString());
                    resultMap.put(keyUser.get("field").toString() + UNIT_SHORT_LINE_UNDER +STRING_EMAIL, keyUser.get("email").toString());
                    resultMap.put(keyUser.get("field").toString() + UNIT_SHORT_LINE_UNDER + KEY_USER_ID, keyUser.get("id").toString());
                }
            }
        }
        return resultMap;
    }

}
