package com.epiroc.workflow.common.system.factory;

import org.springframework.beans.factory.InitializingBean;

/**
 * 模板方法类
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-23
 */
public abstract class AbstractHandler implements InitializingBean {

    public void handle(){
        throw new UnsupportedOperationException("not implemented");
    }

}
