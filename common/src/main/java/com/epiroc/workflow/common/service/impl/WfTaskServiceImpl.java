package com.epiroc.workflow.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.mapper.WfTaskMapper;
import com.epiroc.workflow.common.service.WfTaskService;
import org.springframework.stereotype.Service;

@Service
public class WfTaskServiceImpl extends ServiceImpl<WfTaskMapper, WfTask> implements WfTaskService {
    @Override
    public Boolean editWfask(Integer taskId, String taskStatus, String comment) {
        WfTask wfTask = new WfTask();
        wfTask.setId(taskId);
        wfTask.setComment(comment);
        wfTask.setTaskStatus(taskStatus);
        this.updateById(wfTask);
        int a = 1;
        return null;
    }

}
