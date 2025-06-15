# Workflow数据库自动初始化功能 - 功能总结

## 概述

根据`workflow.sql`中的表结构，在common模块中新增了数据库自动初始化功能。该功能在项目启动时检查workflow相关的数据库表是否存在，如果不存在则自动创建。

## 新增文件列表

### 1. 核心功能类
- `DatabaseInitializer.java` - 数据库初始化器（ApplicationRunner实现）
- `DatabaseTableInitService.java` - 数据库表初始化服务
- `WorkflowProperties.java` - 配置属性类

### 2. 工具类
- `DatabaseInitUtil.java` - 数据库初始化工具类

### 3. 配置和文档
- `application-workflow.yml` - 示例配置文件
- `README-DATABASE-INIT.md` - 功能使用说明
- `sql/workflow.sql` - 数据库表结构SQL文件（从common/sql/复制）

## 功能特性

### 1. 自动检测和创建
- 项目启动时自动检查10张workflow表是否存在
- 只创建不存在的表，不会修改已存在的表
- 支持配置开关控制是否启用自动初始化

### 2. 支持的表
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

### 3. 配置选项
```yaml
workflow:
  database:
    auto-init: true      # 是否启用自动初始化，默认true
    fail-on-error: true  # 初始化失败是否抛异常，默认true
```

### 4. 日志记录
- 详细的初始化过程日志
- 表创建成功/失败的记录
- 缺失表的统计信息

## 使用方式

### 1. 默认使用（推荐）
无需任何配置，项目启动时自动检查和创建表。

### 2. 禁用自动初始化
```yaml
workflow:
  database:
    auto-init: false
```

### 3. 手动检查表状态
```java
@Autowired
private DatabaseInitUtil databaseInitUtil;

// 检查所有表状态
String status = databaseInitUtil.checkAllTables();
System.out.println(status);

// 获取缺失的表
List<String> missingTables = databaseInitUtil.getMissingTables();
```

## 技术实现

### 1. 架构设计
- `DatabaseInitializer` - 启动时执行的主入口
- `DatabaseTableInitService` - 核心业务逻辑
- `WorkflowProperties` - 配置管理
- `DatabaseInitUtil` - 工具方法

### 2. 依赖关系
- Spring Boot ApplicationRunner
- JdbcTemplate
- MySQL数据库
- Lombok

### 3. 错误处理
- 支持配置是否在初始化失败时抛出异常
- 详细的错误日志记录
- 优雅的错误处理机制

## 注意事项

1. **数据库权限**：确保数据库用户有CREATE TABLE权限
2. **生产环境**：建议在生产环境中设置`auto-init: false`，手动管理表结构
3. **SQL兼容性**：当前SQL文件针对MySQL编写
4. **表结构变更**：只创建表，不会修改已存在表的结构

## 扩展性

该功能设计具有良好的扩展性：
- 可以轻松添加新的表到初始化列表
- 支持不同数据库的SQL适配
- 可以扩展为支持表结构升级功能

## 测试建议

1. 在空数据库中测试自动创建功能
2. 在已有部分表的数据库中测试增量创建
3. 测试配置开关的有效性
4. 测试错误处理机制 