package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.util.WorkflowUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 工作流工具服务类
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-24
 */
@Service
public class WfToolService {

    @Resource
    private WfTaskService wfTaskService;

    @Resource
    private WfTaskParticipantService wfTaskParticipantService;

    /**
     * 检查是否是最后一个审批节点
     * @param orderId 订单ID
     * @param currentTaskId 当前任务ID
     * @return 是否是最后一个审批节点
     */
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

}
