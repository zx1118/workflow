package com.epiroc.workflow.common.service;

import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.entity.param.OperateParam;

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
public interface WfOperateService {

    /**
     * 操作参数和订单
     * @param operateParam
     * @return
     */
    WfOrder operateOrderAndParam(OperateParam operateParam);

    /**
     * 通过实体类中的 ID 更新 wf_order
     * @param order
     */
    void updateOrderById(WfOrder order);

    /**
     * 根据条件查询一条记录
     * @param wfProcess
     * @return
     */
    WfProcess queryWfProcessOne(WfProcess wfProcess);

    WfProcess getWfProcessById(Integer wfProcessId);

    /**
     * 通过 OperateParam 获取流程定义
     * @param operateParam
     * @return
     */
    WfProcess getWfProcessByOperateParam(OperateParam operateParam);

    /**
     * 处理提交流程
     * @param operateParam
     * @param orderId
     * @return
     */
    List<WfTaskParticipant> dealSubmitFlow(OperateParam operateParam, Integer orderId);

    boolean updateTaskById(WfTask task);

    List<WfTaskParticipant> getFullFlow(Integer orderId);

    WfTaskParticipant updateCurrentTaskParticipant(WfTaskParticipant current);

    WfTaskParticipant updateAndReturnNextTaskParticipant(WfTaskParticipant current, List<WfTaskParticipant> wfTaskParticipantList);

    boolean saveTask(WfTask nextWfTask);

    boolean isLastApprovalNode(Integer orderId, Integer taskId);

}
