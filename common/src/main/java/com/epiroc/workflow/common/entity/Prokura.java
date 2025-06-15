package com.epiroc.workflow.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Prokura 实体类
 */
@Data
@TableName("prokura")
public class Prokura {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * wf_process 主键 ID
     */
    private Integer wfProcessId;

    /**
     * 公司主键 ID
     */
    @TableField("unit_id")
    private String unitId;

    /**
     * 最小审批额度
     */
    @TableField("min_approval")
    private Double minApproval;

    /**
     * 最大审批额度
     */
    @TableField("max_approval")
    private Double maxApproval;

    /**
     * wf_participant 主键 ID
     */
    @TableField("wf_participant_id")
    private String wfParticipantId;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @TableField("remarks")
    private String remarks;

    /**
     * 删除状态（0，正常，1已删除）
     */
    @TableField("del_flag")
    private String delFlag;

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
}
