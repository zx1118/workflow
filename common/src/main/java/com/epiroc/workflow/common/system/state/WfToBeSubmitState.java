package com.epiroc.workflow.common.system.state;

import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.service.WfOperateService;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.util.OrderNoUtil;
import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.mapper.WfProcessMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 待提交状态
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-14
 */
public class WfToBeSubmitState implements WorkflowState {

    /**
     * 获取状态名称
     * @return 状态名称
     */
    @Override
    public String getStateName() {
        return CommonConstant.ORDER_STATUS_PENDING;
    }

    /**
     * 进行中状态下已经提交，不能再次提交
     * @param context 工作流上下文
     */
    @Override
    public Map<String, Object> submit(WorkflowContext context) {
        Map<String, Object> result = new HashMap<>();
        // 从待提交变为进行中，更改状态
        context.setState(new WfPendingState());
        OperateParam operateParam = context.getOperateParam();
        WfOperateService wfOperateService = context.getOperateService();
        WfOrder order = context.getOrder();
        /************** 提交流程 ****************/
        // 流程处理
        List<WfTaskParticipant> flowList = wfOperateService.dealSubmitFlow(operateParam, order.getId());
        WfTaskParticipant nextFlow = flowList.get(1);
        result.put("next", nextFlow);
        return result;
    }

    /**
     * 从进行中状态保存为草稿，转变为暂存状态
     * @param context 工作流上下文
     */
    @Override
    public void saveAsDraft(WorkflowContext context) {
        // 从进行中变为暂存，更改状态
        context.setState(new WfDraftState());
        // 更新数据库记录，设置工作流状态为暂存
    }

    /**
     * 从进行中状态取消，转变为已取消状态
     * @param context 工作流上下文
     */
    @Override
    public void cancel(WorkflowContext context) {
        // 从进行中变为已取消，更改状态
        context.setState(new WfCancelState());
        // 更新数据库记录，设置工作流状态为已取消
    }

    /**
     * 从进行中状态审批通过，如果是最后一个审批节点则变为已完成状态，否则保持进行中状态
     * @param context 工作流上下文
     */
    @Override
    public Map<String, Object> approve(WorkflowContext context) {
        // 检查是否是最后一个审批节点
        boolean isLastApprovalNode = checkIsLastApprovalNode(context);

        if (isLastApprovalNode) {
            // 如果是最后一个审批节点，变为已完成状态
            context.setState(new WfCompletedState());
            // 更新数据库记录，设置工作流状态为已完成
        } else {
            // 如果不是最后一个审批节点，保持进行中状态，但需要更新当前审批节点信息
            // 更新数据库记录，记录当前审批情况，进入下一个审批节点
        }
        return null;
    }

    /**
     * 从进行中状态拒绝，转变为已拒绝状态
     * @param context 工作流上下文
     */
    @Override
    public void reject(WorkflowContext context) {
        // 从进行中变为已拒绝，更改状态
        context.setState(new WfRejectState());
        // 更新数据库记录，设置工作流状态为已拒绝
    }

    /**
     * 从进行中状态退回，工作流仍然保持进行中状态，但需要退回到上一个节点
     * @param context 工作流上下文
     */
    @Override
    public void returnToUser(WorkflowContext context) {
        // 工作流仍然保持进行中状态，但需要退回到上一个节点
        // 更新数据库记录，记录退回情况，回到上一个节点
    }

    /**
     * 检查是否是最后一个审批节点
     * @param context 工作流上下文
     * @return 是否是最后一个审批节点
     */
    private boolean checkIsLastApprovalNode(WorkflowContext context) {
        // 这里需要根据实际情况实现判断逻辑
        // 例如，可以查询数据库中的工作流定义，检查当前节点是否是最后一个审批节点
        // 简单实现可以返回一个假值
        return false;
    }

}
