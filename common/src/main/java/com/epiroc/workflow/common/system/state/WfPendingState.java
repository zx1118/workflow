package com.epiroc.workflow.common.system.state;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epiroc.workflow.common.common.WorkflowResult;
import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.service.WfDictLoadService;
import com.epiroc.workflow.common.service.WfOperateService;
import com.epiroc.workflow.common.system.constant.ApproveTypeConstant;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.system.constant.TaskStatusConstant;
import com.epiroc.workflow.common.util.DateUtils;
import com.epiroc.workflow.common.util.oConvertUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 具体状态类-进行中状态
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-15
 */
public class WfPendingState implements WorkflowState {

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
        // 进行中状态下已经提交，不能再次提交
        // 可以不做任何操作，也可以抛出异常
        return null;
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
        Map<String, Object> resultMap = new HashMap<>();
        WfTask task = context.getCurrentTask();
        WfOrder order = context.getOrder();
        WfOperateService operateService = context.getOperateService();
        WfDictLoadService wfDictLoadService = context.getWfDictLoadService();
        try {
            // 更新当前任务状态
            task.setTaskStatus(wfDictLoadService.getTaskStatusCacheInfo().get(TaskStatusConstant.APPROVED)); // 已审批
            task.setApproveType(wfDictLoadService.getApproveTypeCacheInfo().get(ApproveTypeConstant.APPROVE)); // 同意
            task.setFinishTime(DateUtils.getDate());
            task.setUpdateTime(DateUtils.getDate());
            operateService.updateTaskById(task);
            // 查询整个流程
            List<WfTaskParticipant> wfTaskParticipantList = operateService.getFullFlow(order.getId());
            // 判断是否是最后一个审批节点
            boolean isLastNode = operateService.isLastApprovalNode(order.getId(), task.getId());
            resultMap.put("isLastNode", isLastNode);
            WfTaskParticipant current = oConvertUtils.entityToModel(task, WfTaskParticipant.class);
            operateService.updateCurrentTaskParticipant(current);
            if (isLastNode) {
                order.setFinishTime(DateUtils.getDate());
                order.setUpdateTime(DateUtils.getDate());
                // 如果是最后一个节点，将工作流状态变为已完成
                context.setState(new WfCompletedState());
                resultMap.put("create", wfTaskParticipantList.get(0));
            } else {
                // 如果不是最后一个节点，保持进行中状态，但创建下一个任务
                // 查询当前的 wf_task_participant
                WfTaskParticipant nextTaskParticipant = operateService.updateAndReturnNextTaskParticipant(current,
                        wfTaskParticipantList);
                resultMap.put("next", nextTaskParticipant);
                if (nextTaskParticipant != null) {
                    // 创建下一个任务
                    WfTask nextWfTask = oConvertUtils.entityToModel(nextTaskParticipant, WfTask.class);
                    operateService.saveTask(nextWfTask);
                }
            }
            resultMap.put("code", 200);
            resultMap.put("message", "审批通过");
            return resultMap;
        } catch (IllegalStateException e) {
            resultMap.put("code", 500);
            resultMap.put("message", "e.getMessage()");
            return resultMap;
        }
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

}