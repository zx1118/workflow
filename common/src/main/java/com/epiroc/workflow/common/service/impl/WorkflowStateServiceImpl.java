package com.epiroc.workflow.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epiroc.workflow.common.common.WorkflowResult;
import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.service.*;
import com.epiroc.workflow.common.system.constant.ApproveTypeConstant;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.system.constant.StateConstant;
import com.epiroc.workflow.common.system.constant.TaskStatusConstant;
import com.epiroc.workflow.common.system.state.WfCompletedState;
import com.epiroc.workflow.common.system.state.WfRejectState;
import com.epiroc.workflow.common.system.state.WorkflowContext;
import com.epiroc.workflow.common.util.DateUtils;
import com.epiroc.workflow.common.util.WorkflowUtil;
import com.epiroc.workflow.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流状态管理服务实现类
 * 基于状态模式管理工作流的状态转换
 */
@Service
public class WorkflowStateServiceImpl implements WorkflowStateService, TaskStatusConstant, StateConstant, ApproveTypeConstant {

    @Resource
    private WfOrderService wfOrderService;

    @Resource
    private WfTaskService wfTaskService;

    @Resource
    private WfTaskParticipantService wfTaskParticipantService;

    @Resource
    private WfOperateService operateService;

    @Resource
    private WfDictLoadService wfDictLoadService;

    /**
     * 提交工作流
     * @param order 工作流订单
     * @return 操作结果
     */
    @Override
    @Transactional
    public WorkflowResult submit(WfOrder order) {
        WorkflowContext context = new WorkflowContext(order, operateService);
        try {
            context.submit();
            return WorkflowResult.ok("工作流提交成功");
        } catch (IllegalStateException e) {
            return WorkflowResult.error(e.getMessage());
        }
    }


    /**
     * 提交工作流
     * @param order 工作流订单
     * @param operateParam 操作参数
     * @return 操作结果
     */
    @Override
    @Transactional
    public WorkflowResult submit(WfOrder order, OperateParam operateParam) {
        WorkflowContext context = new WorkflowContext(order, operateService, operateParam);
        try {
            return WorkflowResult.ok("工作流提交成功", context.submit());
        } catch (IllegalStateException e) {
            return WorkflowResult.error(e.getMessage());
        }
    }

    /**
     * 保存为草稿
     * @param order 工作流订单
     * @return 操作结果
     */
    @Override
    @Transactional
    public WorkflowResult saveAsDraft(WfOrder order) {
        WorkflowContext context = new WorkflowContext(order);
        try {
            context.saveAsDraft();
            // 更新订单状态为暂存
            order.setOrderStatus(CommonConstant.ORDER_STATUS_RETURN_TO_BE_SUBMITTED);
            wfOrderService.updateById(order);
            return WorkflowResult.ok("工作流已保存为草稿");
        } catch (IllegalStateException e) {
            return WorkflowResult.error(e.getMessage());
        }
    }

    /**
     * 从草稿提交工作流
     * @param order 工作流订单
     * @return 操作结果
     */
    @Override
    public WorkflowResult submitFromDraft(WfOrder order, OperateParam operateParam) {
        WorkflowContext context = new WorkflowContext(order);
        try {
            context.submit();
            // 更新订单状态
            order.setOrderStatus(CommonConstant.ORDER_STATUS_RETURN_TO_BE_SUBMITTED);
            wfOrderService.updateById(order);
            return WorkflowResult.ok("工作流已提交");
        } catch (IllegalStateException e) {
            return WorkflowResult.error(e.getMessage());
        }
    }

