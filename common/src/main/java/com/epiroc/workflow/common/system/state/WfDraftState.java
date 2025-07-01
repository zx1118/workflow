package com.epiroc.workflow.common.system.state;

import com.epiroc.workflow.common.system.constant.CommonConstant;

import java.util.Map;

/**
 * 具体状态类-暂存状态
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-15
 */
public class WfDraftState implements WorkflowState {

    /**
     * 从暂存状态提交，转变为进行中状态
     * @param context 工作流上下文
     */
    @Override
    public Map<String, Object> submit(WorkflowContext context) {
        // 从暂存状态提交，转变为进行中状态
        context.setState(new WfPendingState());
        // 这里需要更新数据库，设置工作流状态为进行中
        // 更新当前工作流的orderStatus
        // 创建相应的任务记录等
        return null;
    }

    /**
     * 已经是暂存状态，不需要操作
     * @param context 工作流上下文
     */
    @Override
    public void saveAsDraft(WorkflowContext context) {
        // 已经是暂存状态，不需要操作
    }

    /**
     * 从暂存状态取消，转变为已取消状态
     * @param context 工作流上下文
     */
    @Override
    public void cancel(WorkflowContext context) {
        // 从暂存状态取消，转变为已取消状态
        context.setState(new WfCancelState());
        // 更新数据库，设置工作流状态为已取消
    }

    /**
     * 暂存状态下不能审批
     * @param context 工作流上下文
     */
    @Override
    public Map<String, Object> approve(WorkflowContext context) {
        // 暂存状态下不能审批
        throw new IllegalStateException("工作流处于暂存状态，不能审批");
    }

    /**
     * 暂存状态下不能拒绝
     * @param context 工作流上下文
     */
    @Override
    public void reject(WorkflowContext context) {
        // 暂存状态下不能拒绝
        throw new IllegalStateException("工作流处于暂存状态，不能拒绝");
    }

    /**
     * 暂存状态下不能退回
     * @param context 工作流上下文
     */
    @Override
    public void returnToUser(WorkflowContext context) {
        // 暂存状态下不能退回
        throw new IllegalStateException("工作流处于暂存状态，不能退回");
    }

    /**
     * 获取状态名称
     * @return 状态名称
     */
    @Override
    public String getStateName() {
        return CommonConstant.ORDER_STATUS_PENDING; // 假设暂存状态的常量为ORDER_STATUS_PENDING
    }
}