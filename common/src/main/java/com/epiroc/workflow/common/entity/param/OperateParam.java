package com.epiroc.workflow.common.entity.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 流程操作参数实体类
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-15
 */
@Data
public class OperateParam {

    /**
     * wf_order 主键 ID
     */
    private Integer orderId;

    /**
     * wf_task 主键 ID
     */
    private Integer taskId;

    private String orderStatus;

    /**
     * 1. 提交
     * 2. 保存为草稿
     * 3. 审批
     * 4. 取消
     * 5. 驳回
     * 6. 退回
     */
    private String operateType;

    /**
     * 参数类名
     */
    private String className;

    /**
     * 操作人姓名
     */
    private String operator;

    /**
     * 操作人邮箱
     */
    private String operatorEmail;

    /**
     * 操作人 ID
     */
    private String operatorId;

    private String creatorId;

    private String creatorName;

    private String creatorEmail;

    /**
     * 实际申请人 ID
     */
    private String requesterId;

    private String requesterName;

    private String requesterEmail;

    /**
     * 审批人
     */
    private String approver;


    /**
     *  审批人 ID
     */
    private String approverId;

    /**
     * 审批人邮箱
     */
    private String approverEmail;

    /**
     * 流程
     */
    private List<WfTaskParticipant> flowList;

    /**
     * 流程定义 ID
     */
    private Integer wfProcessId;

    /**
     * 流程定义
     */
    private WfProcess wfProcess;

    /**
     * 任务
     */
    private WfTask wfTask;

    /**
     * 业务参数
     */
    private Map<String, Object> param;

    /**
     * 业务参数-参数列表
     */
    private List<Map<String, Object>> paramList;

    /**
     * 业务参数-参数ID
     */
    private String paramId;

    /**
     * 业务参数-参数ID字段名
     */
    private String idFieldName;

    private String comment;


}
