package com.epiroc.workflow.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.enums.StateEnum;
import com.epiroc.workflow.common.enums.TaskStatusEnum;
import com.epiroc.workflow.common.enums.ApproveTypeEnum;
import com.epiroc.workflow.common.mapper.WfProcessMapper;
import com.epiroc.workflow.common.mapper.WfTaskParticipantMapper;
import com.epiroc.workflow.common.service.*;
import com.epiroc.workflow.common.system.constant.ApproveTypeConstant;
import com.epiroc.workflow.common.system.constant.TaskStatusConstant;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.system.query.QueryWrapperBuilder;
import com.epiroc.workflow.common.util.DateUtils;
import com.epiroc.workflow.common.util.IdUtil;
import com.epiroc.workflow.common.util.OrderNoUtil;
import com.epiroc.workflow.common.util.oConvertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 工作流操作服务类
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-18
 */
@Service
public class WfOperateServiceImpl implements WfOperateService, WorkflowConstant {

    @Resource
    private WfOrderService wfOrderService;

    @Resource
    private WfProcessMapper wfProcessMapper;

    @Resource
    private ParamService paramService;

    @Resource
    private WfTaskParticipantService wfTaskParticipantService;

    @Resource
    private WfTaskService wfTaskService;

    @Resource
    private WfDictLoadService wfDictLoadService;

    @Resource
    private WfToolService wfToolService;

    @Override
    public WfOrder operateOrderAndParam(OperateParam operateParam) {
        WfProcess process = operateParam.getWfProcess();
        String paramId = operateParam.getParamId();
        if(oConvertUtils.isEmpty(paramId)){
            // form 表单参数插入
            paramId = paramService.insertParam(process.getClassName(), operateParam.getParam(),
                    operateParam.getParamList(), operateParam.getIdFieldName());
        }
        String orderStatus = operateParam.getOrderStatus();
        new WfOrder();
        WfOrder wfOrder;
        // 查询 wf_order
        if(oConvertUtils.isNotEmpty(operateParam.getOrderId())){
            wfOrder = wfOrderService.getById(operateParam.getOrderId());
            wfOrder.setUpdateBy(operateParam.getCreatorEmail());
            wfOrder.setUpdateTime(DateUtils.getDate());
        } else {
            wfOrder = oConvertUtils.entityToModel(operateParam, WfOrder.class);
            assert wfOrder != null;
            wfOrder.setProcessId(process.getId());
            if (oConvertUtils.isNotEmpty(paramId)) wfOrder.setBusinessKey(paramId);
            if(wfDictLoadService.getOrderStatusCacheInfo().containsKey(orderStatus)){
                wfOrder.setOrderStatus(wfDictLoadService.getOrderStatusCacheInfo().get(orderStatus));
            }
            // 创建订单编号
            String orderNo = OrderNoUtil.generateOrderNo(process.getOrderNoPre(), process.getOrderNoLength());
            wfOrder.setRequestDate(DateUtils.getDate());
            wfOrder.setOrderNo(orderNo);
            wfOrder.setCreateBy(operateParam.getCreatorEmail());
            wfOrder.setCreateTime(DateUtils.getDate());
            wfOrder.setUpdateBy(operateParam.getCreatorEmail());
            wfOrderService.save(wfOrder);
        }
        return wfOrder;
    }

    /**
     * 通过实体类中的 ID 更新 wf_order
     * @param order
     */
    @Override
    public void updateOrderById(WfOrder order) {
        wfOrderService.updateById(order);
    }

    @Override
    public WfProcess queryWfProcessOne(WfProcess wfProcess) {
        if (wfProcess != null) {
            QueryWrapper<WfProcess> wrapper = QueryWrapperBuilder.buildQueryWrapper(wfProcess);
            return wfProcessMapper.selectOne(wrapper);
        }
        return null;
    }

    @Override
    public WfProcess getWfProcessById(Integer wfProcessId) {
        return wfProcessMapper.selectById(wfProcessId);
    }

    @Override
    public WfProcess getWfProcessByOperateParam(OperateParam operateParam) {
        WfProcess wfProcess = null;
        if(oConvertUtils.isEmpty(operateParam.getWfProcessId())){
            wfProcess = queryWfProcessOne(oConvertUtils.entityToModel(operateParam, WfProcess.class));
        } else {
            wfProcess = getWfProcessById(operateParam.getWfProcessId());
        }
        return wfProcess;
    }

