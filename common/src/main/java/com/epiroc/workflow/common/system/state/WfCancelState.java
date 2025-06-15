package com.epiroc.workflow.common.system.state;

import com.epiroc.workflow.common.system.constant.CommonConstant;

/**
 * 具体状态类-取消状态
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-15
 */
public class WfCancelState implements WorkflowState {

    /**
     * 取消状态下不能提交
     * @param context 工作流上下文
     */
    @Override
    public void submit(WorkflowContext context) {
        // 取消状态下不能提交
        throw new IllegalStateException("工作流已取消，不能提交");
    }

    /**
     * 取消状态下不能保存为草稿
     * @param context 工作流上下文
     */
    @Override
    public void saveAsDraft(WorkflowContext context) {
        // 取消状态下不能保存为草稿
        throw new IllegalStateException("工作流已取消，不能保存为草稿");
    }

    /**
     * 取消状态下不能再次取消
     * @param context 工作流上下文
     */
    @Override
    public void cancel(WorkflowContext context) {
        // 取消状态下不能再次取消
        // 可以不做任何操作，也可以抛出异常
    }

    /**
     * 取消状态下不能审批
     * @param context 工作流上下文
     */
    @Override
    public void approve(WorkflowContext context) {
        // 取消状态下不能审批
        throw new IllegalStateException("工作流已取消，不能审批");
    }

    /**
     * 取消状态下不能拒绝
     * @param context 工作流上下文
     */
    @Override
    public void reject(WorkflowContext context) {
        // 取消状态下不能拒绝
        throw new IllegalStateException("工作流已取消，不能拒绝");
    }

    /**
     * 取消状态下不能退回
     * @param context 工作流上下文
     */
    @Override
    public void returnToUser(WorkflowContext context) {
        // 取消状态下不能退回
        throw new IllegalStateException("工作流已取消，不能退回");
    }

    /**
     * 获取状态名称
     * @return 状态名称
     */
    @Override
    public String getStateName() {
        return CommonConstant.ORDER_STATUS_CANCEL;
    }
}