package com.epiroc.workflow.common.entity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("wf_order")
public class WfOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("order_status")
    private String orderStatus;

    @TableField("order_no")
    private String orderNo;

    @TableField("unit_id")
    private Integer unitId;

    @TableField("depart_id")
    private Integer departId;

    @TableField("business_key")
    private String businessKey;

    @TableField("process_id")
    private Integer processId;

    /**
     * 实际申请人 ID
     */
    @TableField("requester_id")
    private String requesterId;

    @TableField("requester_name")
    private String requesterName;

    @TableField("requester_email")
    private String requesterEmail;

    @TableField("request_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date requestDate;

    @TableField("creator_id")
    private String creatorId;

    @TableField("creator_name")
    private String creatorName;

    @TableField("creator_email")
    private String creatorEmail;

    @TableField("finish_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    @TableField("create_by")
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField(value = "update_by")
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
