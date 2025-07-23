package com.epiroc.workflow.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.form.TaskForm;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface WfTaskMapper extends BaseMapper<WfTask> {

    /**
     * 分页查询待办任务（关联查询wf_order和wf_process表）
     * @param page 分页参数
     * @param currentUserId 当前用户ID
     * @param currentUserEmail 当前用户邮箱
     * @param processId 流程定义ID（可选）
     * @param requesterId 申请人ID（可选）
     * @param requesterName 申请人名称（可选）
     * @param orderStatus 订单状态（可选）
     * @param processName 流程名称（可选）
     * @param startTime 申请开始时间（可选）
     * @param endTime 申请结束时间（可选）
     * @param sortField 排序字段
     * @param sortOrder 排序方向
     * @return 分页结果
     */
    IPage<Map<String, Object>> queryPendingTasksPage(
        Page<Map<String, Object>> page,
        @Param("currentUserId") String currentUserId,
        @Param("currentUserEmail") String currentUserEmail,
        @Param("processId") Long processId,
        @Param("requesterId") String requesterId,
        @Param("requesterName") String requesterName,
        @Param("orderStatus") String orderStatus,
        @Param("processName") String processName,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime,
        @Param("sortField") String sortField,
        @Param("sortOrder") String sortOrder
    );

    IPage<Map<String, Object>> getPendingTasks(Page<Map<String, Object>> page, @Param("params") TaskForm params);

    IPage<Map<String, Object>> getPendingTasksWithoutProcess(Page<Map<String, Object>> page, @Param("params") TaskForm params);

    IPage<Map<String, Object>> getPendingTasksParamMap(Page<Map<String, Object>> page, Map<String, Object> paramMap);

    IPage<Map<String, Object>> getMyTasks(Page<Map<String, Object>> page, @Param("paramMap") Map<String, Object> paramMap);

    IPage<Map<String, Object>> getMyTasksMultiProcess(Page<Map<String, Object>> page, @Param("paramMap") Map<String, Object> paramMap);

    IPage<Map<String, Object>> getAllTasks(Page<Map<String, Object>> page, @Param("paramMap") Map<String, Object> allParam);

    IPage<Map<String, Object>> getPendingTasksMultiProcess(Page<Map<String, Object>> page, @Param("params") TaskForm taskForm);

    IPage<Map<String, Object>> getRelateTasksMultiProcess(Page<Map<String, Object>> page, @Param("paramMap") Map<String, Object> relateParam);
}
