package com.epiroc.workflow.common.system.constant;

/**
 * 任务状态常量
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-15
 */
public interface TaskStatusConstant {

    /**
     * 未审批
     */
    String NOT_APPROVED = "NOT_APPROVED";

    /**
     * 等待操作
     */
    String WAITING = "WAITING";

    /**
     * 已审批
     */
    String APPROVED = "APPROVED";

    /**
     * 取消
     */
    String CANCELLED = "CANCELLED";

    /**
     * 关单
     */
    String CLOSED = "CLOSED";

}
