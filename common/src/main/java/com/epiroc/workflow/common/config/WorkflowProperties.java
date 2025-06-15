package com.epiroc.workflow.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Workflow配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "workflow")
public class WorkflowProperties {

    /**
     * 数据库初始化配置
     */
    private Database database = new Database();

    @Data
    public static class Database {
        /**
         * 是否启用数据库表自动初始化
         * 默认为true
         */
        private boolean autoInit = true;

        /**
         * 是否在初始化失败时抛出异常
         * 默认为true
         */
        private boolean failOnError = true;
    }
} 