package com.epiroc.workflow.common.system.factory;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工厂类
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-23
 */
public class Factory {

    private static Map<String, AbstractHandler> strategyMap = new ConcurrentHashMap<>();

    public static AbstractHandler getInvokeStrategy(String type) {
        return strategyMap.get(type);
    }

    public static void registerHandler(String type, AbstractHandler handler) {
        if (strategyMap.containsKey(type)) {
            throw new RuntimeException("Handler type " + type + " already exists");
        }
        if (null == handler || StringUtils.isEmpty(type)) {
            return;
        }
        strategyMap.put(type, handler);
    }

}
