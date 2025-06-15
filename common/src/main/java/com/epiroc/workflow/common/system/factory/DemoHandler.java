package com.epiroc.workflow.common.system.factory;

import org.springframework.stereotype.Component;

/**
 * Demo
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-23
 */
@Component
public class DemoHandler extends AbstractHandler {

    @Override
    public void afterPropertiesSet() throws Exception {
        Factory.registerHandler("demo", this);
    }

    @Override
    public void handle(){
        // 业务逻辑处理
        throw new UnsupportedOperationException("not implemented");
    }

}
