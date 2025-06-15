package com.epiroc.workflow.common.service;

import com.epiroc.workflow.common.mapper.WfDictItemMapper;
import com.epiroc.workflow.common.mapper.WfDictMapper;
import com.epiroc.workflow.common.system.vo.DictModel;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字典服务类
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-14
 */
@Service
public class WfDictLoadService {

    public static final Map<String, String> WF_ORDER_STATUS_DICT_CACHE = new ConcurrentHashMap<>();

    public static final Map<String, String> WF_TASK_STATUS_DICT_CACHE = new ConcurrentHashMap<>();

    public static final Map<String, String> WF_APPROVE_TYPE_DICT_CACHE = new ConcurrentHashMap<>();

    @Resource
    private WfDictMapper wfDictMapper;

    @Resource
    private WfDictItemMapper wfDictItemMapper;

    @PostConstruct
    public void init() {
        loadOrderStatusDictCache();
        loadTaskStatusDictCache();
    }
    private void loadOrderStatusDictCache() {
        List<DictModel> dicts = wfDictMapper.queryDictItemsByCode("order_status");
        for (DictModel dict : dicts) {
            WF_ORDER_STATUS_DICT_CACHE.put(dict.getValue(), dict.getText());
            WF_ORDER_STATUS_DICT_CACHE.put(dict.getText(), dict.getValue());
        }
    }

    private void loadTaskStatusDictCache() {
        List<DictModel> dicts = wfDictMapper.queryDictItemsByCode("task_status");
        for (DictModel dict : dicts) {
            WF_TASK_STATUS_DICT_CACHE.put(dict.getValue(), dict.getText());
            WF_TASK_STATUS_DICT_CACHE.put(dict.getText(), dict.getValue());
        }
    }

    private void loadApproveTypeDictCache() {
        List<DictModel> dicts = wfDictMapper.queryDictItemsByCode("approve_type");
        for (DictModel dict : dicts) {
            WF_APPROVE_TYPE_DICT_CACHE.put(dict.getValue(), dict.getText());
            WF_APPROVE_TYPE_DICT_CACHE.put(dict.getText(), dict.getValue());
        }
    }

    public Map<String, String> getOrderStatusCacheInfo() {
        return WF_ORDER_STATUS_DICT_CACHE;
    }

    public Map<String, String> getApproveTypeCacheInfo() {
        return WF_APPROVE_TYPE_DICT_CACHE;
    }

    public Map<String, String> getTaskStatusCacheInfo() {
        return WF_TASK_STATUS_DICT_CACHE;
    }




}
