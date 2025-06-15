package com.epiroc.workflow.common.config;

import com.epiroc.workflow.common.service.DatabaseTableInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 数据库表初始化器
 * 项目启动时检查workflow相关表是否存在，如果不存在则自动创建
 */
@Slf4j
@Component
public class DatabaseInitializer implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DatabaseTableInitService databaseTableInitService;
    
    @Autowired
    private WorkflowProperties workflowProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 检查是否启用数据库自动初始化
        if (!workflowProperties.getDatabase().isAutoInit()) {
            log.info("workflow数据库自动初始化已禁用");
            return;
        }
        
        log.info("开始检查workflow数据库表...");
        
        try {
            // 检查表是否存在
            List<String> missingTables = databaseTableInitService.checkMissingTables();
            
            if (missingTables.isEmpty()) {
                log.info("所有workflow表已存在，跳过初始化");
                return;
            }
            
            log.info("发现缺失的表: {}", missingTables);
            
            // 创建缺失的表
            createTables();
            
            log.info("workflow数据库表初始化完成");
            
        } catch (Exception e) {
            log.error("workflow数据库表初始化失败", e);
            if (workflowProperties.getDatabase().isFailOnError()) {
                throw e;
            } else {
                log.warn("忽略数据库初始化错误，继续启动应用");
            }
        }
    }



    /**
     * 创建表
     */
    private void createTables() throws IOException {
        log.info("开始创建workflow表...");
        
        // 读取SQL文件
        ClassPathResource resource = new ClassPathResource("sql/workflow.sql");
        byte[] sqlBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        String sqlContent = new String(sqlBytes, StandardCharsets.UTF_8);
        
        // 移除注释和SET语句，只保留CREATE TABLE语句
        String[] sqlStatements = sqlContent.split(";");
        
        for (String sql : sqlStatements) {
            sql = sql.trim();
            if (!sql.isEmpty() && sql.toUpperCase().startsWith("CREATE TABLE")) {
                try {
                    jdbcTemplate.execute(sql);
                    // 提取表名用于日志
                    String tableName = extractTableName(sql);
                    log.info("成功创建表: {}", tableName);
                } catch (Exception e) {
                    log.error("执行SQL失败: {}", sql.substring(0, Math.min(sql.length(), 100)) + "...", e);
                    throw e;
                }
            }
        }
        
        log.info("workflow表创建完成");
    }
    
    /**
     * 从CREATE TABLE语句中提取表名
     */
    private String extractTableName(String createSql) {
        try {
            String upperSql = createSql.toUpperCase();
            int startIndex = upperSql.indexOf("CREATE TABLE") + 12;
            int endIndex = upperSql.indexOf("(", startIndex);
            String tablePart = createSql.substring(startIndex, endIndex).trim();
            // 移除反引号和IF NOT EXISTS
            tablePart = tablePart.replace("`", "").replace("IF NOT EXISTS", "").trim();
            return tablePart;
        } catch (Exception e) {
            return "未知表名";
        }
    }
} 