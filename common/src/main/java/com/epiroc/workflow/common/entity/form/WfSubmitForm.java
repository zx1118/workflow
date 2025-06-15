package com.epiroc.workflow.common.entity.form;

import com.epiroc.workflow.common.entity.WfFile;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class WfSubmitForm implements Serializable {

    private static final long serialVersionUID = 2468829770165206579L;

    private Integer wfProcessId;

    /**
     * 申请类型
     * 1:请假；2:加班
     */
    private String appCode;

    /**
     * 公司 ID
     */
    private String unitId;

    /**
     * 申请类型
     */
    private String requestType;

    /**
     * 实际申请人 ID
     */
    private String requesterId;

    /**
     * 实际申请人名字
     */
    private String requesterName;

    /**
     * 实际申请人邮箱
     */
    private String requesterEmail;

    /**
     * 创建人 guid
     */
    private String creatorId;

    /**
     * 创建人名字
     */
    private String creatorName;

    /**
     *  创建人邮箱
     */
    private String creatorEmail;

    private String comment;

    /**
     * 流程状态
     * 0：暂存 storage
     * 1：审批中 pending
     * 2：已取消 cancel
     * 3：已完成 complete
     */
    private String orderStatus;

    private String orderId;

    /**
     * 业务参数
     */
    private Map<String, Object> param;

    /**
     * 业务参数-参数列表
     */
    private List<Map<String, Object>> paramList;

    /**
     * 流程列表
     */
    private List<WfTaskParticipant> flowList;

    /**
     * 额外审批人
     */
    private WfTaskParticipant additionalApprover;

    private List<WfFile> fileList;

}
