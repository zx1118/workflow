package com.epiroc.workflow.common.service;

import com.epiroc.workflow.common.common.WorkflowResult;
import com.epiroc.workflow.common.entity.form.GetFlowForm;

/**
 * 工作流配置服务类
 */
public interface WorkFlowSetService {

    /**
     * 获取流程配置信息
     * @param getFlowForm
     * @return
     */
    WorkflowResult getFlowConfig(GetFlowForm getFlowForm);

}