    /**
     * 取消工作流
     * @param order 工作流订单
     * @return 操作结果
     */
    @Override
    @Transactional
    public WorkflowResult cancel(WfOrder order) {
        WorkflowContext context = new WorkflowContext(order);
        try {
            context.cancel();
            // 更新订单状态为已取消
            order.setOrderStatus(CommonConstant.ORDER_STATUS_CANCEL);
            wfOrderService.updateById(order);

            // 更新相关任务状态
            QueryWrapper<WfTask> taskQueryWrapper = new QueryWrapper<>();
            taskQueryWrapper.eq("order_id", order.getId()).eq("task_status", "1"); // 等待操作的任务
            List<WfTask> activeTasks = wfTaskService.list(taskQueryWrapper);

            for (WfTask task : activeTasks) {
                task.setTaskStatus("3"); // 取消
                task.setApproveType("2"); // 取消
                task.setFinishTime(DateUtils.getDate());
                task.setUpdateTime(DateUtils.getDate());
                wfTaskService.updateById(task);
            }

            return WorkflowResult.ok("工作流已取消");
        } catch (IllegalStateException e) {
            return WorkflowResult.error(e.getMessage());
        }
    }

    /**
     * 审批通过
     * @param order 工作流订单
     * @param task 当前任务
     * @return 操作结果
     */
    @Override
    @Transactional
    public WorkflowResult approve(WfOrder order, WfTask task) {
        WorkflowContext context = new WorkflowContext(order, task, operateService, wfDictLoadService);
        try {
            return WorkflowResult.ok("审批通过", context.approve());
        } catch (IllegalStateException e) {
            return WorkflowResult.error(e.getMessage());
        }
    }

    private List<WfTaskParticipant> getWfTaskParticipants(WfTask task) {
        QueryWrapper<WfTaskParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", task.getOrderId()).orderByAsc("sort_order");
        List<WfTaskParticipant> wfTaskParticipantList = wfTaskParticipantService.list(queryWrapper);
        return wfTaskParticipantList;
    }

    /**
     * 拒绝工作流
     * @param order 工作流订单
     * @param task 当前任务
     * @return 操作结果
     */
    @Override
    @Transactional
    public WorkflowResult reject(WfOrder order, WfTask task) {
        WorkflowContext context = new WorkflowContext(order, task);
        try {
            context.setState(new WfRejectState());

            // 更新当前任务状态
            task.setTaskStatus("2"); // 已审批
            task.setApproveType("1"); // 拒绝
            task.setFinishTime(DateUtils.getDate());
            task.setUpdateTime(DateUtils.getDate());
            wfTaskService.updateById(task);

            // 更新订单状态为拒绝
            order.setOrderStatus(CommonConstant.ORDER_STATUS_REJECT);
            order.setFinishTime(DateUtils.getDate());
            wfOrderService.updateById(order);

            return WorkflowResult.ok("已拒绝工作流");
        } catch (IllegalStateException e) {
            return WorkflowResult.error(e.getMessage());
        }
    }

    /**
     * 退回工作流
     * @param order 工作流订单
     * @param task 当前任务
     * @return 操作结果
     */
    @Override
    @Transactional
    public WorkflowResult returnToUser(WfOrder order, WfTask task) {
        WorkflowContext context = new WorkflowContext(order, task);
        try {
            context.returnToUser();

            // 更新当前任务状态
            task.setTaskStatus("2"); // 已审批
            task.setApproveType("2"); // 退回
            task.setFinishTime(DateUtils.getDate());
            task.setUpdateTime(DateUtils.getDate());
            wfTaskService.updateById(task);

            // 更新订单状态为退回
            order.setOrderStatus(CommonConstant.ORDER_STATUS_RETURN);
            wfOrderService.updateById(order);

            // 查询整个流程
            QueryWrapper<WfTaskParticipant> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id", task.getOrderId()).orderByAsc("sort_order");
            List<WfTaskParticipant> wfTaskParticipantList = wfTaskParticipantService.list(queryWrapper);

            // 找到创建者任务
            WfTaskParticipant creatorTask = null;
            for (WfTaskParticipant participant : wfTaskParticipantList) {
                if (participant.getSortOrder() == 0) { // 假设创建者排序为0
                    creatorTask = participant;
                    break;
                }
            }

            if (creatorTask != null) {
                // 创建回退到创建者的任务
                WfTask returnTask = new WfTask();
                returnTask.setOrderId(task.getOrderId());
                returnTask.setName(creatorTask.getName());
                returnTask.setDisplayName(creatorTask.getDisplayName());
                returnTask.setOperator(creatorTask.getOperator());
                returnTask.setOperatorId(creatorTask.getOperatorId());
                returnTask.setOperatorEmail(creatorTask.getOperatorEmail());
                returnTask.setTaskStatus("1"); // 等待操作
                returnTask.setWtpId(creatorTask.getId());
                returnTask.setParentTaskId(task.getId().toString());
                returnTask.setCreateTime(DateUtils.getDate());
                wfTaskService.save(returnTask);
            }

            return WorkflowResult.ok("已退回工作流");
        } catch (IllegalStateException e) {
            return WorkflowResult.error(e.getMessage());
        }
    }

