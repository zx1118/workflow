package com.epiroc.workflow.common.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epiroc.workflow.common.common.WorkflowResult;
import com.epiroc.workflow.common.entity.*;
import com.epiroc.workflow.common.entity.form.ApproveForm;
import com.epiroc.workflow.common.entity.form.TaskForm;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.entity.param.BatchOperateParam;
import com.epiroc.workflow.common.service.*;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.system.constant.OperateConstant;
import com.epiroc.workflow.common.system.constant.StateConstant;
import com.epiroc.workflow.common.system.factory.OperateAbstractHandler;
import com.epiroc.workflow.common.system.factory.OperateFactory;
import com.epiroc.workflow.common.system.factory.OperateBatchSubmitHandler;
import com.epiroc.workflow.common.util.*;
import com.epiroc.workflow.common.convert.WfFlow2WfTaskParticipant;
import com.epiroc.workflow.common.entity.form.GetFlowForm;
import com.epiroc.workflow.common.entity.form.WfSubmitForm;
import com.epiroc.workflow.common.mapper.WfFlowMapper;
import com.epiroc.workflow.common.mapper.WfProcessMapper;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.system.flow.FlowContext;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import com.epiroc.workflow.common.system.rule.RuleContext;
import com.epiroc.workflow.common.system.rule.model.RulePair;
import com.epiroc.workflow.common.system.rule.model.RuleParam;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkflowServiceImpl implements WorkflowService, WorkflowConstant, CommonConstant {

    @Resource
    private WfProcessService wfProcessService;

    @Resource
    private WfFlowService wfFlowService;

    @Resource
    private ParamService paramService;

    @Resource
    private WfRuleService wfRuleService;

    @Resource
    private WfFlowMapper wfFlowMapper;

    @Resource
    private WfProcessMapper wfProcessMapper;

    @Resource
    private WfTaskParticipantService wfTaskParticipantService;

    @Resource
    private WfOrderService wfOrderService;

    @Resource
    private WorkflowDynamicService workflowDynamicService;

    @Resource
    private WorkflowStateService workflowStateService;


    @Resource
    private WfOperateService wfOperateService;

    @Resource
    private WfDictLoadService wfDictLoadService;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 工作流处理
     *
     * 根据order_status判断当前的状态，根据当前状态和操作类型判断要做的操作，
     * 例如：当前order_status为：暂存，操作类型为提交，则执行提交操作，并返回下一个审批人信息
     * @param operateParam
     * @return
     */
    @Override
    public Map<String, Object> operateByHandler(OperateParam operateParam) {
        WfProcess wfProcess = null;
        // 获取流程定义
        if(oConvertUtils.isEmpty(operateParam.getWfProcess())){
            wfProcess = wfProcessService.getWfProcessByOperateParam(operateParam);
        } else {
            wfProcess = operateParam.getWfProcess();
        }
        if (oConvertUtils.isEmpty(wfProcess)) {
            String message = "流程定义不存在";
        }
        operateParam.setWfProcess(wfProcess);
        WfOrder wfOrder = wfOperateService.operateOrderAndParam(operateParam);
        // 获取操作类型
        String operateKey = StringUtil.joinWithChar(operateParam.getOrderStatus(), operateParam.getOperateType(),
                StrUtil.DASHED);
        // 根据当前状态和操作类型执行相应操作,使用WorkflowStateService管理状态
        OperateAbstractHandler invokeStrategy = OperateFactory.getInvokeStrategy(operateKey);
        return invokeStrategy.handle(wfOrder, operateParam);
    }

    /**
     * 工作流处理
     *
     * 根据order_status判断当前的状态，根据当前状态和操作类型判断要做的操作，
     * 例如：当前order_status为：暂存，操作类型为提交，则执行提交操作，并返回下一个审批人信息
     * @param operateParam
     * @return
     */
    @Override
    public WorkflowResult operate(OperateParam operateParam) {
        Integer orderId = operateParam.getOrderId();
        // 查询 wf_order
        WfOrder wfOrder = wfOrderService.getById(orderId);
        wfOrder.setUpdateBy(operateParam.getOperatorId());
        wfOrder.setUpdateTime(DateUtils.getDate());
        if (oConvertUtils.isEmpty(wfOrder)) {
            return WorkflowResult.error("工作流订单不存在");
        }
        // 获取当前状态
        String orderStatus = wfOrder.getOrderStatus();
        // 获取操作类型
        String operateType = operateParam.getOperateType();
        // 根据当前状态和操作类型执行相应操作,使用WorkflowStateService管理状态
        switch (orderStatus) {
            // 暂存状态
            case ORDER_STATUS_RETURN_TO_BE_SUBMITTED:
                if ("submit".equals(operateType)) {
                    // 暂存状态 -> 提交操作
                    return workflowStateService.submitFromDraft(wfOrder, operateParam);
                } else if ("cancel".equals(operateType)) {
                    // 暂存状态 -> 取消操作
                    return workflowStateService.cancel(wfOrder);
                } else {
                    return WorkflowResult.error("暂存状态下不支持的操作类型: " + operateType);
                }
                // 进行中状态
            case ORDER_STATUS_PENDING:
//                if ("approve".equals(operateType)) {
//                    // 进行中状态 -> 审批通过操作
//                    return approveTask(wfOrder, operateParam);
//                } else if ("reject".equals(operateType)) {
//                    // 进行中状态 -> 拒绝操作
//                    return rejectTask(wfOrder, operateParam);
//                } else if ("return".equals(operateType)) {
//                    // 进行中状态 -> 退回操作
//                    return returnTask(wfOrder, operateParam);
//                } else if ("cancel".equals(operateType)) {
//                    // 进行中状态 -> 取消操作
//                    return workflowStateService.cancel(wfOrder);
//                } else if ("save_draft".equals(operateType)) {
//                    // 进行中状态 -> 保存为草稿
//                    return workflowStateService.saveAsDraft(wfOrder);
//                } else {
//                    return Result.error("进行中状态下不支持的操作类型: " + operateType);
//                }
                // 已完成状态
            case ORDER_STATUS_COMPLETE:
                return WorkflowResult.error("工作流已完成，不能执行任何操作");
            // 已拒绝状态
            case ORDER_STATUS_REJECT:
                if ("cancel".equals(operateType)) {
                    // 已拒绝状态 -> 取消操作
                    return workflowStateService.cancel(wfOrder);
                } else {
                    return WorkflowResult.error("已拒绝状态下不支持的操作类型: " + operateType);
                }
                // 已取消状态
            case ORDER_STATUS_CANCEL:
                return WorkflowResult.error("工作流已取消，不能执行任何操作");
            // 退回状态
            case ORDER_STATUS_RETURN:
                if ("submit".equals(operateType)) {
                    // 退回状态 -> 重新提交
//                    return submitFromReturn(wfOrder, operateParam);
                } else if ("cancel".equals(operateType)) {
                    // 退回状态 -> 取消操作
                    return workflowStateService.cancel(wfOrder);
                } else {
                    return WorkflowResult.error("退回状态下不支持的操作类型: " + operateType);
                }
            default:
                return WorkflowResult.error("未知的工作流状态: " + orderStatus);
        }

//        return null;
    }

    /**
     * 从暂存状态提交工作流
     * @param wfOrder 工作流订单
     * @param operateParam 操作参数
     * @return 操作结果
     */
    private WorkflowResult submitFromDraft(WfOrder wfOrder, OperateParam operateParam) {
        // 更新订单信息
        wfOrder.setOrderStatus(CommonConstant.ORDER_STATUS_PENDING);
        wfOrderService.updateById(wfOrder);

        // 调用工作流状态服务提交
        WorkflowResult submitWorkflowResult = workflowStateService.submit(wfOrder, operateParam);
        if (!submitWorkflowResult.isSuccess()) {
            return submitWorkflowResult;
        }

        // 处理流程，创建任务
        if (operateParam.getFlowList() != null && !operateParam.getFlowList().isEmpty()) {
            List<WfTaskParticipant> flowList = wfFlowService.dealSubmitFlow(operateParam.getFlowList(), wfOrder.getId());

            // 返回下一个审批人信息
            if (flowList != null && flowList.size() > 1) {
                WfTaskParticipant next = flowList.get(1);
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("nextApprover", next);
                return WorkflowResult.ok(resultMap);
            }
        }

        return WorkflowResult.ok("提交成功");
    }

    /**
     * 获取流程信息
     * @param getFlowForm
     * @return
     */
    @Override
    public WorkflowResult getFlow(GetFlowForm getFlowForm) {
        // 获取流程定义
        WfProcess wfProcess = wfProcessService.getById(getFlowForm.getWfProcessId());
        if (oConvertUtils.isEmpty(wfProcess)) {
            return WorkflowResult.error("流程定义不存在");
        }
        FlowContext flowContext = new FlowContext(wfProcess.getFlowTypes(), wfFlowService);
        // 流程参数
        FlowParam flowParam = oConvertUtils.entityToModel(getFlowForm, FlowParam.class);
        // 获取流程信息
        Map<String, Object> flowInfoMap = flowContext.getFlowInfoResult(new HashMap<>(), flowParam);
        if (oConvertUtils.isEmpty(flowInfoMap)) {
            return WorkflowResult.error("流程信息获取失败");
        }
        List<WfFlow> flowList = (List<WfFlow>) flowInfoMap.get("flowList");
        // 流程中包含的规则
        List<RulePair> rulePairList = flowList.stream()
                .filter(wfFlow -> wfFlow.getRuleType() != -1)
                .map(wfFlow -> new RulePair(wfFlow.getWfRuleId(), wfFlow.getRuleType()))
                .distinct()
                .collect(Collectors.toList());
        // 流程参与者参数
        RuleParam ruleParam = oConvertUtils.entityToModel(flowParam, RuleParam.class);
        // 规则 Context
        RuleContext ruleContext = new RuleContext(rulePairList, wfRuleService);
        // 添加流程审批人信息
        Map<String, Object> assigneeMap = getFlowForm.getAssigneeMap();
        assigneeMap = ruleContext.getResult(assigneeMap, ruleParam, null);
        // 转换
        List<WfTaskParticipant> resultList = WfFlow2WfTaskParticipant.getSubmitWfTaskParticipants(flowList, assigneeMap);
        // 重设 sort_order
        WorkflowUtil.resetSortOrder(resultList);
        return WorkflowResult.ok(resultList);
    }

    @Override
    public WorkflowResult submit(WfSubmitForm wfSubmitForm) {
        WfProcess wfProcess = null;
        // 获取流程定义
        if(oConvertUtils.isEmpty(wfSubmitForm.getWfProcessId())){
            wfProcess = wfProcessService.queryWfProcessOne(oConvertUtils.entityToModel(wfSubmitForm, WfProcess.class));
        } else {
            wfProcess = wfProcessService.getById(wfSubmitForm.getWfProcessId());
        }

        if (oConvertUtils.isEmpty(wfProcess)) {
            return WorkflowResult.error("流程定义不存在");
        }
        // form 表单参数插入
        String paramId = paramService.insertParam(wfProcess.getClassName(), wfSubmitForm.getParam(), wfSubmitForm.getParamList(), "");
        if (oConvertUtils.isEmpty(paramId)) {
            return WorkflowResult.error("参数插入失败");
        }
        /************** 提交流程 ****************/
        // 创建订单编号
        String orderNo = OrderNoUtil.generateOrderNo(wfProcess.getOrderNoPre(), wfProcess.getOrderNoLength());
        // 插入 wf_order
        WfOrder wfOrder = oConvertUtils.entityToModel(wfSubmitForm, WfOrder.class);
        wfOrder.setOrderNo(orderNo);
        wfOrder.setBusinessKey(paramId);
        wfOrder = wfOrderService.saveSubmitOrder(wfOrder, wfProcess);
        // 流程处理
        List<WfTaskParticipant> flowList = wfFlowService.dealSubmitFlow(wfSubmitForm.getFlowList(), wfOrder.getId());
        WfTaskParticipant next = flowList.get(1);
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("requestName", wfProcess.getRequestType());
        messageMap.put("requestNameEn", "New");
        messageMap.put("name", next.getDisplayName());
        messageMap.put("nameEn", next.getName());

        // 发送邮件
//        JSONObject jsonObject = wfMailSendService.sendSubmitApproveMail(messageMap, next.getOperatorEmail(), "pending");
        return WorkflowResult.ok("提交成功！");
    }

    @Resource
    private WfTaskService wfTaskService;

    @Override
    public WorkflowResult approve(ApproveForm approveForm) {
//        MDSUser mdsUser = approveForm.getCurrentUser();
        Integer taskId = approveForm.getTaskId();
        // 获取 wf_task
        WfTask wfTask = wfTaskService.getById(taskId);
        // 审批当前的 task
        wfTask.setId(taskId);
//        wfTask.setComment(approveForm.getComment());
//        wfTask.setApprover(mdsUser.getCommonName());
//        wfTask.setApproverId(mdsUser.getGuid());
//        wfTask.setApproverEmail(mdsUser.getEmail());
//        wfTask.setUpdateBy(mdsUser.getGuid());
        wfTask.setTaskStatus("2");

        wfTask.setApproveType("0"); // 同意
        wfTask.setFinishTime(DateUtils.getDate());
        wfTask.setUpdateTime(DateUtils.getDate());
        wfTaskService.updateById(wfTask);
        Integer wtpId = wfTask.getWtpId();
        // 查询当前的 wf_task_participant
        WfTaskParticipant wfTaskParticipant = wfTaskParticipantService.getById(wtpId);
        // 查询整个流程
        QueryWrapper<WfTaskParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", wfTask.getOrderId()).orderByAsc("sort_order");
        List<WfTaskParticipant> wfTaskParticipantList = wfTaskParticipantService.list(queryWrapper);
        // 判断是否为审批的最后一步
//        if(WorkflowUtil.isLastElement(wfTaskParticipantList, wfTaskParticipant)){   // 是最后一步，流程结束
//
//        } else {    // 不是最后一步，继续审批
//            wfTaskParticipant = oConvertUtils.entityToModel(wfTask, WfTaskParticipant.class);
//            WfTaskParticipant nextTaskParticipant = wfTaskParticipantService.updateCurrentTaskAndReturnNext(wfTaskParticipant,
//                    wfTaskParticipantList);
//        }
        wfTaskParticipant = oConvertUtils.entityToModel(wfTask, WfTaskParticipant.class);
        WfTaskParticipant nextTaskParticipant = wfTaskParticipantService.updateCurrentTaskAndReturnNext(wfTaskParticipant,
                wfTaskParticipantList);
        if (oConvertUtils.isEmpty(nextTaskParticipant)) {   // 是最后一步，流程结束
            // 更新 wf_order
            WfOrder wfOrder = wfOrderService.getById( wfTask.getOrderId());
            wfOrder.setOrderStatus("3");    // 已完成
//            wfOrder.setUpdateBy(mdsUser.getGuid());
            wfOrder.setFinishTime(DateUtils.getDate());
            wfOrderService.updateById(wfOrder);
        } else {
            WfTask nextWfTask = oConvertUtils.entityToModel(nextTaskParticipant, WfTask.class);
            wfTaskService.save(nextWfTask);
        }
        return WorkflowResult.ok("审批成功！");
    }

    /**
     * 订单详情
     * @param orderId
     * @param className
     * @return
     */
    @Override
    public Map<String, Object> detail(Integer orderId, String className) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> orderMap = wfOrderService.getMap(new QueryWrapper<WfOrder>().eq("id", orderId));
        QueryWrapper<WfTaskParticipant> wfTaskParticipantQueryWrapper = new QueryWrapper<>();
        wfTaskParticipantQueryWrapper.eq("order_id", orderId).orderByAsc("sort_order");
        orderMap.put("order_status_text", wfDictLoadService.getOrderStatusCacheInfo().get(orderMap.get("order_status").toString()));
        resultMap.put("order", orderMap);
        resultMap.put("param", workflowDynamicService.selectById(className,
                orderMap.get("business_key").toString()));
        resultMap.put("flowList", wfTaskParticipantService.list(wfTaskParticipantQueryWrapper));
        return resultMap;
    }

    @Override
    public Map<String, Object> queryTask(TaskForm taskForm) {
        return Collections.emptyMap();
    }

    /**
     * 批量提交流程
     * @param batchParam 批量操作参数
     * @return 批量操作结果
     */
    @Override
    public Map<String, Object> batchSubmit(BatchOperateParam batchParam) {
        // 参数校验
        if (batchParam == null || oConvertUtils.isEmpty(batchParam.getOperateParams())) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "批量操作参数不能为空");
            return result;
        }

        // 设置默认的操作类型为批量提交
        batchParam.setBatchOperateType("BATCH_SUBMIT");

        // 为每个操作参数设置必要的默认值
        for (OperateParam operateParam : batchParam.getOperateParams()) {
            // 设置操作类型为提交
            if (oConvertUtils.isEmpty(operateParam.getOperateType())) {
                operateParam.setOperateType(OperateConstant.OPERATE_SUBMIT);
            }
            // 设置订单状态为待提交
            if (oConvertUtils.isEmpty(operateParam.getOrderStatus())) {
                operateParam.setOrderStatus(StateConstant.TO_BE_SUBMIT);
            }
        }

        // 通过ApplicationContext获取批量提交处理器，避免循环依赖
        OperateBatchSubmitHandler batchHandler = applicationContext.getBean(OperateBatchSubmitHandler.class);
        return batchHandler.handleBatch(batchParam);
    }

    /**
     * 操作订单和参数
     * @param operateParam
     * @return
     */
    @Override
    public WfOrder operateOrderAndParam(OperateParam operateParam) {
        return wfOperateService.operateOrderAndParam(operateParam);
    }

}
