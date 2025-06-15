package com.epiroc.workflow.common.system.flow.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FlowParam {

    private Integer wfProcessId;

    private String stage;

    private Integer inx;

    private String flowType;

    /**
     * 去重,默认为 false
     */
    private Boolean distinct = false;

    private Integer unitId;

    private BigDecimal amount;

}
