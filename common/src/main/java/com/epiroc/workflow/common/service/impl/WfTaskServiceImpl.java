package com.epiroc.workflow.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.entity.WfTask;
import com.epiroc.workflow.common.entity.form.TaskForm;
import com.epiroc.workflow.common.enums.TaskStatusEnum;
import com.epiroc.workflow.common.mapper.WfProcessMapper;
import com.epiroc.workflow.common.mapper.WfTaskMapper;
import com.epiroc.workflow.common.service.WfProcessService;
import com.epiroc.workflow.common.service.WfTaskService;
import com.epiroc.workflow.common.service.WorkflowDynamicService;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.util.WorkflowUtil;
import com.epiroc.workflow.common.util.oConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class WfTaskServiceImpl extends ServiceImpl<WfTaskMapper, WfTask> implements WfTaskService {

    @Autowired
    private WfProcessService wfProcessService;

    @Autowired
    private WorkflowDynamicService workflowDynamicService;

    @Resource
    private WfTaskMapper wfTaskMapper;

    @Resource
    private WfProcessMapper wfProcessMapper;

    @Override
    public Boolean editWfask(Integer taskId, String taskStatus, String comment) {
        WfTask wfTask = new WfTask();
        wfTask.setId(taskId);
        wfTask.setComment(comment);
        wfTask.setTaskStatus(taskStatus);
        this.updateById(wfTask);
        return null;
    }

    @Override
    public IPage<Map<String, Object>> pending(TaskForm taskForm) {
        try {
            // 1. 查询流程定义
            WfProcess wfProcess = null;
            if (taskForm.getProcessId() != null) {
                wfProcess = wfProcessService.getById(taskForm.getProcessId());
                if (wfProcess == null) {
                    log.warn("未找到流程定义，processId: {}", taskForm.getProcessId());
                    return new Page<>(taskForm.getPageNum(), taskForm.getPageSize());
                }
            }
            taskForm.setTableName(wfProcess.getTableName());
            // 创建分页对象
            Page<Map<String, Object>> page = new Page<>(taskForm.getPageNum(), taskForm.getPageSize());
            // 分页查询方法并返回数据列表
            IPage<Map<String, Object>> pageResult = wfTaskMapper.getPendingTasks(page, taskForm);
            return pageResult;
        } catch (Exception e) {
            log.error("查询待办任务列表失败", e);
            throw new RuntimeException("查询待办任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public IPage<Map<String, Object>> pendingWithoutProcess(TaskForm taskForm) {
        try {
            // 创建分页对象
            Page<Map<String, Object>> page = new Page<>(taskForm.getPageNum(), taskForm.getPageSize());
            // 分页查询方法并返回数据列表
            IPage<Map<String, Object>> pageResult = wfTaskMapper.getPendingTasksWithoutProcess(page, taskForm);
            return pageResult;
        } catch (Exception e) {
            log.error("查询待办任务列表失败", e);
            throw new RuntimeException("查询待办任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public IPage<Map<String, Object>> pendingMultiProcess(TaskForm taskForm) {
        try {
            // 1. 查询流程定义
            List<WfProcess> processList = wfProcessMapper.selectList(new QueryWrapper<WfProcess>());
            List<Long> processIdList = new ArrayList<>();
            for(WfProcess wfProcess : processList){
                processIdList.add(Long.valueOf(wfProcess.getId()));
            }
            taskForm.setTableName(processList.get(0).getTableName());
            taskForm.setProcessIds(processIdList);
            // 创建分页对象
            Page<Map<String, Object>> page = new Page<>(taskForm.getPageNum(), taskForm.getPageSize());
            // 分页查询方法并返回数据列表
            IPage<Map<String, Object>> pageResult = wfTaskMapper.getPendingTasksMultiProcess(page, taskForm);
            return pageResult;
        } catch (Exception e) {
            log.error("查询待办任务列表失败", e);
            throw new RuntimeException("查询待办任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 我的任务
     * @param myParam
     * @return
     */
    @Override
    public IPage<Map<String, Object>> myMultiProcess(Map<String, Object> myParam) {
        if(!myParam.containsKey("queryAll")) myParam.put("queryAll", "0");
        try {
            // 1. 查询流程定义
            List<WfProcess> processList = wfProcessMapper.selectList(new QueryWrapper<WfProcess>());
            List<Long> processIdList = new ArrayList<>();
            for(WfProcess wfProcess : processList){
                processIdList.add(Long.valueOf(wfProcess.getId()));
            }
            myParam.put("tableName", processList.get(0).getTableName());
            myParam.put("processIds", processIdList);
            // 第一步：根据实体类过滤有效字段
            Map<String, Object> validParams = WorkflowUtil.filterEntityFields(processList.get(0).getClassName(), myParam);
            // 第二步：基于有效字段生成SQL查询条件
            String tableAlias = "pa";
            // 生成默认查询模式（等于 + 模糊）
            String sqlBoth = WorkflowUtil.generateSqlQuery(validParams, tableAlias);
            System.out.println("\n第二步 - 生成SQL查询条件:");
            System.out.println("默认模式(等于+模糊): " + sqlBoth);
            if(oConvertUtils.isNotEmpty(sqlBoth))
                myParam.put("paramQueryCondition", sqlBoth);
            // 创建分页对象
            Page<Map<String, Object>> page = new Page<>(Integer.valueOf(myParam.get("pageNo").toString()), Integer.valueOf(myParam.get("pageSize").toString()));
            // 分页查询方法并返回数据列表
            IPage<Map<String, Object>> pageResult = wfTaskMapper.getMyTasksMultiProcess(page, myParam);
            return pageResult;
        } catch (Exception e) {
            log.error("查询我的任务列表失败", e);
            throw new RuntimeException("查询我的任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public IPage<Map<String, Object>> relateMultiProcess(Map<String, Object> relateParam) {
        if(!relateParam.containsKey("queryAll")) relateParam.put("queryAll", "0");
        try {
            // 1. 查询流程定义
            List<WfProcess> processList = wfProcessMapper.selectList(new QueryWrapper<WfProcess>());
            List<Long> processIdList = new ArrayList<>();
            for(WfProcess wfProcess : processList){
                processIdList.add(Long.valueOf(wfProcess.getId()));
            }
            relateParam.put("tableName", processList.get(0).getTableName());
            relateParam.put("processIds", processIdList);
            // 第一步：根据实体类过滤有效字段
            Map<String, Object> validParams = WorkflowUtil.filterEntityFields(processList.get(0).getClassName(), relateParam);
            // 第二步：基于有效字段生成SQL查询条件
            String tableAlias = "pa";
            // 生成默认查询模式（等于 + 模糊）
            String sqlBoth = WorkflowUtil.generateSqlQuery(validParams, tableAlias);
            System.out.println("\n第二步 - 生成SQL查询条件:");
            System.out.println("默认模式(等于+模糊): " + sqlBoth);
            if(oConvertUtils.isNotEmpty(sqlBoth))
                relateParam.put("paramQueryCondition", sqlBoth);
            // 创建分页对象
            Page<Map<String, Object>> page = new Page<>(Integer.valueOf(relateParam.get("pageNo").toString()), Integer.valueOf(relateParam.get("pageSize").toString()));
            // 分页查询方法并返回数据列表
            IPage<Map<String, Object>> pageResult = wfTaskMapper.getRelateTasksMultiProcess(page, relateParam);
            return pageResult;
        } catch (Exception e) {
            log.error("查询我的任务列表失败", e);
            throw new RuntimeException("查询我的任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public IPage<Map<String, Object>> my(Map<String, Object> myParam) {
        if(!myParam.containsKey("queryAll")) myParam.put("queryAll", "0");
        try {
            // 1. 查询流程定义
            WfProcess wfProcess = null;
            if (myParam.containsKey("processId")) {
                wfProcess = wfProcessService.getById(myParam.get("processId").toString());
                if (wfProcess == null) {
                    log.warn("未找到流程定义，processId: {}", myParam.get("processId").toString());
                    return new Page<>(Integer.valueOf(myParam.get("pageNum").toString()), Integer.valueOf(myParam.get("pageSize").toString()));
                }
            }
            myParam.put("tableName", wfProcess.getTableName());
            // 第一步：根据实体类过滤有效字段
            Map<String, Object> validParams = WorkflowUtil.filterEntityFields(wfProcess.getClassName(), myParam);
            // 第二步：基于有效字段生成SQL查询条件
            String tableAlias = "pa";
            // 生成默认查询模式（等于 + 模糊）
            String sqlBoth = WorkflowUtil.generateSqlQuery(validParams, tableAlias);
            System.out.println("\n第二步 - 生成SQL查询条件:");
            System.out.println("默认模式(等于+模糊): " + sqlBoth);
            if(oConvertUtils.isNotEmpty(sqlBoth))
                myParam.put("paramQueryCondition", sqlBoth);
            // 创建分页对象
            Page<Map<String, Object>> page = new Page<>(Integer.valueOf(myParam.get("pageNo").toString()), Integer.valueOf(myParam.get("pageSize").toString()));
            // 分页查询方法并返回数据列表
            IPage<Map<String, Object>> pageResult = wfTaskMapper.getMyTasks(page, myParam);
            return pageResult;
        } catch (Exception e) {
            log.error("查询我的任务列表失败", e);
            throw new RuntimeException("查询我的任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 所有任务
     * @param allParam
     * @return
     */
    @Override
    public IPage<Map<String, Object>> all(Map<String, Object> allParam) {
        if(!allParam.containsKey("queryAll")) allParam.put("queryAll", "0");
        try {
            // 1. 查询流程定义
            WfProcess wfProcess = null;
            if (allParam.containsKey("processId")) {
                wfProcess = wfProcessService.getById(allParam.get("processId").toString());
                if (wfProcess == null) {
                    log.warn("未找到流程定义，processId: {}", allParam.get("processId").toString());
                    return new Page<>(Integer.valueOf(allParam.get("pageNum").toString()), Integer.valueOf(allParam.get("pageSize").toString()));
                }
            }
            allParam.put("tableName", wfProcess.getTableName());
            // 第一步：根据实体类过滤有效字段
            Map<String, Object> validParams = WorkflowUtil.filterEntityFields(wfProcess.getClassName(), allParam);
            // 第二步：基于有效字段生成SQL查询条件
            String tableAlias = "pa";
            // 生成默认查询模式（等于 + 模糊）
            String sqlBoth = WorkflowUtil.generateSqlQuery(validParams, tableAlias);
            System.out.println("\n第二步 - 生成SQL查询条件:");
            System.out.println("默认模式(等于+模糊): " + sqlBoth);
            if(oConvertUtils.isNotEmpty(sqlBoth))
                allParam.put("paramQueryCondition", sqlBoth);
            // 创建分页对象
            Page<Map<String, Object>> page = new Page<>(Integer.valueOf(allParam.get("pageNo").toString()),
                    Integer.valueOf(allParam.get("pageSize").toString()));
            // 分页查询方法并返回数据列表
            IPage<Map<String, Object>> pageResult = wfTaskMapper.getAllTasks(page, allParam);
            return pageResult;
        } catch (Exception e) {
            log.error("查询所有任务列表失败", e);
            throw new RuntimeException("查询所有任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询待办任务
     * @param taskForm 查询条件
     * @return
     */
    @Override
    public IPage<Map<String, Object>> queryPendingTasks(TaskForm taskForm) {
        try {
            log.info("开始查询待办任务，查询条件：{}", taskForm);
            
            // 验证必要参数
            if (oConvertUtils.isEmpty(taskForm.getCurrentUserId()) && oConvertUtils.isEmpty(taskForm.getCurrentUserEmail())) {
                throw new IllegalArgumentException("当前用户ID或邮箱不能为空");
            }
            
            // 1. 查询流程定义
            WfProcess wfProcess = null;
            if (taskForm.getProcessId() != null) {
                wfProcess = wfProcessService.getById(taskForm.getProcessId());
                if (wfProcess == null) {
                    log.warn("未找到流程定义，processId: {}", taskForm.getProcessId());
                    return new Page<>(taskForm.getPageNum(), taskForm.getPageSize());
                }
            }
            
            // 2. 构建主查询SQL（关联查询）
            String mainSql = buildMainQuerySql(taskForm, wfProcess);
            
            // 3. 执行主查询，获取基础数据
            IPage<Map<String, Object>> mainResult = executeMainQuery(mainSql, taskForm);
            
            // 4. 如果需要包含业务参数数据，进行二次查询
            if (Boolean.TRUE.equals(taskForm.getIncludeBusinessData()) && wfProcess != null) {
                enrichWithBusinessData(mainResult.getRecords(), wfProcess);
            }
            
            log.info("待办任务查询完成，总数: {}, 当前页: {}/{}", 
                    mainResult.getTotal(), mainResult.getCurrent(), mainResult.getPages());
            
            return mainResult;
            
        } catch (Exception e) {
            log.error("查询待办任务失败", e);
            throw new RuntimeException("查询待办任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建主查询SQL
     */
    private String buildMainQuerySql(TaskForm taskForm, WfProcess wfProcess) {
        StringBuilder sql = new StringBuilder();
        
        sql.append("SELECT DISTINCT ");
        sql.append("  wo.id as order_id, ");
        sql.append("  wo.order_no, ");
        sql.append("  wo.order_status, ");
        sql.append("  wo.requester_id, ");
        sql.append("  wo.requester_name, ");
        sql.append("  wo.requester_email, ");
        sql.append("  wo.request_date, ");
        sql.append("  wo.create_time as order_create_time, ");
        sql.append("  wt.id as task_id, ");
        sql.append("  wt.name as task_name, ");
        sql.append("  wt.display_name as task_display_name, ");
        sql.append("  wt.task_status, ");
        sql.append("  wt.operator_id, ");
        sql.append("  wt.operator_email, ");
        sql.append("  wt.create_time as task_create_time, ");
        if (wfProcess != null) {
            sql.append("  wp.id as process_id, ");
            sql.append("  wp.name as process_name, ");
            sql.append("  wp.display_name as process_display_name, ");
            sql.append("  wp.class_name, ");
            sql.append("  wp.table_name ");
        } else {
            sql.append("  wp.id as process_id, ");
            sql.append("  wp.name as process_name, ");
            sql.append("  wp.display_name as process_display_name, ");
            sql.append("  wp.class_name, ");
            sql.append("  wp.table_name ");
        }
        
        sql.append("FROM wf_task wt ");
        sql.append("INNER JOIN wf_order wo ON wt.order_id = wo.id ");
        sql.append("INNER JOIN wf_process wp ON wo.process_id = wp.id ");
        
        sql.append("WHERE 1=1 ");
        
        // 待办任务：任务状态为等待操作
        sql.append("AND wt.task_status = '").append(TaskStatusEnum.WAITING.getCode()).append("' ");
        
        // 当前登录人的任务
        if (oConvertUtils.isNotEmpty(taskForm.getCurrentUserId())) {
            sql.append("AND (wt.operator_id = '").append(taskForm.getCurrentUserId()).append("' ");
            if (oConvertUtils.isNotEmpty(taskForm.getCurrentUserEmail())) {
                sql.append("OR wt.operator_email = '").append(taskForm.getCurrentUserEmail()).append("' ");
            }
            sql.append(") ");
        } else if (oConvertUtils.isNotEmpty(taskForm.getCurrentUserEmail())) {
            sql.append("AND wt.operator_email = '").append(taskForm.getCurrentUserEmail()).append("' ");
        }
        
        // 流程定义条件
        if (taskForm.getProcessId() != null) {
            sql.append("AND wo.process_id = ").append(taskForm.getProcessId()).append(" ");
        }
        
        // 其他可选查询条件
        addOptionalConditions(sql, taskForm);
        
        // 排序
        addOrderBy(sql, taskForm);
        
        return sql.toString();
    }

    /**
     * 添加可选查询条件
     */
    private void addOptionalConditions(StringBuilder sql, TaskForm taskForm) {
        if (oConvertUtils.isNotEmpty(taskForm.getRequesterId())) {
            sql.append("AND wo.requester_id = '").append(taskForm.getRequesterId()).append("' ");
        }
        
        if (oConvertUtils.isNotEmpty(taskForm.getRequesterName())) {
            sql.append("AND wo.requester_name LIKE '%").append(taskForm.getRequesterName()).append("%' ");
        }
        
        if (oConvertUtils.isNotEmpty(taskForm.getOrderStatus())) {
            sql.append("AND wo.order_status = '").append(taskForm.getOrderStatus()).append("' ");
        }
        
        if (oConvertUtils.isNotEmpty(taskForm.getProcessName())) {
            sql.append("AND (wp.name LIKE '%").append(taskForm.getProcessName()).append("%' ");
            sql.append("OR wp.display_name LIKE '%").append(taskForm.getProcessName()).append("%') ");
        }
        
        if (taskForm.getStartTime() != null) {
            sql.append("AND wo.request_date >= '").append(taskForm.getStartTime()).append("' ");
        }
        
        if (taskForm.getEndTime() != null) {
            sql.append("AND wo.request_date <= '").append(taskForm.getEndTime()).append("' ");
        }
        
        // 动态查询条件
        if (taskForm.getDynamicConditions() != null && !taskForm.getDynamicConditions().isEmpty()) {
            for (Map.Entry<String, Object> entry : taskForm.getDynamicConditions().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null) {
                    sql.append("AND ").append(key).append(" = '").append(value).append("' ");
                }
            }
        }
    }

    /**
     * 添加排序条件
     */
    private void addOrderBy(StringBuilder sql, TaskForm taskForm) {
        sql.append("ORDER BY ");
        
        String sortField = taskForm.getSortField();
        if (oConvertUtils.isEmpty(sortField)) {
            sortField = "wt.create_time";
        }
        
        // 映射排序字段
        switch (sortField) {
            case "create_time":
                sortField = "wt.create_time";
                break;
            case "order_create_time":
                sortField = "wo.create_time";
                break;
            case "request_date":
                sortField = "wo.request_date";
                break;
            case "requester_name":
                sortField = "wo.requester_name";
                break;
            case "process_name":
                sortField = "wp.display_name";
                break;
            default:
                sortField = "wt.create_time";
        }
        
        sql.append(sortField).append(" ");
        
        String sortOrder = taskForm.getSortOrder();
        if (oConvertUtils.isEmpty(sortOrder) || 
            (!CommonConstant.ORDER_TYPE_ASC.equalsIgnoreCase(sortOrder) && 
             !CommonConstant.ORDER_TYPE_DESC.equalsIgnoreCase(sortOrder))) {
            sortOrder = CommonConstant.ORDER_TYPE_DESC;
        }
        
        sql.append(sortOrder);
    }

    /**
     * 执行主查询
     */
    private IPage<Map<String, Object>> executeMainQuery(String sql, TaskForm taskForm) {
        try {
            log.info("执行待办任务主查询，参数：{}", taskForm);
            
            // 创建分页对象
            Page<Map<String, Object>> page = new Page<>(taskForm.getPageNum(), taskForm.getPageSize());
            
            // 格式化时间参数
            String startTimeStr = null;
            String endTimeStr = null;
            if (taskForm.getStartTime() != null) {
                startTimeStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(taskForm.getStartTime());
            }
            if (taskForm.getEndTime() != null) {
                endTimeStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(taskForm.getEndTime());
            }
            
            // 调用自定义Mapper方法
            IPage<Map<String, Object>> result = this.baseMapper.queryPendingTasksPage(
                page,
                taskForm.getCurrentUserId(),
                taskForm.getCurrentUserEmail(),
                taskForm.getProcessId(),
                taskForm.getRequesterId(),
                taskForm.getRequesterName(),
                taskForm.getOrderStatus(),
                taskForm.getProcessName(),
                startTimeStr,
                endTimeStr,
                taskForm.getSortField(),
                taskForm.getSortOrder()
            );
            
            log.info("主查询执行完成，总数: {}, 当前页: {}", result.getTotal(), result.getCurrent());
            
            return result;
            
        } catch (Exception e) {
            log.error("执行主查询失败", e);
            throw new RuntimeException("执行主查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * enrichWithBusinessData - 使用业务参数数据丰富结果
     */
    private void enrichWithBusinessData(List<Map<String, Object>> records, WfProcess wfProcess) {
        if (records.isEmpty() || oConvertUtils.isEmpty(wfProcess.getClassName())) {
            return;
        }
        
        log.info("开始查询业务参数数据，实体类: {}", wfProcess.getClassName());
        
        try {
            for (Map<String, Object> record : records) {
                Object orderId = record.get("order_id");
                if (orderId != null) {
                    // 查询业务参数数据
                    QueryWrapper<Object> paramQuery = new QueryWrapper<>();
                    paramQuery.eq("order_id", orderId);
                    
                    List<Map<String, Object>> businessData = workflowDynamicService.selectMaps(
                        wfProcess.getClassName(), 
                        paramQuery
                    );
                    
                    // 将业务数据添加到记录中
                    if (!businessData.isEmpty()) {
                        record.put("business_data", businessData.get(0)); // 假设一个订单对应一条业务数据
                        record.put("business_data_list", businessData); // 完整的业务数据列表
                    }
                }
            }
            
            log.info("业务参数数据查询完成");
            
        } catch (Exception e) {
            log.error("查询业务参数数据失败", e);
            // 不抛异常，只记录日志，避免影响主查询结果
        }
    }

}
