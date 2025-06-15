package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.entity.param.OperateParam;

public interface WfOrderService extends IService<WfOrder> {

    WfOrder saveSubmitOrder(WfOrder wfOrder, WfProcess wfProcess);

    WfOrder getOrder(WfOrder operateParam);

    WfOrder getOrderByOperateParam(OperateParam operateParam);

}
