package com.epiroc.workflow.common.service;

import com.epiroc.workflow.common.common.Result;
import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.param.OperateParam;

/**
 * 工作流状态管理服务接口
 * 基于状态模式管理工作流的状态转换
 */
public interface WorkflowStateService {

    /**
     * 提交工作流
     * @param order 工作流订单
     * @return 操作结果
     */
    Result submit(WfOrder order);

    /**
     * 提交工作流
     * @param order 工作流订单
     * @return 操作结果
     */
    Result submit(WfOrder order, OperateParam operateParam);

    /**
     * 保存为草稿
     * @param order 工作流订单
     * @return 操作结果
     */
    Result saveAsDraft(WfOrder order);

    /**
     * 从草稿提交工作流
     * @param order 工作流订单
     * @return 操作结果
     */
    Result submitFromDraft(WfOrder order, OperateParam operateParam);

    /**
     * 取消工作流
     * @param order 工作流订单
     * @return 操作结果
     */
    Result cancel(WfOrder order);

    /**
     * 审批通过
     * @param order 工作流订单
     * @param task 当前任务
     * @return 操作结果
     */
    Result approve(WfOrder order, WfTask task);

    /**
     * 拒绝工作流
     * @param order 工作流订单
     * @param task 当前任务
     * @return 操作结果
     */
    Result reject(WfOrder order, WfTask task);

    /**
     * 退回工作流
     * @param order 工作流订单
     * @param task 当前任务
     * @return 操作结果
     */
    Result returnToUser(WfOrder order, WfTask task);

    /**
     * 检查是否是最后一个审批节点
     * @param orderId 订单ID
     * @param currentTaskId 当前任务ID
     * @return 是否是最后一个审批节点
     */
    boolean isLastApprovalNode(Integer orderId, Integer currentTaskId);

}