package com.epiroc.workflow.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epiroc.workflow.common.common.WorkflowResult;
import com.epiroc.workflow.common.convert.WfFlow2WfTaskParticipant;
import com.epiroc.workflow.common.service.*;
import com.epiroc.workflow.common.system.flow.FlowContext;
import com.epiroc.workflow.common.system.rule.RuleContext;
import com.epiroc.workflow.common.system.rule.model.RulePair;
import com.epiroc.workflow.common.system.rule.model.RuleParam;
import com.epiroc.workflow.common.util.DateUtils;
import com.epiroc.workflow.common.util.WorkflowUtil;
import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.entity.*;
import com.epiroc.workflow.common.mapper.ProkuraMapper;
import com.epiroc.workflow.common.mapper.WfFlowMapper;
import com.epiroc.workflow.common.mapper.WfProcessMapper;
import com.epiroc.workflow.common.entity.form.GetFlowForm;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import com.epiroc.workflow.common.system.query.QueryWrapperBuilder;
import com.epiroc.workflow.common.util.IdUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WfFlowServiceImpl extends ServiceImpl<WfFlowMapper, WfFlow> implements WfFlowService {

    @Resource
    private WfFlowMapper wfFlowMapper;

    @Resource
    private WfProcessMapper wfProcessMapper;

    @Resource
    private ProkuraMapper prokuraMapper;

    @Resource
    private WfTaskParticipantService wfTaskParticipantService;

    @Resource
    private WfTaskService wfTaskService;

    @Resource
    private WfProcessService wfProcessService;

    @Resource
    private WfRuleService wfRuleService;

    @Override
    public List<WfFlow> queryWfFlowList(WfFlow wfFlow) {
        if (wfFlow != null) {
            QueryWrapper<WfFlow> wrapper = QueryWrapperBuilder.buildQueryWrapper(wfFlow);
            wrapper.orderByAsc(WorkflowConstant.FIELD_SORT_ORDER);
            return wfFlowMapper.selectList(wrapper);
        }
        return null;
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
        FlowContext flowContext = new FlowContext(wfProcess.getFlowTypes(), this);
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

    /**
     * 根据wf_process_id,flow_type,index,stage查询流程信息
     * @param param
     * @return
     */
    @Override
    public List<Integer> getParticipantIdList(FlowParam param) {
        List<Integer> resultList = new ArrayList<>();
        WfFlow wfFlow = oConvertUtils.entityToModel(param, WfFlow.class);
        List<WfFlow> wfFlowList = queryWfFlowList(wfFlow);
        if (oConvertUtils.listIsNotEmpty(wfFlowList)) {
            for (WfFlow temp : wfFlowList) {
                resultList.addAll(temp.getWfParticipantIds());
            }
        }
        return resultList;
    }

    @Override
    public List<Integer> getFixedParticipantIdList(FlowParam param) {
        List<Integer> resultList = new ArrayList<>();
        WfFlow wfFlow = oConvertUtils.entityToModel(param, WfFlow.class);
        wfFlow.setFlowType(WorkflowConstant.FLOW_TYPE_FIXED);
        // 根据wf_process_id,flow_type,index,stage查询流程信息
        List<WfFlow> wfFlowList = queryWfFlowList(wfFlow);
        if (oConvertUtils.listIsNotEmpty(wfFlowList)) {
            for (WfFlow temp : wfFlowList) {
                resultList.addAll(temp.getWfParticipantIds());
            }
        }
        return resultList;
    }

    @Override
    public List<Integer> getProkuraParticipantIdList(FlowParam param) {
        List<Integer> resultList = new ArrayList<>();
        WfFlow wfFlow = oConvertUtils.entityToModel(param, WfFlow.class);
        wfFlow.setFlowType(WorkflowConstant.FLOW_TYPE_PROKURA);
        // 根据wf_process_id,flow_type,index,stage查询流程信息
        List<WfFlow> wfFlowList = queryWfFlowList(wfFlow);
        if (oConvertUtils.listIsNotEmpty(wfFlowList)) {
            for (WfFlow temp : wfFlowList) {
                resultList.addAll(temp.getWfParticipantIds());
            }
            // 根据需要审批的金额，获取需要的审批人
            List<Prokura> prokuraList = prokuraMapper.getProkuraParticipants(param);
            // 获取前 prokuraList.size() 个元素
            if(resultList.size() != prokuraList.size()){
                resultList = resultList.subList(0, prokuraList.size());
            }
        }
        return resultList;
    }

    /**
     * 获取流程详情
     * @param param
     * @return
     */
    @Override
    public List<WfFlow> getFlowDetail(FlowParam param) {
        WfFlow wfFlow = oConvertUtils.entityToModel(param, WfFlow.class);
        // 根据wf_process_id,flow_type,index,stage查询流程信息
        List<WfFlow> wfFlowList = queryWfFlowList(wfFlow);
        return wfFlowList;
    }

    @Override
    public List<Integer> getProkuraWfFlowIdList(FlowParam param) {
        // 根据wf_process_id,flow_type,index,stage查询流程信息
        List<Integer> resultList = wfFlowMapper.getWfFlowIdList(param);
        if (oConvertUtils.listIsNotEmpty(resultList)) {
            // 根据需要审批的金额，获取需要的审批人
            List<Prokura> prokuraList = prokuraMapper.getProkuraParticipants(param);
            // 获取前 prokuraList.size() 个元素
            if(resultList.size() != prokuraList.size()){
                resultList = resultList.subList(0, prokuraList.size());
            }
        }
        return resultList;
    }

    @Override
    public List<WfFlow> getProkuraFlowDetail(FlowParam param) {
        WfFlow wfFlow = oConvertUtils.entityToModel(param, WfFlow.class);
        // 根据wf_process_id,flow_type,index,stage查询流程信息
        List<WfFlow> wfFlowList = queryWfFlowList(wfFlow);
        if (oConvertUtils.listIsNotEmpty(wfFlowList)) {
            // 根据需要审批的金额，获取需要的审批人
            List<Prokura> prokuraList = prokuraMapper.getProkuraParticipants(param);
            // 获取前 prokuraList.size() 个元素
            if(wfFlowList.size() != prokuraList.size() && wfFlowList.size() > prokuraList.size()){
                wfFlowList = wfFlowList.subList(0, prokuraList.size());
            }
        }
        return wfFlowList;
    }

    @Override
    public List<WfFlow> getSetFlowDetail(FlowParam param) {
        WfFlow wfFlow = oConvertUtils.entityToModel(param, WfFlow.class);
        // 根据wf_process_id,flow_type,index,stage查询流程信息
        List<WfFlow> wfFlowList = queryWfFlowList(wfFlow);
        return wfFlowList;
    }

    @Override
    public List<Integer> getSetWfFlowIdList(FlowParam param) {
        return Collections.emptyList();
    }

    @Override
    public List<Integer> getWfFlowIdList(FlowParam param) {
        // 根据wf_process_id,flow_type,index,stage查询流程信息
        List<Integer> resultList = wfFlowMapper.getWfFlowIdList(param);
        return resultList;
    }

    @Override
    public List<WfTaskParticipant> dealSubmitFlow(List<WfTaskParticipant> flowList, Integer orderId) {
        for(int i = 0;i < flowList.size();i++){
            WfTaskParticipant wfTaskParticipant = flowList.get(i);
            wfTaskParticipant.setOrderId(orderId);
            wfTaskParticipant.setCreateTime(DateUtils.getDate());
            wfTaskParticipant.setUpdateTime(DateUtils.getDate());
            String operatorIdStr = wfTaskParticipant.getOperatorId();
            List<String> operatorIdList = Arrays.asList(operatorIdStr.split(","));
            List<String> operatorIds = new ArrayList<>();
            for (String temp : operatorIdList) {
                String operatorId = IdUtil.resetGuidStr(temp);
                operatorIds.add(operatorId);
            }
            wfTaskParticipant.setOperatorId(String.join(",", operatorIds));
            if (i == 0) {
                wfTaskParticipant.setTaskStatus("2");   // 已审批
                wfTaskParticipant.setApproveType("0");  // 0：同意
                wfTaskParticipant.setFinishTime(DateUtils.getDate());
                wfTaskParticipant.setApprover(wfTaskParticipant.getOperator());
                wfTaskParticipant.setApproverId(wfTaskParticipant.getOperatorId());
                wfTaskParticipant.setApproverEmail(wfTaskParticipant.getOperatorEmail());
            } else if (i == 1) {
                wfTaskParticipant.setTaskStatus("1");   // 等待审批
            } else {
                wfTaskParticipant.setTaskStatus("0");   // 未审批
            }
        }
        wfTaskParticipantService.saveBatch(flowList);
        WfTask createWftask = oConvertUtils.entityToModel(flowList.get(0), WfTask.class);
        createWftask.setWtpId(flowList.get(0).getId());
        wfTaskService.save(createWftask);
        WfTask nextWftask = oConvertUtils.entityToModel(flowList.get(1), WfTask.class);
        wfTaskService.save(nextWftask);
        return flowList;
    }


}
