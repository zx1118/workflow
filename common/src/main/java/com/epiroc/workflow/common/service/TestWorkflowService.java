package com.epiroc.workflow.common.service;

import com.epiroc.workflow.common.common.WorkflowResult;
import com.epiroc.workflow.common.entity.form.ApproveForm;
import com.epiroc.workflow.common.entity.form.GetFlowForm;
import com.epiroc.workflow.common.entity.form.WfSubmitForm;

import java.util.Map;

public interface TestWorkflowService {

    /**
     * 操作
     * @return
     */
    WorkflowResult operate();


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
     * 审批操作
     * @param approveForm
     * @return
     */
    WorkflowResult approve(ApproveForm approveForm);

    /**
     * 取消工作流
     * @param orderId 订单ID
     * @return 操作结果
     */
    WorkflowResult cancel(Integer orderId);

    /**
     * 订单详情
     * @param orderId
     * @param className
     * @return
     */
    Map<String, Object> detail(Integer orderId, String className);

}