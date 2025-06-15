package com.epiroc.workflow.common.service;

import java.util.List;
import java.util.Map;

public interface ParamService {

    /**
     * 插入参数
     *
     * @param className
     * @param param
     * @param paramList
     * @return
     */
    String insertParam(String className, Map<String, Object> param, List<Map<String, Object>> paramList);

}
