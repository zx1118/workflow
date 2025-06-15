package com.epiroc.workflow.common.util;

import com.epiroc.workflow.common.service.DatabaseTableInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库初始化工具类
 * 提供手动检查和初始化数据库表的方法
 */
@Slf4j
@Component
public class DatabaseInitUtil {

    @Autowired
    private DatabaseTableInitService databaseTableInitService;

    /**
     * 检查所有workflow表的状态
     * @return 检查结果信息
     */
    public String checkAllTables() {
        StringBuilder result = new StringBuilder();
        result.append("Workflow表状态检查结果:\n");
        
        List<String> allTables = databaseTableInitService.getAllTableNames();
        List<String> missingTables = databaseTableInitService.checkMissingTables();
        
        result.append("总表数: ").append(allTables.size()).append("\n");
        result.append("已存在表数: ").append(allTables.size() - missingTables.size()).append("\n");
        result.append("缺失表数: ").append(missingTables.size()).append("\n");
        
        if (!missingTables.isEmpty()) {
            result.append("缺失的表: ").append(String.join(", ", missingTables)).append("\n");
        }
        
        result.append("详细状态:\n");
        for (String tableName : allTables) {
            boolean exists = databaseTableInitService.tableExists(tableName);
            result.append("  ").append(tableName).append(": ")
                  .append(exists ? "✓ 存在" : "✗ 不存在").append("\n");
        }
        
        return result.toString();
    }

    /**
     * 获取缺失的表列表
     * @return 缺失的表名列表
     */
    public List<String> getMissingTables() {
        return databaseTableInitService.checkMissingTables();
    }

    /**
     * 检查指定表是否存在
     * @param tableName 表名
     * @return 是否存在
     */
    public boolean isTableExists(String tableName) {
        return databaseTableInitService.tableExists(tableName);
    }
} 