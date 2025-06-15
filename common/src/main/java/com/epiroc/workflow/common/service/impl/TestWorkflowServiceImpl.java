package com.epiroc.workflow.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epiroc.workflow.common.common.Result;
import com.epiroc.workflow.common.entity.*;
import com.epiroc.workflow.common.entity.form.ApproveForm;
import com.epiroc.workflow.common.service.*;
import com.epiroc.workflow.common.util.DateUtils;
import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.convert.WfFlow2WfTaskParticipant;
import com.epiroc.workflow.common.util.OrderNoUtil;
import com.epiroc.workflow.common.entity.form.GetFlowForm;
import com.epiroc.workflow.common.entity.form.WfSubmitForm;
import com.epiroc.workflow.common.mapper.WfFlowMapper;
import com.epiroc.workflow.common.mapper.WfProcessMapper;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.system.flow.FlowContext;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import com.epiroc.workflow.common.system.rule.RuleContext;
import com.epiroc.workflow.common.system.rule.model.RulePair;
import com.epiroc.workflow.common.system.rule.model.RuleParam;
import com.epiroc.workflow.common.util.WorkflowUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestWorkflowServiceImpl implements TestWorkflowService, WorkflowConstant {

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
    private WfTaskService wfTaskService;

    @Resource
    private WorkflowStateService workflowStateService;

    @Override
    public Result operate() {
        return null;
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

    /**
     * 申请提交或暂存
     * 使用状态模式处理工作流状态转换
     * @param wfSubmitForm
     * @return
     */
    @Override
    @Transactional
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

        // 判断是提交还是暂存
        boolean isDraft = CommonConstant.ORDER_STATUS_RETURN_TO_BE_SUBMITTED.equals(wfSubmitForm.getOrderStatus());

        // form 表单参数插入
        String paramId = paramService.insertParam(wfProcess.getClassName(), wfSubmitForm.getParam(), wfSubmitForm.getParamList());
        if (oConvertUtils.isEmpty(paramId)) {
            return Result.error("参数插入失败");
        }

        // 创建订单编号
        String orderNo = OrderNoUtil.generateOrderNo(wfProcess.getOrderNoPre(), wfProcess.getOrderNoLength());

        // 插入 wf_order
        WfOrder wfOrder = oConvertUtils.entityToModel(wfSubmitForm, WfOrder.class);
        wfOrder.setOrderNo(orderNo);
        wfOrder.setBusinessKey(paramId);
        wfOrder = wfOrderService.saveSubmitOrder(wfOrder, wfProcess);

        // 使用WorkflowStateService管理状态
        Result stateResult;
        if (isDraft) {
            // 如果是暂存
            stateResult = workflowStateService.saveAsDraft(wfOrder);
            if (!stateResult.isSuccess()) {
                return stateResult;
            }
            return Result.ok("暂存成功！");
        } else {
            // 如果是提交
            stateResult = workflowStateService.submit(wfOrder);
            if (!stateResult.isSuccess()) {
                return stateResult;
            }

            // 流程处理
            List<WfTaskParticipant> flowList = wfFlowService.dealSubmitFlow(wfSubmitForm.getFlowList(), wfOrder.getId());

            if (flowList != null && flowList.size() > 1) {
                WfTaskParticipant next = flowList.get(1);
                Map<String, String> messageMap = new HashMap<>();
                messageMap.put("requestName", wfProcess.getRequestType());
                messageMap.put("requestNameEn", "New");
                messageMap.put("name", next.getDisplayName());
                messageMap.put("nameEn", next.getName());

                // 发送邮件
                // JSONObject jsonObject = wfMailSendService.sendSubmitApproveMail(messageMap, next.getOperatorEmail(), "pending");
            }

            return Result.ok("提交成功！");
        }
    }

    /**
     * 审批操作
     * 使用WorkflowStateService处理工作流状态转换
     * @param approveForm
     * @return
     */
    @Override
    @Transactional
    public Result approve(ApproveForm approveForm) {
        Integer taskId = approveForm.getTaskId();
        // 获取 wf_task
        WfTask wfTask = wfTaskService.getById(taskId);
        if (wfTask == null) {
            return Result.error("任务不存在");
        }

        // 获取订单信息
        WfOrder wfOrder = wfOrderService.getById(wfTask.getOrderId());
        if (wfOrder == null) {
            return Result.error("工作流订单不存在");
        }

        // 设置任务的基本信息
        wfTask.setComment(approveForm.getComment());
        wfTask.setUpdateTime(DateUtils.getDate());

        // 根据操作类型执行相应的状态转换
        String action = approveForm.getOrderStatus();

        if ("approve".equals(action)) {
            // 审批通过
            return workflowStateService.approve(wfOrder, wfTask);
        } else if ("reject".equals(action)) {
            // 拒绝
            return workflowStateService.reject(wfOrder, wfTask);
        } else if ("return".equals(action)) {
            // 退回
            return workflowStateService.returnToUser(wfOrder, wfTask);
        } else {
            return Result.error("不支持的操作类型");
        }
    }

    /**
     * 取消工作流
     * 使用WorkflowStateService处理工作流状态转换
     * @param orderId 订单ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result cancel(Integer orderId) {
        // 获取订单信息
        WfOrder wfOrder = wfOrderService.getById(orderId);
        if (wfOrder == null) {
            return Result.error("工作流订单不存在");
        }

        // 使用WorkflowStateService取消工作流
        return workflowStateService.cancel(wfOrder);
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