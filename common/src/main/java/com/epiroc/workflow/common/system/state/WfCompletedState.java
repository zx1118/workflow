package com.epiroc.workflow.common.system.state;

import com.epiroc.workflow.common.system.constant.CommonConstant;

import java.util.Map;

/**
 * 具体状态类-完成状态
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-15
 */
public class WfCompletedState implements WorkflowState {

    /**
     * 已完成状态下不能再提交
     * @param context 工作流上下文
     */
    @Override
    public Map<String, Object> submit(WorkflowContext context) {
        // 已完成状态下不能再提交
        throw new IllegalStateException("工作流已完成，不能再提交");
    }

    /**
     * 已完成状态下不能保存为草稿
     * @param context 工作流上下文
     */
    @Override
    public void saveAsDraft(WorkflowContext context) {
        // 已完成状态下不能保存为草稿
        throw new IllegalStateException("工作流已完成，不能保存为草稿");
    }

    /**
     * 已完成状态下不能取消
     * @param context 工作流上下文
     */
    @Override
    public void cancel(WorkflowContext context) {
        // 已完成状态下不能取消
        throw new IllegalStateException("工作流已完成，不能取消");
    }

    /**
     * 已完成状态下不能再审批
     * @param context 工作流上下文
     */
    @Override
    public Map<String, Object> approve(WorkflowContext context) {
        // 已完成状态下不能再审批
        throw new IllegalStateException("工作流已完成，不能再审批");
    }

    /**
     * 已完成状态下不能拒绝
     * @param context 工作流上下文
     */
    @Override
    public void reject(WorkflowContext context) {
        // 已完成状态下不能拒绝
        throw new IllegalStateException("工作流已完成，不能拒绝");
    }

    /**
     * 已完成状态下不能退回
     * @param context 工作流上下文
     */
    @Override
    public void returnToUser(WorkflowContext context) {
        // 已完成状态下不能退回
        throw new IllegalStateException("工作流已完成，不能退回");
    }

    /**
     * 获取状态名称
     * @return 状态名称
     */
    @Override
    public String getStateName() {
        return CommonConstant.ORDER_STATUS_COMPLETE;
    }
}