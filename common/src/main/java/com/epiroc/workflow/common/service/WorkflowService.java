package com.epiroc.workflow.common.service;

import com.epiroc.workflow.common.common.WorkflowResult;
import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.entity.form.ApproveForm;
import com.epiroc.workflow.common.entity.form.GetFlowForm;
import com.epiroc.workflow.common.entity.form.TaskForm;
import com.epiroc.workflow.common.entity.form.WfSubmitForm;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.entity.param.BatchOperateParam;

import java.util.List;
import java.util.Map;

/**
 * 工作流服务接口
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-14
 */
public interface WorkflowService {

    /**
     * 工作流处理
     * @param operateParam
     * @return
     */
    WorkflowResult operate(OperateParam operateParam);

    /**
     * 工作流处理-通过Handler处理
     * @param operateParam
     * @return
     */
    Map<String, Object> operateByHandler(OperateParam operateParam);

    /**
     * 获取流程信息
     * @param getFlowForm
     * @return
     */
    WorkflowResult getFlow(GetFlowForm getFlowForm);

    /**
     * 申请提交
     * @param wfSubmitForm
     * @return
     */
    WorkflowResult submit(WfSubmitForm wfSubmitForm);

    /**
     * 审批流程
     * @param approveForm
     * @return
     */
    WorkflowResult approve(ApproveForm approveForm);

    /**
     * 订单详情
     * @param orderId
     * @param className
     * @return
     */
    Map<String, Object> detail(Integer orderId, String className);

    /**
     * 查询任务
     * @param taskForm
     * @return
     */
    Map<String, Object> queryTask(TaskForm taskForm);

    /**
     * 批量提交流程
     * @param batchParam 批量操作参数
     * @return 批量操作结果
     */
    Map<String, Object> batchSubmit(BatchOperateParam batchParam);

    /**
     * 操作订单和参数
     * @param operateParam
     * @return
     */
    WfOrder operateOrderAndParam(OperateParam operateParam);

}
