package com.epiroc.workflow.common.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epiroc.workflow.common.common.Result;
import com.epiroc.workflow.common.entity.*;
import com.epiroc.workflow.common.entity.form.ApproveForm;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.service.*;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.system.factory.OperateAbstractHandler;
import com.epiroc.workflow.common.system.factory.OperateFactory;
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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    private DynamicServiceDeprecate dynamicService;

    @Resource
    private WorkflowStateService workflowStateService;


    @Resource
    private WfOperateService wfOperateService;

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
        // 获取流程定义
        WfProcess wfProcess = wfProcessService.getWfProcessByOperateParam(operateParam);
        if (oConvertUtils.isEmpty(wfProcess)) {
            String message = "流程定义不存在";
        }
        operateParam.setWfProcess(wfProcess);
        WfOrder wfOrder = wfOperateService.operateOrderAndParam(operateParam);
        // 获取操作类型
        String operateKey = StringUtil.joinWithChar(operateParam.getOrderStatus(), operateParam.getOperateType(), StrUtil.DASHED);
        // 根据当前状态和操作类型执行相应操作,使用WorkflowStateService管理状态
        OperateAbstractHandler invokeStrategy = OperateFactory.getInvokeStrategy(operateKey);
        invokeStrategy.handle(wfOrder, operateParam);
        return null;
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
    public Result operate(OperateParam operateParam) {
        Integer orderId = operateParam.getOrderId();
        // 查询 wf_order
        WfOrder wfOrder = wfOrderService.getById(orderId);
        wfOrder.setUpdateBy(operateParam.getOperatorId());
        wfOrder.setUpdateTime(DateUtils.getDate());
        if (oConvertUtils.isEmpty(wfOrder)) {
            return Result.error("工作流订单不存在");
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
                    return Result.error("暂存状态下不支持的操作类型: " + operateType);
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
                return Result.error("工作流已完成，不能执行任何操作");
            // 已拒绝状态
            case ORDER_STATUS_REJECT:
                if ("cancel".equals(operateType)) {
                    // 已拒绝状态 -> 取消操作
                    return workflowStateService.cancel(wfOrder);
                } else {
                    return Result.error("已拒绝状态下不支持的操作类型: " + operateType);
                }
                // 已取消状态
            case ORDER_STATUS_CANCEL:
                return Result.error("工作流已取消，不能执行任何操作");
            // 退回状态
            case ORDER_STATUS_RETURN:
                if ("submit".equals(operateType)) {
                    // 退回状态 -> 重新提交
//                    return submitFromReturn(wfOrder, operateParam);
                } else if ("cancel".equals(operateType)) {
                    // 退回状态 -> 取消操作
                    return workflowStateService.cancel(wfOrder);
                } else {
                    return Result.error("退回状态下不支持的操作类型: " + operateType);
                }
            default:
                return Result.error("未知的工作流状态: " + orderStatus);
        }

//        return null;
    }

    /**
     * 从暂存状态提交工作流
     * @param wfOrder 工作流订单
     * @param operateParam 操作参数
     * @return 操作结果
     */
    private Result submitFromDraft(WfOrder wfOrder, OperateParam operateParam) {
        // 更新订单信息
        wfOrder.setOrderStatus(CommonConstant.ORDER_STATUS_PENDING);
        wfOrderService.updateById(wfOrder);

        // 调用工作流状态服务提交
        Result submitResult = workflowStateService.submit(wfOrder, operateParam);
        if (!submitResult.isSuccess()) {
            return submitResult;
        }

        // 处理流程，创建任务
        if (operateParam.getFlowList() != null && !operateParam.getFlowList().isEmpty()) {
            List<WfTaskParticipant> flowList = wfFlowService.dealSubmitFlow(operateParam.getFlowList(), wfOrder.getId());

            // 返回下一个审批人信息
            if (flowList != null && flowList.size() > 1) {
                WfTaskParticipant next = flowList.get(1);
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("nextApprover", next);
                return Result.ok(resultMap);
            }
        }

        return Result.ok("提交成功");
    }

    /**
     * 获取流程信息
     * @param getFlowForm
     * @return
     */
    @Override
    public Result getFlow(GetFlowForm getFlowForm) {
        // 获取流程定义
        WfProcess wfProcess = wfProcessService.getById(getFlowForm.getWfProcessId());
        if (oConvertUtils.isEmpty(wfProcess)) {
            return Result.error("流程定义不存在");
        }
        FlowContext flowContext = new FlowContext(wfProcess.getFlowTypes(), wfFlowService);
        // 流程参数
        FlowParam flowParam = oConvertUtils.entityToModel(getFlowForm, FlowParam.class);
        // 获取流程信息
        Map<String, Object> flowInfoMap = flowContext.getFlowInfoResult(new HashMap<>(), flowParam);
        if (oConvertUtils.isEmpty(flowInfoMap)) {
            return Result.error("流程信息获取失败");
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
        return Result.ok(resultList);
    }

    @Override
    public Result submit(WfSubmitForm wfSubmitForm) {
        WfProcess wfProcess = null;
        // 获取流程定义
        if(oConvertUtils.isEmpty(wfSubmitForm.getWfProcessId())){
            wfProcess = wfProcessService.queryWfProcessOne(oConvertUtils.entityToModel(wfSubmitForm, WfProcess.class));
        } else {
            wfProcess = wfProcessService.getById(wfSubmitForm.getWfProcessId());
        }

        if (oConvertUtils.isEmpty(wfProcess)) {
            return Result.error("流程定义不存在");
        }
        // form 表单参数插入
        String paramId = paramService.insertParam(wfProcess.getClassName(), wfSubmitForm.getParam(), wfSubmitForm.getParamList());
        if (oConvertUtils.isEmpty(paramId)) {
            return Result.error("参数插入失败");
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
        return Result.ok("提交成功！");
    }

    @Resource
    private WfTaskService wfTaskService;

    @Override
    public Result approve(ApproveForm approveForm) {
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
        return Result.ok("审批成功！");
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
        WfOrder wfOrder = wfOrderService.getById(orderId);
        QueryWrapper<WfTaskParticipant> wfTaskParticipantQueryWrapper = new QueryWrapper<>();
        wfTaskParticipantQueryWrapper.eq("order_id", orderId).orderByAsc("sort_order");
        resultMap.put("order", wfOrder);
        resultMap.put("param", dynamicService.selectById(className, wfOrder.getBusinessKey()));
        resultMap.put("flowList", wfTaskParticipantService.list(wfTaskParticipantQueryWrapper));
        return resultMap;
    }

}
