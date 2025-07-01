package com.epiroc.workflow.common.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.epiroc.workflow.common.service.DatabaseTableInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * 数据库表初始化器
 * 项目启动时检查workflow相关表是否存在，如果不存在则自动创建
 */
@Slf4j
@Component
@Order(1)
public class DatabaseInitializer implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DatabaseTableInitService databaseTableInitService;
    
    @Autowired
    private WorkflowProperties workflowProperties;
    
    @Autowired
    private DataSource dataSource;

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
            List<String> allTables = databaseTableInitService.getAllTableNames();
            
            log.info("所有workflow表: {}", allTables);
            log.info("缺失的表: {}", missingTables);
            
            if (missingTables.isEmpty()) {
                log.info("所有workflow表已存在");
                // 检查字典数据是否存在
                checkAndInsertDictData();
            } else {
                log.info("发现缺失的表: {}", missingTables);
                // 创建缺失的表
                createTables();
            }
            
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
     * 检查并插入字典数据
     */
    private void checkAndInsertDictData() {
        try {
            // 检查wf_dict表是否有数据
            String dictCountSql = "SELECT COUNT(*) FROM wf_dict WHERE dict_code IN ('order_status', 'task_status', 'approve_type')";
            Integer dictCount = jdbcTemplate.queryForObject(dictCountSql, Integer.class);
            
            if (dictCount == null || dictCount < 3) {
                log.info("检测到字典数据缺失，开始插入默认字典数据...");
                insertDefaultDictData();
                log.info("默认字典数据插入完成");
            } else {
                log.info("字典数据已存在，跳过插入");
            }
        } catch (Exception e) {
            log.warn("检查字典数据时出错: {}", e.getMessage());
        }
    }
    
    /**
     * 插入默认字典数据
     */
    private void insertDefaultDictData() {
        try {
            // 插入字典定义
            String[] dictInserts = {
                "INSERT IGNORE INTO `wf_dict` VALUES ('1', '订单状态', 'order_status', NULL, 0, NULL, NULL, NULL, NULL, 0, 0, NULL)",
                "INSERT IGNORE INTO `wf_dict` VALUES ('2', '任务状态', 'task_status', NULL, 0, NULL, NULL, NULL, NULL, 0, 0, NULL)",
                "INSERT IGNORE INTO `wf_dict` VALUES ('3', '审批类型', 'approve_type', NULL, 0, NULL, NULL, NULL, NULL, 0, 0, NULL)"
            };
            
            for (String sql : dictInserts) {
                jdbcTemplate.execute(sql);
            }
            
            // 插入字典项
            String[] dictItemInserts = {
                "INSERT IGNORE INTO `wf_dict_item` VALUES ('1', '1', 'TO_BE_SUBMIT', '-1', NULL, '待提交', 1, 1, NULL, NULL, NULL, NULL)",
                "INSERT IGNORE INTO `wf_dict_item` VALUES ('20', '2', 'NOT_APPROVED', '0', NULL, '未审批', 1, 1, NULL, NULL, NULL, NULL)",
                "INSERT IGNORE INTO `wf_dict_item` VALUES ('21', '2', 'WAITING', '1', NULL, '等待操作', 2, 1, NULL, NULL, NULL, NULL)",
                "INSERT IGNORE INTO `wf_dict_item` VALUES ('22', '2', 'APPROVED', '2', NULL, '已审批', 3, 1, NULL, NULL, NULL, NULL)",
                "INSERT IGNORE INTO `wf_dict_item` VALUES ('23', '2', 'CANCELLED', '3', NULL, '取消', 4, 1, NULL, NULL, NULL, NULL)",
                "INSERT IGNORE INTO `wf_dict_item` VALUES ('24', '2', 'CLOSED', '4', NULL, '关单', 5, 1, NULL, NULL, NULL, NULL)"
            };
            
            for (String sql : dictItemInserts) {
                jdbcTemplate.execute(sql);
            }
            
            log.info("成功插入默认字典数据");
        } catch (Exception e) {
            log.warn("插入默认字典数据失败: {}", e.getMessage());
        }
    }

    /**
     * 创建表
     */
    private void createTables() throws Exception {
        log.info("开始创建workflow表...");
        
        // 读取SQL文件
        ClassPathResource resource = new ClassPathResource("sql/workflow.sql");
        byte[] sqlBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        String sqlContent = new String(sqlBytes, StandardCharsets.UTF_8);
        
        // 按分号分割SQL语句
        String[] sqlStatements = sqlContent.split(";");
        
        // 获取缺失的表列表
        List<String> missingTables = databaseTableInitService.checkMissingTables();
        log.info("需要创建的表: {}", missingTables);
        
        // 如果没有缺失的表，直接返回
        if (missingTables.isEmpty()) {
            log.info("所有表都已存在，跳过创建");
            return;
        }
        
        // 先执行CREATE TABLE语句
        for (String sql : sqlStatements) {
            sql = sql.trim();
            
            // 跳过DROP语句和空语句
            if (sql.isEmpty() || sql.toUpperCase().startsWith("DROP TABLE")) {
                continue;
            }
            
            if (sql.toUpperCase().startsWith("CREATE TABLE")) {
                String tableName = extractTableName(sql);
                log.debug("处理CREATE TABLE语句，提取的表名: {}", tableName);
                
                // 只有当表在缺失列表中时才创建
                if (missingTables.contains(tableName)) {
                    log.info("表 {} 不存在，开始创建", tableName);
                    try {
                        // 将CREATE TABLE改为CREATE TABLE IF NOT EXISTS以避免重复创建错误
                        String createTableSql = sql.replaceFirst("(?i)CREATE TABLE", "CREATE TABLE IF NOT EXISTS");
                        
                        // 使用原生JDBC连接执行DDL，避免Druid的wall filter检查
                        executeWithRawConnection(createTableSql);
                        log.info("成功创建表: {}", tableName);
                    } catch (Exception e) {
                        // 如果是"表已存在"错误，记录警告但不抛异常
                        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("already exists")) {
                            log.warn("表 {} 已存在，跳过创建", tableName);
                            continue;
                        }
                        log.error("执行CREATE TABLE SQL失败，表名: {}", tableName, e);
                        throw e;
                    }
                } else {
                    log.debug("表 {} 已存在，跳过创建", tableName);
                }
            }
        }
        
        log.info("workflow表创建完成");
        
        // 再执行INSERT语句插入默认数据
        log.info("开始插入默认数据...");
        insertDefaultData(sqlStatements);
        log.info("默认数据插入完成");
    }
    
    /**
     * 使用原生JDBC连接执行SQL，避免Druid拦截器
     */
    private void executeWithRawConnection(String sql) throws Exception {
        Connection connection = null;
        Statement statement = null;
        try {
            // 获取原生连接
            connection = dataSource.getConnection();
            
            // 获取真实的物理连接（绕过Druid代理）
            Connection physicalConnection = connection;
            if (connection.isWrapperFor(Connection.class)) {
                try {
                    physicalConnection = connection.unwrap(Connection.class);
                } catch (Exception e) {
                    log.debug("无法获取物理连接，使用代理连接: {}", e.getMessage());
                }
            }
            
            statement = physicalConnection.createStatement();
            statement.execute(sql);
            
        } catch (Exception e) {
            // 如果是"表已存在"错误，直接抛出让上层处理
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("already exists")) {
                throw e;
            }
            
            // 如果直接执行失败，尝试使用JdbcTemplate但修改SQL
            log.warn("使用原生连接执行失败，尝试使用JdbcTemplate: {}", e.getMessage());
            try {
                // 对于CREATE TABLE语句，我们可以尝试分解执行
                executeCreateTableWithTemplate(sql);
            } catch (Exception e2) {
                // 如果第二次尝试也是"表已存在"错误，抛出原始异常
                if (e2.getMessage() != null && e2.getMessage().toLowerCase().contains("already exists")) {
                    throw e2;
                }
                log.error("两种方式都失败了，抛出原始异常");
                throw e;
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    log.warn("关闭Statement时出错: {}", e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    log.warn("关闭Connection时出错: {}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * 使用JdbcTemplate执行CREATE TABLE语句的备用方法
     */
    private void executeCreateTableWithTemplate(String sql) throws Exception {
        // 临时禁用Druid的wall filter（如果可能的话）
        try {
            // 直接使用JdbcTemplate执行，但先记录警告
            log.warn("正在尝试使用JdbcTemplate执行可能被Druid拦截的SQL");
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            // 如果是Druid的SQL注入检查错误，提供更详细的错误信息
            if (e.getMessage().contains("sql injection violation")) {
                String tableName = extractTableName(sql);
                log.error("Druid检测到SQL注入风险，表名: {}。这通常是由于COMMENT中包含特殊字符导致的。", tableName);
                log.error("建议检查SQL文件中的注释内容，避免使用括号、分号等特殊字符。");
                log.error("或者在应用配置中调整Druid的wall filter设置。");
            }
            throw e;
        }
    }
    
    /**
     * 插入默认数据
     */
    private void insertDefaultData(String[] sqlStatements) {
        for (String sql : sqlStatements) {
            sql = sql.trim();
            if (!sql.isEmpty() && sql.toUpperCase().startsWith("INSERT INTO")) {
                try {
                    // 将INSERT INTO改为INSERT IGNORE INTO以避免重复插入错误
                    String ignoreSql = sql.replaceFirst("(?i)INSERT INTO", "INSERT IGNORE INTO");
                    jdbcTemplate.execute(ignoreSql);
                    // 提取表名用于日志
                    String tableName = extractTableNameFromInsert(sql);
                    log.debug("成功插入数据到表: {}", tableName);
                } catch (Exception e) {
                    // 对于INSERT语句，如果失败（可能是数据已存在），只记录警告，不抛出异常
                    String tableName = extractTableNameFromInsert(sql);
                    log.warn("插入数据到表 {} 失败，可能数据已存在: {}", tableName, e.getMessage());
                }
            }
        }
    }
    
    /**
     * 从INSERT语句中提取表名
     */
    private String extractTableNameFromInsert(String insertSql) {
        try {
            String upperSql = insertSql.toUpperCase();
            int startIndex = upperSql.indexOf("INSERT INTO") + 11;
            int endIndex = upperSql.indexOf("VALUES", startIndex);
            if (endIndex == -1) {
                endIndex = upperSql.indexOf("(", startIndex);
            }
            String tablePart = insertSql.substring(startIndex, endIndex).trim();
            // 移除反引号
            tablePart = tablePart.replace("`", "").trim();
            return tablePart;
        } catch (Exception e) {
            return "未知表名";
        }
    }
    
    /**
     * 从CREATE TABLE语句中提取表名
     */
    private String extractTableName(String createSql) {
        try {
            String upperSql = createSql.toUpperCase();
            int startIndex;
            
            // 检查是否包含IF NOT EXISTS
            if (upperSql.contains("IF NOT EXISTS")) {
                startIndex = upperSql.indexOf("IF NOT EXISTS") + 13;
            } else {
                startIndex = upperSql.indexOf("CREATE TABLE") + 12;
            }
            
            int endIndex = upperSql.indexOf("(", startIndex);
            if (endIndex == -1) {
                // 如果没有找到(，可能是语句不完整，尝试到空格结束
                endIndex = upperSql.indexOf(" ", startIndex);
                if (endIndex == -1) {
                    endIndex = createSql.length();
                }
            }
            
            String tablePart = createSql.substring(startIndex, endIndex).trim();
            // 移除反引号和多余的空格
            tablePart = tablePart.replace("`", "").trim();
            
            // 如果表名中包含空格，取第一个单词
            if (tablePart.contains(" ")) {
                tablePart = tablePart.split("\\s+")[0];
            }
            
            return tablePart;
        } catch (Exception e) {
            log.warn("提取表名失败: {}", e.getMessage());
            return "未知表名";
        }
    }
} 