    /**
     * 检查是否是最后一个审批节点
     * @param orderId 订单ID
     * @param currentTaskId 当前任务ID
     * @return 是否是最后一个审批节点
     */
    @Override
    public boolean isLastApprovalNode(Integer orderId, Integer currentTaskId) {
        // 查询当前任务
        WfTask currentTask = wfTaskService.getById(currentTaskId);
        if (currentTask == null) {
            return false;
        }

        // 查询当前任务对应的参与者
        WfTaskParticipant currentParticipant = wfTaskParticipantService.getById(currentTask.getWtpId());
        if (currentParticipant == null) {
            return false;
        }

        // 查询整个流程的所有参与者
        QueryWrapper<WfTaskParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId).orderByAsc("sort_order");
        List<WfTaskParticipant> allParticipants = wfTaskParticipantService.list(queryWrapper);

        // 使用工作流工具判断是否为最后一个节点
        return WorkflowUtil.isLastElement(allParticipants, currentParticipant);
    }


//    @Override
//    @Transactional
//    public WorkflowResult approve(WfOrder order, WfTask task) {
//        Map<String, Object> resultMap = new HashMap<>();
//        try {
//            // 更新当前任务状态
//            task.setTaskStatus(wfDictLoadService.getTaskStatusCacheInfo().get(APPROVED)); // 已审批
//            task.setApproveType(wfDictLoadService.getApproveTypeCacheInfo().get(APPROVE)); // 同意
//            task.setFinishTime(DateUtils.getDate());
//            task.setUpdateTime(DateUtils.getDate());
//            wfTaskService.updateById(task);
//            // 判断是否是最后一个审批节点
//            boolean isLastNode = isLastApprovalNode(order.getId(), task.getId());
//            resultMap.put("isLastNode", isLastNode);
//            // 查询整个流程
//            List<WfTaskParticipant> wfTaskParticipantList = wfTaskParticipantService.getFullFlow(order.getId());
//            WfTaskParticipant current = oConvertUtils.entityToModel(task, WfTaskParticipant.class);
//            wfTaskParticipantService.updateCurrent(current);
//            if (isLastNode) {
//                order.setFinishTime(DateUtils.getDate());
//                order.setUpdateTime(DateUtils.getDate());
//                WorkflowContext context = new WorkflowContext(order, task, operateService);
//                // 如果是最后一个节点，将工作流状态变为已完成
//                context.setState(new WfCompletedState());
//            } else {
//                // 如果不是最后一个节点，保持进行中状态，但创建下一个任务
//                // 查询当前的 wf_task_participant
//                WfTaskParticipant nextTaskParticipant = wfTaskParticipantService.updateAndReturnNext(current,
//                        wfTaskParticipantList);
//                resultMap.put("next", nextTaskParticipant);
//                if (nextTaskParticipant != null) {
//                    // 创建下一个任务
//                    WfTask nextWfTask = oConvertUtils.entityToModel(nextTaskParticipant, WfTask.class);
//                    wfTaskService.save(nextWfTask);
//                }
//            }
//            return WorkflowResult.ok("审批通过");
//        } catch (IllegalStateException e) {
//            return WorkflowResult.error(e.getMessage());
//        }
//    }

}