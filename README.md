# Epiroc Workflow Module

Epiroc工作流模块，提供完整的工作流引擎功能。

## 功能特性

- 🔄 **工作流引擎**：支持复杂的业务流程定义和执行
- 📊 **流程管理**：流程定义、实例管理、任务分配
- 👥 **多角色支持**：支持多种审批角色和权限控制
- 🗃️ **数据库自动初始化**：首次启动自动创建表结构和默认数据
- 💾 **字典缓存**：高性能的字典数据缓存机制
- 📧 **邮件通知**：集成邮件发送功能
- 🔧 **灵活配置**：支持多种配置方式和自定义扩展

## 模块结构

```
workflow/
├── common/          # 核心功能模块
│   ├── entity/      # 实体类
│   ├── mapper/      # 数据访问层
│   ├── service/     # 业务逻辑层
│   ├── config/      # 配置类
│   └── system/      # 系统核心组件
├── server/          # 服务端应用
│   └── controller/  # REST API
└── client/          # 客户端模块
```

## 快速开始

### 1. 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/workflow?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 2. 启用自动初始化（默认已启用）

```yaml
workflow:
  database:
    auto-init: true      # 自动创建表和默认数据
    fail-on-error: true  # 初始化失败时抛出异常
```

### 3. 启动应用

```bash
# 使用Maven
mvn spring-boot:run

# 或直接运行
java -jar workflow-server.jar
```

首次启动时，系统会自动：
- 检查并创建workflow相关的数据库表
- 插入必要的字典数据（订单状态、任务状态、审批类型等）
- 加载字典缓存到内存

## 主要功能

### 工作流引擎
- 流程定义和管理
- 任务分配和执行
- 审批流程控制
- 流程实例跟踪

### 数据库自动初始化
- ✅ 自动创建10个核心表
- ✅ 插入必要的字典数据
- ✅ 智能检查，避免重复操作
- ✅ 支持配置开关

### 字典管理
- 订单状态字典
- 任务状态字典  
- 审批类型字典
- 内存缓存机制

## 配置说明

详细配置请参考：[数据库初始化文档](common/src/main/resources/README-DATABASE-INIT.md)

### 基本配置
```yaml
workflow:
  database:
    auto-init: true      # 是否自动初始化数据库
    fail-on-error: true  # 初始化失败是否抛出异常
```

### 数据源配置
```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/workflow
          username: root
          password: password
          driver-class-name: com.mysql.cj.jdbc.Driver
```

## 构建说明

### 默认构建（包含源码和注释）
```bash
mvn clean install
```

### 生产环境构建（精简版）
```bash
mvn clean install -Pprod
```

详细构建说明请参考：[构建包含注释的JAR包指南](BUILD-WITH-COMMENTS.md)

## API文档

启动应用后，可以通过以下端点访问：

- 健康检查：`GET /workflow/test/detail?orderId=1`
- 流程提交：`POST /workflow/test/submit`

## 开发指南

### 添加新的工作流

1. 在`wf_process`表中定义流程
2. 在`wf_flow`表中配置流程步骤
3. 实现相应的业务逻辑
4. 配置审批规则

### 扩展字典数据

1. 在`wf_dict`表中添加新的字典定义
2. 在`wf_dict_item`表中添加字典项
3. 更新`WfDictLoadService`加载新的字典缓存

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库连接配置
   - 确保数据库服务正在运行

2. **表创建失败**
   - 检查数据库用户权限
   - 确保有CREATE TABLE权限

3. **字典加载失败**
   - 检查字典表是否存在
   - 查看日志中的详细错误信息

### 日志配置

```yaml
logging:
  level:
    com.epiroc.workflow: DEBUG
    com.epiroc.workflow.common.config.DatabaseInitializer: INFO
    com.epiroc.workflow.common.service.WfDictLoadService: INFO
```

## 版本要求

- Java 8+
- Spring Boot 2.7+
- MySQL 8.0+
- Maven 3.6+

## 许可证

Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
