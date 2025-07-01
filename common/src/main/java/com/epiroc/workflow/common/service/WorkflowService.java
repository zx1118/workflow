package com.epiroc.workflow.common.service;

import com.epiroc.workflow.common.common.WorkflowResult;
import com.epiroc.workflow.common.entity.form.ApproveForm;
import com.epiroc.workflow.common.entity.form.GetFlowForm;
import com.epiroc.workflow.common.entity.form.WfSubmitForm;
import com.epiroc.workflow.common.entity.param.OperateParam;

import java.util.Map;

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

    WorkflowResult approve(ApproveForm approveForm);

    /**
     * 订单详情
     * @param orderId
     * @param className
     * @return
     */
    Map<String, Object> detail(Integer orderId, String className);

}
