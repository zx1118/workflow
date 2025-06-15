package com.epiroc.workflow.common.system.state;

/**
 * 工作流状态接口
 * <p>
 * 定义工作流各个状态下可执行的操作。工作流有以下状态：
 * <ul>
 *     <li>进行中 (Pending) - 工作流正在审批过程中</li>
 *     <li>暂存 (Draft) - 工作流被保存为草稿</li>
 *     <li>已取消 (Cancel) - 工作流被用户取消</li>
 *     <li>已拒绝 (Reject) - 工作流被审批人拒绝</li>
 *     <li>已完成 (Complete) - 工作流审批完成</li>
 * </ul>
 * <p>
 * 不同角色可以执行的操作：
 * <ul>
 *     <li>用户：提交、暂存、取消</li>
 *     <li>审批人：同意、拒绝、退回</li>
 * </ul>
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-15
 */
public interface WorkflowState {

    /**
     * 提交工作流
     * <p>
     * 用户操作，将工作流提交到审批流程中。
     * <ul>
     *     <li>暂存状态：可以提交，变为进行中状态</li>
     *     <li>进行中状态：已经提交，不需要操作</li>
     *     <li>其他状态：不能提交，抛出异常</li>
     * </ul>
     *
     * @param context 工作流上下文
     */
    void submit(WorkflowContext context);

    /**
     * 保存工作流为草稿
     * <p>
     * 用户操作，将工作流保存为草稿，不进入审批流程。
     * <ul>
     *     <li>进行中状态：可以保存为草稿，变为暂存状态</li>
     *     <li>暂存状态：已经是草稿，不需要操作</li>
     *     <li>其他状态：不能保存为草稿，抛出异常</li>
     * </ul>
     *
     * @param context 工作流上下文
     */
    void saveAsDraft(WorkflowContext context);

    /**
     * 取消工作流
     * <p>
     * 用户操作，取消当前工作流。
     * <ul>
     *     <li>进行中状态：可以取消，变为已取消状态</li>
     *     <li>暂存状态：可以取消，变为已取消状态</li>
     *     <li>已拒绝状态：可以取消，变为已取消状态</li>
     *     <li>已取消状态：已经取消，不需要操作</li>
     *     <li>已完成状态：不能取消，抛出异常</li>
     * </ul>
     *
     * @param context 工作流上下文
     */
    void cancel(WorkflowContext context);

    /**
     * 审批通过工作流
     * <p>
     * 审批人操作，同意当前审批节点。
     * <ul>
     *     <li>进行中状态：
     *       <ul>
     *         <li>如果是最后一个审批节点，变为已完成状态</li>
     *         <li>如果不是最后一个审批节点，保持进行中状态，但更新当前节点</li>
     *       </ul>
     *     </li>
     *     <li>其他状态：不能审批，抛出异常</li>
     * </ul>
     *
     * @param context 工作流上下文
     */
    void approve(WorkflowContext context);

    /**
     * 拒绝工作流
     * <p>
     * 审批人操作，拒绝当前审批节点，工作流结束。
     * <ul>
     *     <li>进行中状态：可以拒绝，变为已拒绝状态</li>
     *     <li>已拒绝状态：已经拒绝，不需要操作</li>
     *     <li>其他状态：不能拒绝，抛出异常</li>
     * </ul>
     *
     * @param context 工作流上下文
     */
    void reject(WorkflowContext context);

    /**
     * 退回工作流
     * <p>
     * 审批人操作，将工作流退回到上一个节点或退回给申请人。
     * <ul>
     *     <li>进行中状态：可以退回，保持进行中状态，但更新当前节点为上一个节点</li>
     *     <li>其他状态：不能退回，抛出异常</li>
     * </ul>
     *
     * @param context 工作流上下文
     */
    void returnToUser(WorkflowContext context);

    /**
     * 获取当前状态名称
     * <p>
     * 返回状态对应的字符串常量，用于更新数据库。
     *
     * @return 状态名称常量
     */
    String getStateName();
}