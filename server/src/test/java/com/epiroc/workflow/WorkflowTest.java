package com.epiroc.workflow;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epiroc.workflow.common.convert.WfFlow2WfTaskParticipant;
import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.entity.param.BatchOperateParam;
import com.epiroc.workflow.common.service.WfFlowService;
import com.epiroc.workflow.common.service.WfProcessService;
import com.epiroc.workflow.common.service.WorkflowService;
import com.epiroc.workflow.common.system.flow.FlowContext;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import com.epiroc.workflow.common.util.WorkflowUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作流测试
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-14
 */
@SpringBootTest
public class WorkflowTest {

    @Resource
    private WorkflowService workflowService;

    @Resource
    private WfFlowService wfFlowService;

    @Resource
    private WfProcessService wfProcessService;


    @Test
    public void testSubmit() {
        FlowParam flowParam = new FlowParam();

        FlowContext flowContext = new FlowContext("2,0", wfFlowService);
        flowParam.setStage("1");
        flowParam.setWfProcessId(95);
        flowParam.setUnitId("1");
        flowParam.setAmount(new BigDecimal("6000"));
        Map<String, Object> testMap = flowContext.getFlowInfoResult(new HashMap<>(), flowParam);
        List<WfFlow> flowList = (List<WfFlow>) testMap.get("flowList");
        // 转换
        Map<String, Object> assigneeMap = new HashMap<>();
        List<WfTaskParticipant> flowResultList = WfFlow2WfTaskParticipant.getSubmitWfTaskParticipants(flowList, assigneeMap);
        OperateParam operateParam = new OperateParam();
        operateParam.setFlowList(flowResultList);
        operateParam.setWfProcessId(95);
        operateParam.setOrderStatus("TO_BE_SUBMIT");
        operateParam.setOperateType("SUBMIT");
        Map<String, Object> resultMap = workflowService.operateByHandler(operateParam);
    }

    @Test
    public void testDictDataLoad() {
        // 这个测试主要是验证字典数据是否正确加载，不会抛出异常即为成功
        System.out.println("字典数据加载测试完成");
    }

    /**
     * 测试批量提交 - 顺序处理模式
     */
    @Test
    public void testBatchSubmitSequential() {
        WfProcess wfProcess = wfProcessService.getById(96);
        FlowParam flowParam = new FlowParam();
        FlowContext flowContext = new FlowContext(wfProcess.getFlowTypes(), wfFlowService);
        flowParam.setStage("1");
        flowParam.setWfProcessId(96);
//        QueryWrapper<SysDepart> sysDepartQueryWrapper =new QueryWrapper<SysDepart>().eq("depart_name", tneVatCheckStartApprovalForm.getUnit());
//        SysDepart sysDepart = sysDepartMapper.selectOne(sysDepartQueryWrapper);
//        flowParam.setUnitId(sysDepart.getId());
        Map<String, Object> flowMap = flowContext.getFlowInfoResult(new HashMap<>(), flowParam);
        List<WfFlow> flowList = (List<WfFlow>) flowMap.get("flowList");

        // 创建批量操作参数
        BatchOperateParam batchParam = new BatchOperateParam();
        batchParam.setBatchOperator("测试管理员");
        batchParam.setBatchOperatorEmail("test@epiroc.com");
        batchParam.setBatchOperatorId("TEST001");
        batchParam.setBatchComment("批量提交测试 - 顺序处理");
        batchParam.setIgnoreErrors(false); // 顺序处理，遇错即停

        // 创建操作参数列表
        List<OperateParam> operateParams = new ArrayList<>();
        
        // 添加第一个提交
        OperateParam operateParam = createTestOperateParam("张三", "zhangsan@epiroc.com", "U001",
                "李四", "lisi@epiroc.com", "U002");

        Map<String, Object> assigneeMap = new HashMap<>();
        assigneeMap.put("REQUESTER", "张三");
        assigneeMap.put("REQUESTER_GUID", "U002");
        assigneeMap.put("REQUESTER_EMAIL", "lisi@epiroc.com");
        assigneeMap.put("Confirmer", "李四");
        assigneeMap.put("Confirmer_GUID", "U002");
        assigneeMap.put("Confirmer_EMAIL", "lisi@epiroc.com");
        List<WfTaskParticipant> flowResultList = WfFlow2WfTaskParticipant.getSubmitWfTaskParticipants(flowList, assigneeMap);
        operateParam.setFlowList(flowResultList);
        operateParams.add(operateParam);
                // 添加第二个提交
        OperateParam operateParam1 = createTestOperateParam("王五", "wangwu@epiroc.com", "U003",
                                                "赵六", "zhaoliu@epiroc.com", "U004");
        Map<String, Object> assigneeMap1 = new HashMap<>();
        assigneeMap1.put("REQUESTER", "张三");
        assigneeMap1.put("REQUESTER_GUID", "U002");
        assigneeMap1.put("REQUESTER_EMAIL", "lisi@epiroc.com");
        assigneeMap1.put("Confirmer", "李四1");
        assigneeMap1.put("Confirmer_GUID", "U0021");
        assigneeMap1.put("Confirmer_EMAIL", "lisi1@epiroc.com");
        List<WfTaskParticipant> flowResultList1 = WfFlow2WfTaskParticipant.getSubmitWfTaskParticipants(flowList, assigneeMap1);
        operateParam1.setFlowList(flowResultList1);
        operateParams.add(operateParam1);

        batchParam.setOperateParams(operateParams);

        // 执行批量提交
        Map<String, Object> result = workflowService.batchSubmit(batchParam);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.get("totalCount"));
        assertNotNull(result.get("successCount"));
        assertNotNull(result.get("failureCount"));
        assertNotNull(result.get("results"));
        
