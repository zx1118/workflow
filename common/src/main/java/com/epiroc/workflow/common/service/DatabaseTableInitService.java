package com.epiroc.workflow.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 数据库表初始化服务
 * 提供workflow相关表的检查和创建功能
 */
@Slf4j
@Service
public class DatabaseTableInitService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * workflow相关的表名列表
     */
    private static final List<String> WORKFLOW_TABLES = Arrays.asList(
            "wf_dict",
            "wf_dict_item", 
            "wf_file",
            "wf_flow",
            "wf_key_user",
            "wf_order",
            "wf_process",
            "wf_rule",
            "wf_task",
            "wf_task_participant"
    );

    /**
     * 检查表是否存在
     */
    public boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("检查表 {} 是否存在时出错: {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * 检查缺失的表
     */
    public List<String> checkMissingTables() {
        return WORKFLOW_TABLES.stream()
                .filter(tableName -> !tableExists(tableName))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取所有workflow表名
     */
    public List<String> getAllTableNames() {
        return WORKFLOW_TABLES;
    }
} 