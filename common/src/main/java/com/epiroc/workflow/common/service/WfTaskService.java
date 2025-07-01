package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.form.TaskForm;

import java.util.List;
import java.util.Map;

public interface WfTaskService extends IService<WfTask> {

    Boolean editWfask(Integer taskId, String number, String comment);

    IPage<Map<String, Object>> pending(TaskForm taskForm);

    /**
     * 分页查询待办任务
     * @param taskForm 查询条件
     * @return 分页结果
     */
    IPage<Map<String, Object>> queryPendingTasks(TaskForm taskForm);

}
