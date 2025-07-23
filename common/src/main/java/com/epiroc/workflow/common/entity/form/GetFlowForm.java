package com.epiroc.workflow.common.entity.form;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class GetFlowForm {

    /**
     * wf_process 主键 ID
     */
    private Integer wfProcessId;

    /**
     * 阶段（针对存在多段流程的情况，默认为1）
     */
    private String stage = "1";

    private String unitId;

    private String unit;

    private Map<String, Object> assigneeMap;

    private String comment;

    private String requestType;

    private BigDecimal amount;

    private Map<String, Object> param;

}
