package com.epiroc.workflow.common.system.factory;

import cn.hutool.core.util.StrUtil;
import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.service.WorkflowStateService;
import com.epiroc.workflow.common.system.constant.OperateConstant;
import com.epiroc.workflow.common.system.constant.StateConstant;
import com.epiroc.workflow.common.util.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 审批-同意
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-24
 */
@Component
public class OperateApproveHandler extends OperateAbstractHandler implements StateConstant, OperateConstant {

    @Resource
    private WorkflowStateService workflowStateService;

    @Override
    public void afterPropertiesSet() throws Exception {
        OperateFactory.registerHandler(StringUtil.joinWithChar(PENDING, OPERATE_APPROVE, StrUtil.DASHED), this);
    }

    @Override
    public Map<String, Object> handle(WfOrder wfOrder, OperateParam operateParam) {
        WfTask wfTask = new WfTask();
        wfTask.setApprover(operateParam.getApprover());
        wfTask.setApproverId(operateParam.getApproverId());
        wfTask.setApproverEmail(operateParam.getApproverEmail());
        wfTask.setId(operateParam.getTaskId());
        wfTask.setComment(operateParam.getComment());
        // 审批状态 -> 审批操作
        return (Map<String, Object>) workflowStateService.approve(wfOrder, wfTask).getResult();
    }

}
