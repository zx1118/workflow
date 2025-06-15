package com.epiroc.workflow.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Data
@TableName("wf_task_participant")
public class WfTaskParticipant implements Serializable {

    private static final long serialVersionUID = -486076815193079630L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("order_id")
    private Integer orderId;

    @TableField("wf_flow_Id")
    private Integer WfFlowId;

    @TableField("participant_Id")
    private Integer participantId;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("name")
    private String name;

    @TableField("display_name")
    private String displayName;

    @TableField("task_status")
    private String taskStatus;

    @TableField("operator")
    private String operator;

    @TableField("operator_id")
    private String operatorId;

    @TableField("operator_email")
    private String operatorEmail;

    @TableField("finish_time")
    private Date finishTime;

    @TableField("comment")
    private String comment;

    @TableField("approve_type")
    private String approveType;

    @TableField("approver")
    private String approver;

    @TableField("approver_id")
    private String approverId;

    @TableField("approver_email")
    private String approverEmail;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField(value = "create_by")
    private String createBy;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField(value = "update_by")
    private String updateBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WfTaskParticipant that = (WfTaskParticipant) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(sortOrder, that.sortOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, sortOrder);
    }

}