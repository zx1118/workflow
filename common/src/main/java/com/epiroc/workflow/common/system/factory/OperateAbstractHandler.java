package com.epiroc.workflow.common.system.factory;

import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.param.OperateParam;
import org.springframework.beans.factory.InitializingBean;

/**
 * 操作-模板方法类
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-14
 */
public abstract class OperateAbstractHandler implements InitializingBean {

    public void handle(WfOrder wfOrder, OperateParam operateParam){
        throw new UnsupportedOperationException("not implemented");
    }

}