        System.out.println("批量提交结果（顺序处理）:");
        System.out.println("总数: " + result.get("totalCount"));
        System.out.println("成功数: " + result.get("successCount"));
        System.out.println("失败数: " + result.get("failureCount"));
        
        // 打印详细结果
        List<BatchOperateParam.BatchOperateResult> results = 
            (List<BatchOperateParam.BatchOperateResult>) result.get("results");
        for (BatchOperateParam.BatchOperateResult batchResult : results) {
            System.out.println("订单ID: " + batchResult.getOrderId() + 
                             ", 成功: " + batchResult.isSuccess() + 
                             ", 错误信息: " + batchResult.getErrorMessage());
        }
    }

    /**
     * 测试批量提交 - 并发处理模式
     */
    @Test
    public void testBatchSubmitConcurrent() {
        // 创建批量操作参数
        BatchOperateParam batchParam = new BatchOperateParam();
        batchParam.setBatchOperator("系统管理员");
        batchParam.setBatchOperatorEmail("admin@epiroc.com");
        batchParam.setBatchOperatorId("ADMIN");
        batchParam.setBatchComment("批量提交测试 - 并发处理");
        batchParam.setIgnoreErrors(true); // 并发处理，忽略错误

        // 创建更多的操作参数来测试并发
        List<OperateParam> operateParams = new ArrayList<>();
        
        // 添加5个提交任务
        for (int i = 1; i <= 5; i++) {
            String creatorName = "用户" + i;
            String creatorEmail = "user" + i + "@epiroc.com";
            String creatorId = "U00" + i;
            String approverName = "审批人" + i;
            String approverEmail = "approver" + i + "@epiroc.com";
            String approverId = "A00" + i;
            
            operateParams.add(createTestOperateParam(creatorName, creatorEmail, creatorId,
                                                    approverName, approverEmail, approverId));
        }
        
        batchParam.setOperateParams(operateParams);

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 执行批量提交
        Map<String, Object> result = workflowService.batchSubmit(batchParam);
        
        // 记录结束时间
        long endTime = System.currentTimeMillis();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(5, result.get("totalCount"));
        
        System.out.println("批量提交结果（并发处理）:");
        System.out.println("总数: " + result.get("totalCount"));
        System.out.println("成功数: " + result.get("successCount"));
        System.out.println("失败数: " + result.get("failureCount"));
        System.out.println("执行时间: " + (endTime - startTime) + "ms");
    }

    /**
     * 测试批量提交 - 空参数
     */
    @Test
    public void testBatchSubmitEmptyParams() {
        // 创建空的批量操作参数
        BatchOperateParam batchParam = new BatchOperateParam();
        batchParam.setOperateParams(new ArrayList<>());

        // 执行批量提交
        Map<String, Object> result = workflowService.batchSubmit(batchParam);
        
        // 验证结果
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("批量操作参数不能为空", result.get("message"));
        
        System.out.println("空参数测试结果: " + result.get("message"));
    }

    /**
     * 测试批量提交 - 包含业务参数
     */
    @Test
    public void testBatchSubmitWithBusinessParams() {

        // 创建批量操作参数
        BatchOperateParam batchParam = new BatchOperateParam();
        batchParam.setBatchOperator("业务管理员");
        batchParam.setBatchOperatorEmail("business@epiroc.com");
        batchParam.setBatchOperatorId("BUS001");
        batchParam.setIgnoreErrors(false);

        // 创建带有业务参数的操作
        List<OperateParam> operateParams = new ArrayList<>();
        
        // 第一个提交 - 带单个参数
        OperateParam param1 = createTestOperateParam("张三", "zhangsan@epiroc.com", "U001",
                                                    "李四", "lisi@epiroc.com", "U002");
        Map<String, Object> businessParam1 = new HashMap<>();
        businessParam1.put("amount", new BigDecimal("10000"));
        businessParam1.put("reason", "采购申请");
        businessParam1.put("department", "IT部门");
        param1.setParam(businessParam1);
        operateParams.add(param1);
        
        // 第二个提交 - 带参数列表
        OperateParam param2 = createTestOperateParam("王五", "wangwu@epiroc.com", "U003",
                                                    "赵六", "zhaoliu@epiroc.com", "U004");
        List<Map<String, Object>> businessParamList = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("itemName", "电脑");
        item1.put("quantity", 5);
        item1.put("price", new BigDecimal("5000"));
        businessParamList.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("itemName", "显示器");
        item2.put("quantity", 5);
        item2.put("price", new BigDecimal("2000"));
        businessParamList.add(item2);
        
        param2.setParamList(businessParamList);
        operateParams.add(param2);
        
        batchParam.setOperateParams(operateParams);

        // 执行批量提交
        Map<String, Object> result = workflowService.batchSubmit(batchParam);
        
        // 验证结果
        assertNotNull(result);
        System.out.println("带业务参数的批量提交结果:");
        System.out.println("成功: " + result.get("success"));
        System.out.println("总数: " + result.get("totalCount"));
        System.out.println("成功数: " + result.get("successCount"));
    }

    /**
     * 创建测试用的操作参数
     */
    private OperateParam createTestOperateParam(String creatorName, String creatorEmail, String creatorId,
                                               String approverName, String approverEmail, String approverId) {
        OperateParam operateParam = new OperateParam();
        
        // 设置流程信息
        operateParam.setWfProcessId(96); // 使用测试流程ID
        operateParam.setClassName("com.epiroc.workflow.common.entity.ScParam");
        operateParam.setOrderStatus("TO_BE_SUBMIT");
        operateParam.setOperateType("SUBMIT");
        
        // 设置创建人信息
        operateParam.setCreatorId(creatorId);
        operateParam.setCreatorName(creatorName);
        operateParam.setCreatorEmail(creatorEmail);
        
        // 设置申请人信息（与创建人相同）
        operateParam.setRequesterId(creatorId);
        operateParam.setRequesterName(creatorName);
        operateParam.setRequesterEmail(creatorEmail);
        
        // 创建流程列表
        List<WfTaskParticipant> flowList = new ArrayList<>();
        
        // 第一个节点 - 申请人
        WfTaskParticipant participant1 = new WfTaskParticipant();
        participant1.setOperator(creatorName);
        participant1.setOperatorId(creatorId);
        participant1.setOperatorEmail(creatorEmail);
        participant1.setSortOrder(1);
        flowList.add(participant1);
        
        // 第二个节点 - 审批人
        WfTaskParticipant participant2 = new WfTaskParticipant();
        participant2.setOperator(approverName);
        participant2.setOperatorId(approverId);
        participant2.setOperatorEmail(approverEmail);
        participant2.setSortOrder(2);
        flowList.add(participant2);
        
        operateParam.setFlowList(flowList);
        
        return operateParam;
    }

    @Test
    public void testGenerateSqlQuery() {
        // 测试基本功能
        Map<String, Object> params = new HashMap<>();
        params.put("costCenter", "test");
        params.put("userName", "张三");
        params.put("update_by", "admin");  // 已经是下划线格式
        
        // 测试默认模式（BOTH）
        String sql = WorkflowUtil.generateSqlQuery(params, "wpd");
        System.out.println("Default mode SQL: " + sql);
        
        // 测试EQUAL模式
        String sqlEqual = WorkflowUtil.generateSqlQuery(params, "wpd", WorkflowUtil.QueryMode.EQUAL);
        System.out.println("Equal mode SQL: " + sqlEqual);
        
        // 测试LIKE模式
        String sqlLike = WorkflowUtil.generateSqlQuery(params, "wpd", WorkflowUtil.QueryMode.LIKE);
        System.out.println("Like mode SQL: " + sqlLike);
        
        // 测试无表别名
        String sqlNoAlias = WorkflowUtil.generateSqlQuery(params, null);
        System.out.println("No alias SQL: " + sqlNoAlias);
        
        // 测试空参数
        String sqlEmpty = WorkflowUtil.generateSqlQuery(new HashMap<>(), "wpd");
        System.out.println("Empty params SQL: " + sqlEmpty);
        
        // 测试包含特殊字符的值
        Map<String, Object> specialParams = new HashMap<>();
        specialParams.put("description", "test'value;--comment");
        String sqlSpecial = WorkflowUtil.generateSqlQuery(specialParams, "t");
        System.out.println("Special chars SQL: " + sqlSpecial);
    }

    @Test
    public void testFilterEntityFields() {
        // 测试基本功能 - 使用ScParam实体类
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("supplierName", "测试供应商");  // 实际存在的字段
        params.put("supplier_name", "测试供应商");  // 下划线格式
        params.put("contact", "张三");  // 实际存在的字段
        params.put("invalidField", "无效字段");  // 实体类中不存在的字段
        params.put("requester", "test@epiroc.com");  // 实际存在的字段
        params.put("nonExistentField", "不存在");  // 不存在的字段
        
        // 测试字符串类名方式
        String entityClassName = "com.epiroc.workflow.common.entity.ScParam";
        Map<String, Object> filteredParams = WorkflowUtil.filterEntityFields(entityClassName, params);
        
        System.out.println("原始参数: " + params);
        System.out.println("过滤后参数: " + filteredParams);
        System.out.println("过滤前字段数: " + params.size());
        System.out.println("过滤后字段数: " + filteredParams.size());
        
        // 验证结果 - 无效字段应该被过滤掉
        assertFalse(filteredParams.containsKey("invalidField"));
        assertFalse(filteredParams.containsKey("nonExistentField"));
        
        // 测试Class对象方式
        try {
            Class<?> entityClass = Class.forName(entityClassName);
            Map<String, Object> filteredParams2 = WorkflowUtil.filterEntityFields(entityClass, params);
            System.out.println("使用Class对象过滤后参数: " + filteredParams2);
            
            // 两种方式结果应该一致
            assertEquals(filteredParams.size(), filteredParams2.size());
        } catch (ClassNotFoundException e) {
            System.out.println("找不到实体类: " + entityClassName);
        }
        
        // 测试空参数
        Map<String, Object> emptyResult = WorkflowUtil.filterEntityFields(entityClassName, new HashMap<>());
        assertTrue(emptyResult.isEmpty());
        System.out.println("空参数测试通过");
        
        // 测试null参数
        Map<String, Object> nullResult = WorkflowUtil.filterEntityFields(entityClassName, null);
        assertTrue(nullResult.isEmpty());
        System.out.println("null参数测试通过");
        
        // 测试不存在的类
        try {
            WorkflowUtil.filterEntityFields("com.example.NonExistentClass", params);
            fail("应该抛出异常");
        } catch (IllegalArgumentException e) {
            System.out.println("不存在的类测试通过: " + e.getMessage());
        }
        
        // 测试使用WfTask实体类
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("id", 1);
        taskParams.put("name", "测试任务");
        taskParams.put("taskStatus", "PENDING");
        taskParams.put("task_status", "PENDING");  // 下划线格式
        taskParams.put("operator", "张三");  // 实际存在的字段
        taskParams.put("invalidTaskField", "无效");
        
        Map<String, Object> filteredTaskParams = WorkflowUtil.filterEntityFields(
            "com.epiroc.workflow.common.entity.WfTask", taskParams);
        
        System.out.println("WfTask原始参数: " + taskParams);
        System.out.println("WfTask过滤后参数: " + filteredTaskParams);
        
        // 无效字段应该被过滤掉
        assertFalse(filteredTaskParams.containsKey("invalidTaskField"));
    }

    @Test
    public void testCombinedEntityFilterAndSqlGeneration() {
        System.out.println("=== 综合测试：实体字段过滤 + SQL查询生成 ===");
        
        // 模拟前端传来的查询参数（包含无效字段和List类型）
        Map<String, Object> frontendParams = new HashMap<>();
        frontendParams.put("id", 123);
        frontendParams.put("supplierName", "测试供应商");  // 实际存在的字段
        frontendParams.put("contact", "张三");  // 实际存在的字段
        frontendParams.put("email", "test@epiroc.com");  // 实际存在的字段
        frontendParams.put("unit", Arrays.asList("IT部门", "财务部门", "采购部门"));  // List类型字段
        frontendParams.put("invalidField1", "无效字段1");  // 实体类中不存在
        frontendParams.put("invalidField2", "无效字段2");  // 实体类中不存在
        frontendParams.put("hackAttempt", "'; DROP TABLE users; --");  // 恶意字段
        
        System.out.println("前端传入参数: " + frontendParams);
        System.out.println("参数数量: " + frontendParams.size());
        
        // 第一步：根据实体类过滤有效字段
        String entityClassName = "com.epiroc.workflow.common.entity.ScParam";
        Map<String, Object> validParams = WorkflowUtil.filterEntityFields(entityClassName, frontendParams);
        
        System.out.println("\n第一步 - 字段过滤后:");
        System.out.println("有效参数: " + validParams);
        System.out.println("有效参数数量: " + validParams.size());
        
        // 第二步：基于有效字段生成SQL查询条件
        String tableAlias = "sp";
        
        // 生成默认查询模式（等于 + 模糊）
        String sqlBoth = WorkflowUtil.generateSqlQuery(validParams, tableAlias);
        System.out.println("\n第二步 - 生成SQL查询条件:");
        System.out.println("默认模式(等于+模糊): " + sqlBoth);
        
        // 生成只等于查询
        String sqlEqual = WorkflowUtil.generateSqlQuery(validParams, tableAlias, WorkflowUtil.QueryMode.EQUAL);
        System.out.println("等于模式: " + sqlEqual);
        
        // 生成只模糊查询
        String sqlLike = WorkflowUtil.generateSqlQuery(validParams, tableAlias, WorkflowUtil.QueryMode.LIKE);
        System.out.println("模糊模式: " + sqlLike);
        
        // 模拟在Mapper中使用
        System.out.println("\n第三步 - Mapper中使用示例:");
        String fullSql = "SELECT * FROM sc_param sp WHERE " + sqlEqual + " ORDER BY sp.create_time DESC";
        System.out.println("完整SQL: " + fullSql);
        
        // 验证安全性：确保无效字段和恶意内容被过滤掉
        assertFalse(validParams.containsKey("invalidField1"), "无效字段应该被过滤");
        assertFalse(validParams.containsKey("invalidField2"), "无效字段应该被过滤");
        assertFalse(validParams.containsKey("hackAttempt"), "恶意字段应该被过滤");
        assertFalse(sqlBoth.contains("DROP TABLE"), "SQL中不应包含恶意内容");
        
        // 测试不同实体类的场景
        System.out.println("\n=== 测试其他实体类 ===");
        
        // 测试WfTask实体
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("name", "审批任务");  // 实际存在的字段
        taskParams.put("taskStatus", "PENDING");
        taskParams.put("operatorId", "U001");
        taskParams.put("operator", "张三");  // 实际存在的字段
        taskParams.put("invalidTaskField", "无效");
        
        Map<String, Object> validTaskParams = WorkflowUtil.filterEntityFields(
            "com.epiroc.workflow.common.entity.WfTask", taskParams);
        String taskSql = WorkflowUtil.generateSqlQuery(validTaskParams, "wt", WorkflowUtil.QueryMode.BOTH);
        
        System.out.println("WfTask有效参数: " + validTaskParams);
        System.out.println("WfTask SQL: " + taskSql);
        
        // 验证结果
        assertTrue(validParams.containsKey("supplierName"), "应该包含有效字段");
        assertTrue(validTaskParams.containsKey("taskStatus"), "应该包含有效字段");
        assertFalse(validTaskParams.containsKey("invalidTaskField"), "应该过滤无效字段");
        
        System.out.println("\n综合测试完成！");
    }

    @Test
    public void testGenerateSqlQueryWithList() {
        System.out.println("=== 测试List类型IN查询功能 ===");
        
        // 测试包含List类型的查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("costCenter", "IT");  // 单个值
        params.put("userIds", Arrays.asList("001", "002", "003"));  // List类型
        params.put("departments", Arrays.asList("IT", "HR", "Finance"));  // List类型
        params.put("status", "ACTIVE");  // 单个值
        
        // 测试默认模式（BOTH）
        String sqlBoth = WorkflowUtil.generateSqlQuery(params, "emp");
        System.out.println("默认模式SQL: " + sqlBoth);
        
        // 测试EQUAL模式
        String sqlEqual = WorkflowUtil.generateSqlQuery(params, "emp", WorkflowUtil.QueryMode.EQUAL);
        System.out.println("等于模式SQL: " + sqlEqual);
        
        // 测试LIKE模式
        String sqlLike = WorkflowUtil.generateSqlQuery(params, "emp", WorkflowUtil.QueryMode.LIKE);
        System.out.println("模糊模式SQL: " + sqlLike);
        
        // 验证结果包含IN查询
        assertTrue(sqlBoth.contains("in ("), "应该包含IN查询");
        assertTrue(sqlBoth.contains("user_ids in ('001','002','003')"), "应该包含正确的IN查询");
        assertTrue(sqlBoth.contains("departments in ('IT','HR','Finance')"), "应该包含部门IN查询");
        
        // 测试空List
        Map<String, Object> emptyListParams = new HashMap<>();
        emptyListParams.put("name", "张三");
        emptyListParams.put("emptyList", new ArrayList<>());  // 空List应该被忽略
        
        String sqlEmptyList = WorkflowUtil.generateSqlQuery(emptyListParams, "t");
        System.out.println("空List测试SQL: " + sqlEmptyList);
        assertFalse(sqlEmptyList.contains("in ("), "空List不应该生成IN查询");
        assertTrue(sqlEmptyList.contains("name"), "应该包含非空字段");
        
        // 测试包含null值的List
        Map<String, Object> nullItemParams = new HashMap<>();
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add("001");
        listWithNull.add(null);  // null值
        listWithNull.add("003");
        nullItemParams.put("ids", listWithNull);
        
        String sqlNullItem = WorkflowUtil.generateSqlQuery(nullItemParams, "t");
        System.out.println("包含null的List SQL: " + sqlNullItem);
        assertTrue(sqlNullItem.contains("ids in ('001','003')"), "应该过滤掉null值");
        
        // 测试单元素List
        Map<String, Object> singleItemParams = new HashMap<>();
        singleItemParams.put("category", Arrays.asList("URGENT"));
        
        String sqlSingleItem = WorkflowUtil.generateSqlQuery(singleItemParams, "task");
        System.out.println("单元素List SQL: " + sqlSingleItem);
        assertTrue(sqlSingleItem.contains("category in ('URGENT')"), "单元素List也应该使用IN查询");
        
        // 测试List中包含特殊字符
        Map<String, Object> specialCharsParams = new HashMap<>();
        specialCharsParams.put("names", Arrays.asList("O'Connor", "D'Angelo", "test;drop"));
        
        String sqlSpecialChars = WorkflowUtil.generateSqlQuery(specialCharsParams, "user");
        System.out.println("特殊字符List SQL: " + sqlSpecialChars);
        // 验证特殊字符被正确转义
        assertTrue(sqlSpecialChars.contains("O''Connor"), "单引号应该被转义");
        assertFalse(sqlSpecialChars.contains("drop"), "危险字符应该被移除");
        
        System.out.println("List类型IN查询测试完成！");
    }

    @Test
    public void testCompleteUsageExample() {
        System.out.println("=== 完整使用示例演示 ===");
        
        // 模拟不同类型的查询场景
        System.out.println("\n场景1：员工查询");
        Map<String, Object> employeeQuery = new HashMap<>();
        employeeQuery.put("name", "张三");  // 单个值：等于或模糊查询
        employeeQuery.put("departments", Arrays.asList("IT", "HR"));  // List：IN查询
        employeeQuery.put("status", "ACTIVE");  // 单个值
        employeeQuery.put("levels", Arrays.asList("L1", "L2", "L3"));  // List：IN查询
        
        // 1. 过滤有效字段（假设使用WfTask实体）
        Map<String, Object> validEmployeeFields = WorkflowUtil.filterEntityFields(
            "com.epiroc.workflow.common.entity.WfTask", employeeQuery);
        
        // 2. 生成SQL查询
        String employeeSql = WorkflowUtil.generateSqlQuery(validEmployeeFields, "emp", WorkflowUtil.QueryMode.BOTH);
        System.out.println("员工查询SQL: " + employeeSql);
        System.out.println("完整查询语句: SELECT * FROM employee emp WHERE " + employeeSql);
        
        System.out.println("\n场景2：供应商查询");
        Map<String, Object> supplierQuery = new HashMap<>();
        supplierQuery.put("supplierName", "测试");  // 模糊查询
        supplierQuery.put("currency", Arrays.asList("CNY", "USD", "EUR"));  // IN查询
        supplierQuery.put("unit", "IT部门");  // 等于查询
        supplierQuery.put("paymentType", Arrays.asList("CASH", "BANK_TRANSFER"));  // IN查询
        
        Map<String, Object> validSupplierFields = WorkflowUtil.filterEntityFields(
            "com.epiroc.workflow.common.entity.ScParam", supplierQuery);
        
        String supplierSql = WorkflowUtil.generateSqlQuery(validSupplierFields, "sp", WorkflowUtil.QueryMode.EQUAL);
        System.out.println("供应商查询SQL: " + supplierSql);
        System.out.println("完整查询语句: SELECT * FROM sc_param sp WHERE " + supplierSql);
        
        System.out.println("\n场景3：任务查询（混合类型）");
        Map<String, Object> taskQuery = new HashMap<>();
        taskQuery.put("taskStatus", Arrays.asList("PENDING", "IN_PROGRESS"));  // 状态列表
        taskQuery.put("operator", "张三");  // 单个操作人
        taskQuery.put("operatorIds", Arrays.asList("U001", "U002", "U003"));  // 操作人ID列表
        taskQuery.put("name", "审批");  // 任务名称（模糊查询）
        
        Map<String, Object> validTaskFields = WorkflowUtil.filterEntityFields(
            "com.epiroc.workflow.common.entity.WfTask", taskQuery);
        
        String taskSql = WorkflowUtil.generateSqlQuery(validTaskFields, "wt", WorkflowUtil.QueryMode.LIKE);
        System.out.println("任务查询SQL: " + taskSql);
        System.out.println("完整查询语句: SELECT * FROM wf_task wt WHERE " + taskSql);
        
        // 验证关键功能
        System.out.println("\n功能验证:");
        
        // 验证IN查询
        assertTrue(employeeSql.contains("in (") || supplierSql.contains("in (") || taskSql.contains("in ("), 
                  "应该包含IN查询");
        
        // 验证字段转换
        if (validTaskFields.containsKey("taskStatus")) {
            assertTrue(taskSql.contains("task_status"), "驼峰字段应该转换为下划线");
        }
        
        // 验证过滤功能
        assertFalse(validEmployeeFields.containsKey("departments"), "departments不是WfTask的字段，应该被过滤");
        assertTrue(validSupplierFields.containsKey("supplierName"), "supplierName是ScParam的字段，应该保留");
        
        System.out.println("\n使用提示:");
        System.out.println("1. 单个字符串值：根据查询模式生成 = 或 LIKE 查询");
        System.out.println("2. List类型值：自动生成 IN 查询，忽略查询模式");
        System.out.println("3. 字段名自动转换：驼峰 ↔ 下划线");
        System.out.println("4. 安全防护：SQL注入防护、字段验证");
        System.out.println("5. 空值处理：自动跳过null、空字符串、空List");
        
        System.out.println("\n完整使用示例演示完成！");
    }

}
