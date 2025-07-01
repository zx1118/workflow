package com.epiroc.workflow.common.service;

import com.epiroc.workflow.common.mapper.WfDictItemMapper;
import com.epiroc.workflow.common.mapper.WfDictMapper;
import com.epiroc.workflow.common.system.vo.DictModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
@Slf4j
@Service
@Order(2) // 确保在DatabaseInitializer(@Order(1))之后执行
public class WfDictLoadService implements ApplicationRunner {

    public static final Map<String, String> WF_ORDER_STATUS_DICT_CACHE = new ConcurrentHashMap<>();

    public static final Map<String, String> WF_TASK_STATUS_DICT_CACHE = new ConcurrentHashMap<>();

    public static final Map<String, String> WF_APPROVE_TYPE_DICT_CACHE = new ConcurrentHashMap<>();

    @Resource
    private WfDictMapper wfDictMapper;

    @Resource
    private WfDictItemMapper wfDictItemMapper;
    
    @Autowired
    private DatabaseTableInitService databaseTableInitService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        init();
    }

    public void init() {
        try {
            // 检查表是否存在
            if (!databaseTableInitService.tableExists("wf_dict") || 
                !databaseTableInitService.tableExists("wf_dict_item")) {
                log.warn("字典表尚未创建，跳过字典缓存加载");
                return;
            }
            
            log.info("开始加载字典缓存...");
            loadOrderStatusDictCache();
            loadTaskStatusDictCache();
            loadApproveTypeDictCache();
            log.info("字典缓存加载完成");
        } catch (Exception e) {
            log.error("加载字典缓存失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
    
    private void loadOrderStatusDictCache() {
        try {
            List<DictModel> dicts = wfDictMapper.queryDictItemsByCode("order_status");
            for (DictModel dict : dicts) {
                WF_ORDER_STATUS_DICT_CACHE.put(dict.getValue(), dict.getText());
                WF_ORDER_STATUS_DICT_CACHE.put(dict.getText(), dict.getValue());
            }
            log.info("加载order_status字典缓存成功，共{}条", dicts.size());
        } catch (Exception e) {
            log.warn("加载order_status字典缓存失败: {}", e.getMessage());
        }
    }

    private void loadTaskStatusDictCache() {
        try {
            List<DictModel> dicts = wfDictMapper.queryDictItemsByCode("task_status");
            for (DictModel dict : dicts) {
                WF_TASK_STATUS_DICT_CACHE.put(dict.getValue(), dict.getText());
                WF_TASK_STATUS_DICT_CACHE.put(dict.getText(), dict.getValue());
            }
            log.info("加载task_status字典缓存成功，共{}条", dicts.size());
        } catch (Exception e) {
            log.warn("加载task_status字典缓存失败: {}", e.getMessage());
        }
    }

    private void loadApproveTypeDictCache() {
        try {
            List<DictModel> dicts = wfDictMapper.queryDictItemsByCode("approve_type");
            for (DictModel dict : dicts) {
                WF_APPROVE_TYPE_DICT_CACHE.put(dict.getValue(), dict.getText());
                WF_APPROVE_TYPE_DICT_CACHE.put(dict.getText(), dict.getValue());
            }
            log.info("加载approve_type字典缓存成功，共{}条", dicts.size());
        } catch (Exception e) {
            log.warn("加载approve_type字典缓存失败: {}", e.getMessage());
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
