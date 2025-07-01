package com.epiroc.workflow.common.system.state;

import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.service.WfDictLoadService;
import com.epiroc.workflow.common.service.WfOperateService;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.system.constant.StateConstant;

import java.util.Map;

/**
 * 上下文-维护 WfOrder 实例及其状态
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-04-15
 */
public class WorkflowContext {

    private WorkflowState currentState;
    private WfOperateService operateService;
    private WfOrder order;
    private WfTask currentTask;
    private OperateParam operateParam;
    private WfDictLoadService wfDictLoadService;

    public WorkflowContext(WfOrder order) {
        this.order = order;
        // 根据数据库中的状态初始化当前状态
        setStateFromDatabase(order.getOrderStatus());
    }

    public WorkflowContext(WfOrder order, WfOperateService operateService) {
        this.order = order;
        this.operateService = operateService;
        // 根据数据库中的状态初始化当前状态
        setStateFromDatabase(order.getOrderStatus());
    }

    public WorkflowContext(WfOrder order, WfOperateService operateService, OperateParam operateParam) {
        this.order = order;
        this.operateService = operateService;
        this.operateParam = operateParam;
        // 根据数据库中的状态初始化当前状态
        setStateFromDatabase(order.getOrderStatus());
    }

    public WorkflowContext(WfOrder order, WfTask currentTask) {
        this.order = order;
        this.currentTask = currentTask;
        // 根据数据库中的状态初始化当前状态
        setStateFromDatabase(order.getOrderStatus());
    }

    public WorkflowContext(WfOrder order, WfTask currentTask, WfOperateService operateService, WfDictLoadService wfDictLoadService) {
        this.order = order;
        this.currentTask = currentTask;
        this.operateService = operateService;
        this.wfDictLoadService = wfDictLoadService;
        // 根据数据库中的状态初始化当前状态
        setStateFromDatabase(order.getOrderStatus());
    }

    // 设置状态
    public void setState(WorkflowState state) {
        this.currentState = state;
        // 更新数据库中的状态
        updateOrderStatus(state.getStateName());
    }

    // 初始化状态
    // TODO 修改为抽象工厂的方式
    private void setStateFromDatabase(String orderStatus) {
        switch (orderStatus) {
            case CommonConstant.ORDER_STATUS_TO_BE_SUBMIT:
                this.currentState = new WfToBeSubmitState();
                break;
            case CommonConstant.ORDER_STATUS_PENDING:
                this.currentState = new WfPendingState();
                break;
            case CommonConstant.ORDER_STATUS_COMPLETE:
                this.currentState = new WfCompletedState();
                break;
            case CommonConstant.ORDER_STATUS_CANCEL:
                this.currentState = new WfCancelState();
                break;
            case CommonConstant.ORDER_STATUS_REJECT:
                this.currentState = new WfRejectState();
                break;
            case CommonConstant.ORDER_STATUS_RETURN_TO_BE_SUBMITTED:
            case CommonConstant.ORDER_STATUS_STORAGE:
                this.currentState = new WfDraftState();
                break;
            default:
                // 默认为进行中状态
                this.currentState = new WfPendingState();
                break;
        }
    }

    // 更新数据库中的订单状态
    private void updateOrderStatus(String stateName) {
        if (order != null) {
            order.setOrderStatus(stateName);
            // 这里应该调用service层更新数据库
            operateService.updateOrderById(order);
        }
    }

    // 委托给当前状态的方法
    public Map<String, Object> submit() {
        return currentState.submit(this);
    }

    public void saveAsDraft() {
        currentState.saveAsDraft(this);
    }

    public void cancel() {
        currentState.cancel(this);
    }

    public Map<String, Object> approve() {
        return currentState.approve(this);
    }

    public void reject() {
        currentState.reject(this);
    }

    public void returnToUser() {
        currentState.returnToUser(this);
    }

    // Getter and Setter
    public WfOrder getOrder() {
        return order;
    }

    public void setOrder(WfOrder order) {
        this.order = order;
    }

    public WfTask getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(WfTask currentTask) {
        this.currentTask = currentTask;
    }

    public WorkflowState getCurrentState() {
        return currentState;
    }

    public OperateParam getOperateParam() {
        return operateParam;
    }

    public void setOperateParam(OperateParam operateParam) {
        this.operateParam = operateParam;
    }

    public WfOperateService getOperateService() {
        return operateService;
    }


    public WfDictLoadService getWfDictLoadService() {
        return wfDictLoadService;
    }

    public void setWfDictLoadService(WfDictLoadService wfDictLoadService) {
        this.wfDictLoadService = wfDictLoadService;
    }

}