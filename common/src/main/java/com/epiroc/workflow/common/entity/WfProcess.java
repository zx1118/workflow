package com.epiroc.workflow.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.epiroc.workflow.common.system.annotation.LikeQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("wf_process")
public class WfProcess {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    @LikeQuery
    private String name;

    @TableField("display_name")
    private String displayName;

    @TableField("request_type")
    private String requestType;

    @TableField("unit_id")
    private Integer unitId;

    @TableField("depart_id")
    private Integer departId;

    @TableField("app_code")
    private String appCode;

    @TableField("order_no_pre")
    private String orderNoPre;

    @TableField("order_no_length")
    private Integer orderNoLength;

    @TableField("create_by")
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField("update_by")
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField("del_flag")
    private String delFlag;

    @TableField("class_name")
    private String className;

    @TableField("table_name")
    private String tableName;

    @TableField("flow_types")
    private String flowTypes;

    private String requestNameEn;

}