    @Override
    public List<WfTaskParticipant> dealSubmitFlow(OperateParam operateParam, Integer orderId) {
        List<WfTaskParticipant> flowList = operateParam.getFlowList();
        // 参数校验
        if (flowList == null || flowList.isEmpty()) {
            throw new IllegalArgumentException("流程列表不能为空");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        
        // 获取当前时间
        java.util.Date currentTime = DateUtils.getDate();
        
        // 处理流程参与者
        for (int i = 0; i < flowList.size(); i++) {
            WfTaskParticipant participant = flowList.get(i);
            
            // 设置基础信息
            participant.setOrderId(orderId);
            participant.setCreateTime(currentTime);
            participant.setUpdateTime(currentTime);
            
            // 处理操作员ID
            processOperatorIds(participant);
            
            // 根据索引设置任务状态
            setTaskStatusByIndex(participant, i, currentTime);
        }
        
        // 批量保存流程参与者
        wfTaskParticipantService.saveBatch(flowList);
        
        // 创建任务 - 只为前两个流程创建任务
        createTasksForFlow(flowList);
        
        return flowList;
    }

    @Override
    public boolean updateTaskById(WfTask task) {
        return wfTaskService.updateById(task);
    }

    @Override
    public List<WfTaskParticipant> getFullFlow(Integer orderId) {
        return wfTaskParticipantService.getFullFlow(orderId);
    }

    @Override
    public WfTaskParticipant updateCurrentTaskParticipant(WfTaskParticipant current) {
        return wfTaskParticipantService.updateCurrent(current);
    }

    @Override
    public WfTaskParticipant updateAndReturnNextTaskParticipant(WfTaskParticipant current, List<WfTaskParticipant> wfTaskParticipantList) {
        return wfTaskParticipantService.updateAndReturnNext(current, wfTaskParticipantList);
    }

    @Override
    public boolean saveTask(WfTask nextWfTask) {
        return wfTaskService.save(nextWfTask);
    }

    @Override
    public boolean isLastApprovalNode(Integer orderId, Integer taskId) {
        return wfToolService.isLastApprovalNode(orderId, taskId);
    }

    /**
     * 处理操作员ID字符串
     */
    private void processOperatorIds(WfTaskParticipant participant) {
        String operatorIdStr = participant.getOperatorId();
        if (oConvertUtils.isEmpty(operatorIdStr)) {
            return;
        }
        
        // 使用Stream API优化ID处理
        String processedIds = Arrays.stream(operatorIdStr.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(IdUtil::resetGuidStr)
                .collect(java.util.stream.Collectors.joining(","));
        
        participant.setOperatorId(processedIds);
    }
    
    /**
      * 根据索引设置任务状态
      */
     private void setTaskStatusByIndex(WfTaskParticipant participant, int index, java.util.Date currentTime) {
         switch (index) {
             case 0:
                 // 第一个任务：已审批
                 participant.setTaskStatus(wfDictLoadService.getTaskStatusCacheInfo().get(TaskStatusConstant.APPROVED));
                 participant.setApproveType(wfDictLoadService.getApproveTypeCacheInfo().get(ApproveTypeConstant.APPROVE));
                 participant.setFinishTime(currentTime);
                 participant.setApprover(participant.getOperator());
                 participant.setApproverId(participant.getOperatorId());
                 participant.setApproverEmail(participant.getOperatorEmail());
                 break;
             case 1:
                 // 第二个任务：等待审批
                 participant.setTaskStatus(wfDictLoadService.getTaskStatusCacheInfo().get(TaskStatusConstant.WAITING));
                 participant.setSortOrder((index + 1) * FLOW_SORT_ORDER_CONSTANT);
                 break;
             default:
                 // 其他任务：未审批
                 participant.setTaskStatus(wfDictLoadService.getTaskStatusCacheInfo().get(TaskStatusConstant.NOT_APPROVED));
                 break;
         }
     }
    
    /**
     * 为流程创建任务
     */
    private void createTasksForFlow(List<WfTaskParticipant> flowList) {
        // 确保至少有两个流程参与者
        if (flowList.size() < 2) {
            throw new IllegalStateException("流程列表至少需要包含两个参与者");
        }
        
        // 创建第一个任务（已完成）
        WfTask firstTask = createTaskFromParticipant(flowList.get(0));
        wfTaskService.save(firstTask);
        
        // 创建第二个任务（待处理）
        WfTask secondTask = createTaskFromParticipant(flowList.get(1));
        wfTaskService.save(secondTask);
        
        // 更新第二个参与者的updateBy字段
        flowList.get(1).setUpdateBy(secondTask.getId().toString());
    }
    
    /**
     * 从参与者创建任务
     */
    private WfTask createTaskFromParticipant(WfTaskParticipant participant) {
        WfTask task = oConvertUtils.entityToModel(participant, WfTask.class);
        if (task == null) {
            throw new IllegalStateException("无法从参与者创建任务");
        }
        task.setWtpId(participant.getId());
        return task;
    }

}
