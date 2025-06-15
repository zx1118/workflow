package com.epiroc.workflow.common.entity.form;

import com.epiroc.workflow.common.entity.WfTaskParticipant;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author : Theo Zheng
 * @version : V1.0
 * @project : antbi
 * @file : ApproveForm
 * @description : TODO
 * @date : 2020-12-30 16:00
 * @Copyright : 2020 Epiroc Trading Co., Ltd. All rights reserved.
 */
@Data
public class ApproveForm implements Serializable {

    private static final long serialVersionUID = -90591004858615648L;

//    private MDSUser currentUser;

    private String applicationCode;

    private String orderId;

    private Integer taskId;

    private String comment;

    private String orderStatus;

    private List<WfTaskParticipant> flowList;

    private WfTaskParticipant additionalApprover;

    /**
     * 第二阶段工作流
     */
    private List<WfTaskParticipant> stageTwoFlowList;

    private Boolean invoiceApprove = false;

    private Boolean forceApprove = false;

    private List<String> invoiceIdList;

}
