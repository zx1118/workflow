# Workflow数据库自动初始化功能说明

## 功能概述

本功能提供workflow模块的数据库表自动初始化能力，包括：
- 检查workflow相关表是否存在
- 自动创建缺失的表结构
- 插入必要的默认字典数据
- 支持多种数据库类型（MySQL、PostgreSQL等）

## 核心特性

- ✅ **自动创建表**：检查并创建缺失的数据库表
- ✅ **插入默认数据**：自动插入字典表的必要数据
- ✅ **智能检查**：即使表存在，也会检查默认数据是否完整
- ✅ **重复执行安全**：使用`INSERT IGNORE`避免重复插入错误
- ✅ **执行顺序控制**：确保数据库初始化在字典缓存加载之前完成

## 支持的表

- wf_dict - 字典表
- wf_dict_item - 字典项表
- wf_file - 文件表
- wf_flow - 流程表
- wf_key_user - 关键用户表
- wf_order - 工作流实例表
- wf_process - 流程定义表
- wf_rule - 流程规则表
- wf_task - 任务表
- wf_task_participant - 任务参与者表

## 默认数据说明

### 字典表(wf_dict)默认数据
| ID | 字典名称 | 字典编码 | 说明 |
|----|----------|----------|------|
| 1  | 订单状态 | order_status | 工作流订单状态字典 |
| 2  | 任务状态 | task_status | 工作流任务状态字典 |
| 3  | 审批类型 | approve_type | 审批类型字典 |

### 字典项表(wf_dict_item)默认数据
| 字典编码 | 项值 | 项文本 | 说明 |
|----------|------|--------|------|
| order_status | TO_BE_SUBMIT | 待提交 | 订单待提交状态 |
| task_status | NOT_APPROVED | 未审批 | 任务未审批状态 |
| task_status | WAITING | 等待操作 | 任务等待操作状态 |
| task_status | APPROVED | 已审批 | 任务已审批状态 |
| task_status | CANCELLED | 取消 | 任务取消状态 |
| task_status | CLOSED | 关单 | 任务关单状态 |

## 配置说明

### 基本配置

在`application.yml`中添加以下配置：

```yaml
workflow:
  database:
    auto-init: true          # 是否启用自动初始化，默认true
    fail-on-error: false     # 初始化失败时是否终止应用启动，默认false
```

### Druid连接池配置（重要）

如果您的项目使用了Druid连接池，需要特别注意SQL防注入检查可能会阻止DDL语句的执行。

#### 问题现象
启动时可能出现类似错误：
```
sql injection violation, dbType mysql, druid-version 1.2.22, syntax error: unclosed str
```

#### 解决方案

**方案1：调整Druid的wall filter配置（推荐）**

在`application.yml`中添加：

```yaml
spring:
  datasource:
    druid:
      # 基本配置
      url: jdbc:mysql://localhost:3306/your_database
      username: your_username
      password: your_password
      driver-class-name: com.mysql.cj.jdbc.Driver
      
      # Wall Filter配置 - 允许DDL语句
      wall:
        enabled: true
        config:
          # 允许执行多语句
          multi-statement-allow: true
          # 允许执行DDL语句
          create-table-allow: true
          drop-table-allow: true
          alter-table-allow: true
          # 允许注释
          comment-allow: true
          # 允许复杂的注释内容
          strict-syntax-check: false
          
      # 过滤器配置
      filters: stat,wall,log4j2
      
      # Web监控配置（可选）
      web-stat-filter:
        enabled: true
      stat-view-servlet:
        enabled: true
```

**方案2：在初始化时临时禁用wall filter**

如果方案1不适用，可以在数据源配置中添加：

```yaml
spring:
  datasource:
    druid:
      # 其他配置...
      wall:
        enabled: false  # 临时禁用，初始化完成后可以重新启用
```

**方案3：使用自定义数据源配置**

```java
@Configuration
public class DruidConfig {
    
    @Bean
    @Primary
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        // 基本配置
        dataSource.setUrl("jdbc:mysql://localhost:3306/your_database");
        dataSource.setUsername("your_username");
        dataSource.setPassword("your_password");
        
        // Wall Filter配置
        WallConfig wallConfig = new WallConfig();
        wallConfig.setMultiStatementAllow(true);
        wallConfig.setCreateTableAllow(true);
        wallConfig.setDropTableAllow(true);
        wallConfig.setAlterTableAllow(true);
        wallConfig.setCommentAllow(true);
        wallConfig.setStrictSyntaxCheck(false);
        
        WallFilter wallFilter = new WallFilter();
        wallFilter.setConfig(wallConfig);
        
        List<Filter> filters = new ArrayList<>();
        filters.add(new StatFilter());
        filters.add(wallFilter);
        dataSource.setProxyFilters(filters);
        
        return dataSource;
    }
}
```

## 执行流程

### 1. 启动检查
- 应用启动时自动执行（@Order(1)优先级）
- 检查配置是否启用自动初始化
- 检查数据库连接是否正常

### 2. 表结构检查
检查以下表是否存在：
- `wf_dict` - 字典定义表
- `wf_dict_item` - 字典项表  
- `wf_file` - 文件表
- `wf_flow` - 流程步骤表
- `wf_key_user` - 关键用户表
- `wf_order` - 工作流实例表
- `wf_process` - 流程定义表
- `wf_rule` - 流程规则表
- `wf_task` - 任务表
- `wf_task_participant` - 任务参与者表

### 3. 创建表结构
- 如果发现缺失的表，读取`sql/workflow.sql`文件
- 逐个执行CREATE TABLE语句
- 使用原生JDBC连接绕过Druid的wall filter检查

### 4. 插入默认数据
自动插入以下默认字典数据：

