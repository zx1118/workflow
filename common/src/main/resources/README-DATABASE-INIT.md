# Workflow数据库自动初始化功能

## 功能说明

本功能在项目启动时自动检查workflow相关的数据库表是否存在，如果不存在则自动创建。

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

## 配置说明

在`application.yml`或`application.properties`中可以配置以下参数：

### YAML配置示例
```yaml
workflow:
  database:
    auto-init: true      # 是否启用数据库表自动初始化，默认为true
    fail-on-error: true  # 是否在初始化失败时抛出异常，默认为true
```

### Properties配置示例
```properties
# 是否启用数据库表自动初始化，默认为true
workflow.database.auto-init=true
# 是否在初始化失败时抛出异常，默认为true
workflow.database.fail-on-error=true
```

## 使用方式

1. **自动启用（默认）**：
   - 项目启动时会自动检查并创建缺失的表
   - 如果所有表都存在，则跳过初始化

2. **禁用自动初始化**：
   ```yaml
   workflow:
     database:
       auto-init: false
   ```

3. **忽略初始化错误**：
   ```yaml
   workflow:
     database:
       fail-on-error: false
   ```
   设置为false时，即使初始化失败也不会阻止应用启动

## 日志输出

- 启动时会输出检查和创建表的日志信息
- 成功创建表时会记录表名
- 初始化失败时会记录详细错误信息

## 注意事项

1. 确保数据库连接配置正确
2. 确保数据库用户有创建表的权限
3. SQL文件位于`classpath:sql/workflow.sql`
4. 只会创建不存在的表，不会修改已存在的表结构
5. 建议在生产环境中谨慎使用，可以设置`auto-init: false`手动管理表结构

## 依赖要求

- Spring Boot
- JdbcTemplate
- MySQL数据库（SQL文件针对MySQL编写） 