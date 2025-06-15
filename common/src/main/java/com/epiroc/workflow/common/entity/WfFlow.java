package com.epiroc.workflow.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.epiroc.workflow.common.system.handler.JsonListTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class WfFlow implements Serializable {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * wf_process 主键 ID
     */
    @TableField("wf_process_id")
    private Integer wfProcessId;

    /**
     * wf_participant 主键 IDs（JSON 数组）
     */
    @TableField(value = "wf_participant_ids", typeHandler = JsonListTypeHandler.class)
    private List<Integer> wfParticipantIds;

    /**
     * 流程类型(0-固定流程,1-Prokura流程,2-设置审批人的流程)
     */
    @TableField("flow_type")
    private String flowType;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * flow_type在wf_process中的顺序
     */
    @TableField("inx")
    private Integer inx;

    @TableField("name")
    private String name;

    @TableField("display_name")
    private String displayName;

    /**
     * 阶段（针对存在多段流程的情况，默认为1）
     */
    @TableField("stage")
    private String stage;

    @TableField("operator")
    private String operator;

    @TableField("operator_id")
    private String operatorId;

    @TableField("operator_email")
    private String operatorEmail;

    private String field;

    private String relateField;

    private Integer wfRuleId;

    private Integer ruleType;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新人
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 删除状态（0，正常，1已删除）
     */
    @TableField("del_flag")
    private String delFlag;

    /**
     * 备注
     */
    @TableField("remarks")
    private String remarks;

}
