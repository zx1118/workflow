package com.epiroc.workflow.common.system.factory;

import cn.hutool.core.util.StrUtil;
import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.service.WorkflowStateService;
import com.epiroc.workflow.common.system.constant.OperateConstant;
import com.epiroc.workflow.common.system.constant.StateConstant;
import com.epiroc.workflow.common.util.StringUtil;

import javax.annotation.Resource;

/**
 * 待提交-提交
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-14
 */
public class OperateWfToBeSumbit2SubmitHandler extends OperateAbstractHandler implements StateConstant, OperateConstant {

    @Resource
    private WorkflowStateService workflowStateService;

    @Override
    public void afterPropertiesSet() throws Exception {
        OperateFactory.registerHandler(StringUtil.joinWithChar(TO_BE_SUBMIT, OPERATE_SUBMIT, StrUtil.DASHED), this);
    }

    @Override
    public void handle(WfOrder wfOrder, OperateParam operateParam){
        // 未提交状态 -> 提交操作
        workflowStateService.submit(wfOrder, operateParam);
    }
}
