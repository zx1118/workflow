package com.epiroc.workflow.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 工作流任务表(wf_task)实体类
 */
@Data
@TableName("wf_task")
public class WfTask {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * wf_oder 主键 ID
     */
    @TableField("order_id")
    private Integer orderId;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 处理人
     */
    private String operator;

    /**
     * 审批人 ID
     */
    @TableField("operator_id")
    private String operatorId;

    /**
     * 审批人邮箱
     */
    @TableField("operator_email")
    private String operatorEmail;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("finish_time")
    private Date finishTime;

    /**
     * 审批类型：0：同意；1：退回；2：取消；3：关单
     */
    @TableField("approve_type")
    private String approveType;

    /**
     * 任务状态：0：未审批；1：等待操作；2：已审批；3：取消；4：关单
     */
    @TableField("task_status")
    private String taskStatus;

    /**
     * 父任务 ID
     */
    @TableField("parent_task_id")
    private String parentTaskId;

    /**
     * 审批人
     */
    private String approver;

    /**
     * 审批人 ID
     */
    @TableField("approver_id")
    private String approverId;

    /**
     * 审批人邮箱
     */
    @TableField("approver_email")
    private String approverEmail;

    /**
     * WTP ID
     */
    @TableField("wtp_id")
    private Integer wtpId;

    /**
     * 评论
     */
    private String comment;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新人
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 删除标志
     */
    @TableField("del_flag")
    private String delFlag;

    @Override
    public String toString() {
        return "WfTask{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", operator='" + operator + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", operatorEmail='" + operatorEmail + '\'' +
                ", finishTime=" + finishTime +
                ", approveType='" + approveType + '\'' +
                ", taskStatus='" + taskStatus + '\'' +
                ", parentTaskId='" + parentTaskId + '\'' +
                ", approver='" + approver + '\'' +
                ", approverId='" + approverId + '\'' +
                ", approverEmail='" + approverEmail + '\'' +
                ", wtpId='" + wtpId + '\'' +
                ", comment='" + comment + '\'' +
                ", createBy='" + createBy + '\'' +
                ", createTime=" + createTime +
                ", updateBy='" + updateBy + '\'' +
                ", updateTime=" + updateTime +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
