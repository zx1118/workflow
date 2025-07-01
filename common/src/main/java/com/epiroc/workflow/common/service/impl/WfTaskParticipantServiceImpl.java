package com.epiroc.workflow.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epiroc.workflow.common.service.WfDictLoadService;
import com.epiroc.workflow.common.system.constant.ApproveTypeConstant;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.system.constant.StateConstant;
import com.epiroc.workflow.common.system.constant.TaskStatusConstant;
import com.epiroc.workflow.common.util.DateUtils;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.mapper.WfTaskParticipantMapper;
import com.epiroc.workflow.common.service.WfTaskParticipantService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class WfTaskParticipantServiceImpl extends ServiceImpl<WfTaskParticipantMapper, WfTaskParticipant>
        implements WfTaskParticipantService, TaskStatusConstant, StateConstant, ApproveTypeConstant {

    @Resource
    private WfDictLoadService wfDictLoadService;


    @Override
    public Integer saveSubmitFlow(List<WfTaskParticipant> flowList, Integer orderId) {
        for(int i = 0;i < flowList.size();i++){
            WfTaskParticipant wfTaskParticipant = flowList.get(i);
            wfTaskParticipant.setOrderId(orderId);
            String operatorIdStr = wfTaskParticipant.getOperatorId();
            List<String> operatorIdList = Arrays.asList(operatorIdStr.split(","));
            List<String> operatorIds = new ArrayList<>();
            for (String temp : operatorIdList) {
                String operatorId = resetGuidStr(temp);
                operatorIds.add(operatorId);
            }
            wfTaskParticipant.setOperatorId(String.join(",", operatorIds));
            if (i == 0) {
                wfTaskParticipant.setTaskStatus(wfDictLoadService.getTaskStatusCacheInfo().get(APPROVED));   // 已审批
                wfTaskParticipant.setApproveType(wfDictLoadService.getApproveTypeCacheInfo().get(APPROVE));  // 0：同意
                wfTaskParticipant.setFinishTime(DateUtils.getDate());
                wfTaskParticipant.setApprover(wfTaskParticipant.getOperator());
                wfTaskParticipant.setApproverId(wfTaskParticipant.getOperatorId());
                wfTaskParticipant.setApproverEmail(wfTaskParticipant.getOperatorEmail());
            } else if (i == 1) {
                wfTaskParticipant.setTaskStatus(wfDictLoadService.getTaskStatusCacheInfo().get(WAITING));   // 等待审批
            } else {
                wfTaskParticipant.setTaskStatus(wfDictLoadService.getTaskStatusCacheInfo().get(NOT_APPROVED));   // 未审批
            }
        }
        saveBatch(flowList);
        return flowList.get(1).getId();
    }

    @Override
    public WfTaskParticipant updateCurrentTaskAndReturnNext(WfTaskParticipant currentTaskParticipant,
                                                            List<WfTaskParticipant> wfTaskParticipantList) {
        currentTaskParticipant.setFinishTime(DateUtils.getDate());
        updateById(currentTaskParticipant);
        // 判断当前流程在列表中的位置
        for(int i = 0;i < wfTaskParticipantList.size();i++){
            WfTaskParticipant temp = wfTaskParticipantList.get(i);
            if(temp.getId().equals(currentTaskParticipant.getId())){
                if(i == wfTaskParticipantList.size() - 1){
                    return null;
                } else {
                    WfTaskParticipant nextParticipantTask = wfTaskParticipantList.get(i + 1);
                    nextParticipantTask.setTaskStatus(wfDictLoadService.getTaskStatusCacheInfo().get(WAITING));
                    nextParticipantTask.setUpdateTime(DateUtils.getDate());
                    updateById(nextParticipantTask);
                    return nextParticipantTask;
                }
           }
        }
        return null;
    }

    @Override
    public List<WfTaskParticipant> getFullFlow(Integer id) {
        QueryWrapper<WfTaskParticipant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", id).orderByAsc("sort_order");
        List<WfTaskParticipant> wfTaskParticipantList = list(queryWrapper);
        return wfTaskParticipantList;
    }

    @Override
    public WfTaskParticipant updateCurrent(WfTaskParticipant current) {
        updateById(current);
        return current;
    }

    @Override
    public WfTaskParticipant updateAndReturnNext(WfTaskParticipant current,
                                                 List<WfTaskParticipant> wfTaskParticipantList) {
        // 判断当前流程在列表中的位置
        for(int i = 0;i < wfTaskParticipantList.size();i++){
            WfTaskParticipant temp = wfTaskParticipantList.get(i);
            if(temp.getId().equals(current.getId())){
                if(i == wfTaskParticipantList.size() - 1){
                    return null;
                } else {
                    WfTaskParticipant nextParticipantTask = wfTaskParticipantList.get(i + 1);
                    nextParticipantTask.setTaskStatus(wfDictLoadService.getTaskStatusCacheInfo().get(WAITING));
                    nextParticipantTask.setUpdateTime(DateUtils.getDate());
                    updateById(nextParticipantTask);
                    return nextParticipantTask;
                }
            }
        }
        return null;
    }

    public static String resetGuidStr(String guid) {
        int len = 0;
        if(guid.length() < CommonConstant.BASE_GUID_LENGTH){
            len = CommonConstant.BASE_GUID_LENGTH - guid.length();
        }
        for(int i = 0;i < len;i++){
            guid = CommonConstant.STRING_ZERO + guid;
        }
        return guid;
    }

}
