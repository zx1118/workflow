package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.entity.param.OperateParam;

public interface WfProcessService extends IService<WfProcess> {

    /**
     * 根据 entity 条件，查询一条记录
     * @param wfProcess
     * @return
     */
    WfProcess queryWfProcessOne(WfProcess wfProcess);

    WfProcess getWfProcessById(Integer wfProcessId);

    WfProcess getWfProcessByOperateParam(OperateParam operateParam);

}
