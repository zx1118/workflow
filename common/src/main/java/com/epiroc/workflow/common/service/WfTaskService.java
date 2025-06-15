package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epiroc.workflow.common.entity.WfTask;

public interface WfTaskService extends IService<WfTask> {

    Boolean editWfask(Integer taskId, String number, String comment);

}