#### wf_dict表数据
| id | dict_name | dict_code | 说明 |
|----|-----------|-----------|------|
| 1 | 订单状态 | order_status | 工作流实例状态 |
| 2 | 任务状态 | task_status | 任务状态 |
| 3 | 审批类型 | approve_type | 审批操作类型 |

#### wf_dict_item表数据
| dict_id | item_text | item_value | 说明 |
|---------|-----------|------------|------|
| 1 | 待提交 | -1 | 草稿状态 |
| 2 | 未审批 | 0 | 待审批 |
| 2 | 等待操作 | 1 | 审批中 |
| 2 | 已审批 | 2 | 已完成 |
| 2 | 取消 | 3 | 已取消 |
| 2 | 关单 | 4 | 已关闭 |

## 故障排除

### 常见问题

**1. Druid SQL注入检查失败**
```
错误信息：sql injection violation, syntax error: unclosed str
解决方案：按照上述Druid配置说明调整wall filter设置
```

**2. 表已存在错误**
```
错误信息：Table 'wf_dict' already exists
解决方案：这是正常现象，系统会自动跳过已存在的表
```

**3. 权限不足**
```
错误信息：Access denied for user
解决方案：确保数据库用户具有CREATE、INSERT权限
```

**4. 字符集问题**
```
错误信息：Incorrect string value
解决方案：确保数据库和表使用utf8mb4字符集
```

### 调试模式

启用详细日志以便排查问题：

```yaml
logging:
  level:
    com.epiroc.workflow.common.config.DatabaseInitializer: DEBUG
    com.epiroc.workflow.common.service.DatabaseTableInitService: DEBUG
```

### 手动初始化

如果自动初始化失败，可以手动执行SQL：

1. 找到`common/src/main/resources/sql/workflow.sql`文件
2. 在数据库管理工具中执行该文件
3. 设置`workflow.database.auto-init=false`禁用自动初始化

## 监控和维护

### 初始化状态监控

系统会在日志中记录初始化过程：

```
INFO  - 开始检查workflow数据库表...
INFO  - 发现缺失的表: [wf_dict, wf_order]
INFO  - 开始创建workflow表...
INFO  - 成功创建表: wf_dict
INFO  - 成功创建表: wf_order
INFO  - workflow表创建完成
INFO  - 开始插入默认数据...
INFO  - 默认数据插入完成
INFO  - workflow数据库表初始化完成
```

### 性能影响

- 首次启动：需要执行DDL和INSERT语句，耗时约1-3秒
- 后续启动：只需检查表存在性，耗时约100-300毫秒
- 对应用启动性能影响极小

### 数据安全

- 使用`INSERT IGNORE`避免重复插入
- 不会删除或修改现有数据
- 只创建不存在的表结构
- 支持增量更新

## 最佳实践

1. **生产环境建议**：
   - 设置`fail-on-error: true`确保数据库问题能及时发现
   - 预先手动初始化数据库，设置`auto-init: false`

2. **开发环境建议**：
   - 保持`auto-init: true`便于快速搭建环境
   - 设置`fail-on-error: false`避免开发中断

3. **测试环境建议**：
   - 启用自动初始化，确保测试环境一致性
   - 定期清理测试数据，验证初始化功能

4. **Druid配置建议**：
   - 生产环境启用wall filter但配置允许DDL
   - 开发环境可以临时禁用wall filter
   - 定期更新Druid版本以获得更好的兼容性

## 使用方式

### 1. **自动启用（默认）**
项目启动时会自动：
- 检查并创建缺失的表
- 插入必要的默认数据
- 加载字典缓存

### 2. **禁用自动初始化**
```yaml
workflow:
  database:
    auto-init: false
```

### 3. **忽略初始化错误**
```yaml
workflow:
  database:
    fail-on-error: false
```
设置为false时，即使初始化失败也不会阻止应用启动

## 日志输出示例

```
2025-01-XX XX:XX:XX.XXX  INFO --- DatabaseInitializer : 开始检查workflow数据库表...
2025-01-XX XX:XX:XX.XXX  INFO --- DatabaseInitializer : 发现缺失的表: [wf_dict, wf_dict_item]
2025-01-XX XX:XX:XX.XXX  INFO --- DatabaseInitializer : 开始创建workflow表...
2025-01-XX XX:XX:XX.XXX  INFO --- DatabaseInitializer : 成功创建表: wf_dict
2025-01-XX XX:XX:XX.XXX  INFO --- DatabaseInitializer : 成功创建表: wf_dict_item
2025-01-XX XX:XX:XX.XXX  INFO --- DatabaseInitializer : workflow表创建完成
2025-01-XX XX:XX:XX.XXX  INFO --- DatabaseInitializer : 开始插入默认数据...
2025-01-XX XX:XX:XX.XXX  INFO --- DatabaseInitializer : 默认数据插入完成
2025-01-XX XX:XX:XX.XXX  INFO --- DatabaseInitializer : workflow数据库表初始化完成
2025-01-XX XX:XX:XX.XXX  INFO --- WfDictLoadService   : 开始加载字典缓存...
2025-01-XX XX:XX:XX.XXX  INFO --- WfDictLoadService   : 加载order_status字典缓存成功，共1条
2025-01-XX XX:XX:XX.XXX  INFO --- WfDictLoadService   : 字典缓存加载完成
```

## 依赖要求

- Spring Boot 2.7+
- JdbcTemplate
- MySQL 8.0+（推荐）
- 数据库连接池（如Druid）

## 相关类说明

- `DatabaseInitializer`: 数据库初始化器，负责创建表和插入默认数据
- `DatabaseTableInitService`: 数据库表检查服务
- `WfDictLoadService`: 字典数据加载服务，负责将字典数据加载到内存缓存
- `WorkflowProperties`: 配置属性类 