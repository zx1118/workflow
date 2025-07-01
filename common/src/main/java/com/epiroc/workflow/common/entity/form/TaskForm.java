package com.epiroc.workflow.common.entity.form;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-23
 */
@Data
public class TaskForm {

    /**
     * 流程定义ID
     */
    private Long processId;

    /**
     * 当前登录用户ID
     */
    private String currentUserId;

    /**
     * 当前登录用户邮箱
     */
    private String currentUserEmail;

    /**
     * 当前登录用户名
     */
    private String currentUserName;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField = "create_time";

    /**
     * 排序方向：ASC或DESC
     */
    private String sortOrder = "DESC";

    /**
     * 申请人ID（可选查询条件）
     */
    private String requesterId;

    /**
     * 申请人名称（可选查询条件）
     */
    private String requesterName;

    /**
     * 订单状态（可选查询条件）
     */
    private String orderStatus;

    /**
     * 任务状态（可选查询条件）
     */
    private String taskStatus;

    /**
     * 流程名称（可选查询条件）
     */
    private String processName;

    /**
     * 申请开始时间（可选查询条件）
     */
    private Date startTime;

    /**
     * 申请结束时间（可选查询条件）
     */
    private Date endTime;

    /**
     * 动态查询条件Map（可选）
     */
    private Map<String, Object> dynamicConditions;

    /**
     * 是否包含业务参数数据
     */
    private Boolean includeBusinessData = false;

    private String tableName;

}
