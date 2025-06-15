package com.epiroc.workflow.common.system.factory;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 操作工厂类
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-14
 */
public class OperateFactory {

    private static Map<String, OperateAbstractHandler> operateStrategyMap = new ConcurrentHashMap<>();

    public static OperateAbstractHandler getInvokeStrategy(String type) {
        return operateStrategyMap.get(type);
    }

    public static void registerHandler(String type, OperateAbstractHandler handler) {
        if (operateStrategyMap.containsKey(type)) {
            throw new RuntimeException("Operate Handler type " + type + " already exists");
        }
        if (null == handler || StringUtils.isEmpty(type)) {
            return;
        }
        operateStrategyMap.put(type, handler);
    }

}
