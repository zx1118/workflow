# 待办任务查询功能使用说明

## 功能概述

此功能实现了一个完整的待办任务查询系统，支持：
- 分页查询
- 多条件过滤
- 动态业务参数查询
- 多表关联查询（wf_task、wf_order、wf_process）

## API 接口

### 1. 分页查询待办任务

**接口地址**: `POST /test/pending-tasks`

**请求参数** (TaskForm):
```json
{
  "processId": 1,                    // 流程定义ID（可选）
  "currentUserId": "user123",        // 当前用户ID（必填）
  "currentUserEmail": "user@epiroc.com", // 当前用户邮箱（可选，但与userId至少填一个）
  "currentUserName": "张三",          // 当前用户名（可选）
  "pageNum": 1,                      // 页码，从1开始（默认1）
  "pageSize": 10,                    // 每页大小（默认10）
  "sortField": "create_time",        // 排序字段（默认create_time）
  "sortOrder": "DESC",               // 排序方向：ASC/DESC（默认DESC）
  "requesterId": "req001",           // 申请人ID（可选）
  "requesterName": "李四",           // 申请人名称（可选，支持模糊查询）
  "orderStatus": "1",                // 订单状态（可选）
  "taskStatus": "1",                 // 任务状态（可选）
  "processName": "请假流程",          // 流程名称（可选，支持模糊查询）
  "startTime": "2025-01-01",         // 申请开始时间（可选）
  "endTime": "2025-12-31",           // 申请结束时间（可选）
  "includeBusinessData": true,       // 是否包含业务参数数据（默认false）
  "dynamicConditions": {             // 动态查询条件（可选）
    "custom_field": "custom_value"
  }
}
```

**响应结果**:
```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {
    "records": [
      {
        "order_id": 1,
        "order_no": "WF20250101001",
        "order_status": "1",
        "requester_id": "req001",
        "requester_name": "李四",
        "requester_email": "lisi@epiroc.com",
        "request_date": "2025-01-01",
        "order_create_time": "2025-01-01 10:00:00",
        "task_id": 1,
        "task_name": "部门主管审批",
        "task_display_name": "部门主管审批",
        "task_status": "1",
        "operator_id": "user123",
        "operator_email": "user@epiroc.com",
        "task_create_time": "2025-01-01 10:01:00",
        "process_id": 1,
        "process_name": "leave_process",
        "process_display_name": "请假流程",
        "class_name": "com.epiroc.workflow.entity.LeaveParam",
        "table_name": "leave_param",
        "business_data": {              // 当includeBusinessData=true时包含
          "leave_type": "年假",
          "leave_days": 3,
          "leave_reason": "家庭事务"
        }
      }
    ],
    "total": 15,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}
```

### 2. 不分页查询待办任务列表

**接口地址**: `POST /test/pending-list`

**请求参数**: 同上（但分页参数不生效）

**响应结果**: 直接返回任务列表数组

## 核心查询逻辑

### 1. 主查询SQL结构
```sql
SELECT DISTINCT
  wo.id as order_id,
  wo.order_no,
  wo.order_status,
  -- ... 其他字段
FROM wf_task wt
INNER JOIN wf_order wo ON wt.order_id = wo.id
INNER JOIN wf_process wp ON wo.process_id = wp.id
WHERE wt.task_status = '1'  -- 等待操作状态
  AND (wt.operator_id = ? OR wt.operator_email = ?)  -- 当前用户任务
  -- ... 其他动态条件
ORDER BY wt.create_time DESC
```

### 2. 业务参数查询
当 `includeBusinessData=true` 时，会根据 `wf_process.class_name` 动态查询对应的业务参数表：
```sql
SELECT * FROM {dynamic_table_name} WHERE order_id = ?
```

## 支持的排序字段

- `create_time`: 任务创建时间（默认）
- `order_create_time`: 订单创建时间
- `request_date`: 申请日期
- `requester_name`: 申请人姓名
- `process_name`: 流程名称

## 使用示例

### Java代码调用
```java
@Autowired
private WfTaskService wfTaskService;

public void queryMyPendingTasks() {
    TaskForm taskForm = new TaskForm();
    taskForm.setCurrentUserId("user123");
    taskForm.setCurrentUserEmail("user@epiroc.com");
    taskForm.setPageNum(1);
    taskForm.setPageSize(20);
    taskForm.setSortField("create_time");
    taskForm.setSortOrder("DESC");
    taskForm.setIncludeBusinessData(true);
    
    // 分页查询
    IPage<Map<String, Object>> result = wfTaskService.queryPendingTasks(taskForm);
    
    // 不分页查询
    List<Map<String, Object>> list = wfTaskService.pending(taskForm);
}
```

### HTTP请求示例
```bash
curl -X POST http://localhost:8080/test/pending-tasks \
  -H "Content-Type: application/json" \
  -d '{
    "currentUserId": "user123",
    "currentUserEmail": "user@epiroc.com",
    "pageNum": 1,
    "pageSize": 10,
    "includeBusinessData": true,
    "requesterName": "张",
    "processName": "请假"
  }'
```

## 数据表结构要求

### 核心表
1. **wf_task**: 工作流任务表
   - id: 主键
   - order_id: 关联订单ID
   - task_status: 任务状态（1=等待操作）
   - operator_id: 操作人ID
   - operator_email: 操作人邮箱

2. **wf_order**: 工作流订单表
   - id: 主键
   - process_id: 关联流程定义ID
   - requester_id: 申请人ID
   - requester_name: 申请人名称
   - order_status: 订单状态

3. **wf_process**: 工作流流程定义表
   - id: 主键
   - name: 流程名称
   - display_name: 显示名称
   - class_name: 对应的业务实体类全名
   - table_name: 对应的业务表名

### 动态业务表
根据 `wf_process.class_name` 确定的业务参数表，需要包含：
- order_id: 关联订单ID（用于关联查询）

## 注意事项

1. **用户身份验证**: 必须提供 `currentUserId` 或 `currentUserEmail` 之一
2. **任务状态**: 只查询状态为"1"（等待操作）的任务
3. **权限控制**: 只返回当前用户有权操作的任务
4. **性能优化**: 支持分页查询，避免大数据量查询
5. **业务数据**: 业务参数查询是可选的，避免不必要的性能开销
6. **错误处理**: 完整的异常处理和日志记录

## 扩展性

1. **自定义查询条件**: 通过 `dynamicConditions` 参数添加自定义查询条件
2. **多表关联**: 可以通过修改SQL扩展更多表的关联查询
3. **业务逻辑**: 可以在 `enrichWithBusinessData` 方法中添加更复杂的业务逻辑
4. **缓存支持**: 可以添加Redis缓存来提升查询性能

## 故障排除

1. **查询无结果**: 检查用户ID/邮箱是否正确，任务状态是否为等待操作
2. **业务数据为空**: 检查流程定义的 `class_name` 是否正确配置
3. **性能问题**: 考虑添加数据库索引，限制查询结果数量
4. **权限问题**: 确认当前用户是否有查看该任务的权限 