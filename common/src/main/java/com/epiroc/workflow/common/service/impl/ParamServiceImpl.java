package com.epiroc.workflow.common.service.impl;

import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.service.DynamicServiceDeprecate;
import com.epiroc.workflow.common.service.ParamService;
import com.epiroc.workflow.common.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ParamServiceImpl implements ParamService {

    @Autowired
    private DynamicServiceDeprecate dynamicService;

    @Autowired
    private TestService testService;

    @Override
    public String insertParam(String className, Map<String, Object> param, List<Map<String, Object>> paramList) {
        String id = null;
        if (oConvertUtils.isNotEmpty(param)) {
            id = dynamicService.insertWithStringId(className, param);
        }

        if (paramList != null && paramList.size() > 0) {
            for (Map<String, Object> map : paramList) {
                dynamicService.insert(className, map);
            }
        }
        return id;
    }
}
