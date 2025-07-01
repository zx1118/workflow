# 构建包含注释的JAR包指南

## 📋 概述

本项目已配置Maven插件来支持生成包含源代码注释的JAR包。通过不同的构建方式，您可以获得：

- 📦 **基本JAR包**：只包含编译后的class文件
- 📝 **源码JAR包**：包含原始Java源代码和注释
- 📚 **Javadoc JAR包**：包含生成的API文档

## 🚀 构建方式

### 1. 默认构建（包含注释和源码）

```bash
# 使用默认profile，会生成源码和文档JAR
mvn clean install

# 或者明确指定dev-with-docs profile
mvn clean install -Pdev-with-docs
```

**生成的文件：**
```
target/
├── workflow-common-0.0.1-SNAPSHOT.jar         # 基本JAR包
├── workflow-common-0.0.1-SNAPSHOT-sources.jar # 源码JAR包（包含注释）
└── workflow-common-0.0.1-SNAPSHOT-javadoc.jar # Javadoc JAR包
```

### 2. 生产环境构建（精简版）

```bash
# 使用prod profile，只生成基本JAR，减小文件大小
mvn clean install -Pprod
```

**生成的文件：**
```
target/
└── workflow-common-0.0.1-SNAPSHOT.jar         # 基本JAR包（精简版）
```

### 3. 只生成源码JAR

```bash
# 单独生成源码JAR
mvn source:jar

# 或者在package阶段自动生成
mvn clean package
```

### 4. 只生成Javadoc JAR

```bash
# 单独生成Javadoc JAR
mvn javadoc:jar

# 生成Javadoc HTML文档
mvn javadoc:javadoc
```

## 📁 输出文件说明

| 文件类型 | 文件名后缀 | 内容说明 | 用途 |
|----------|------------|----------|------|
| 基本JAR | `.jar` | 编译后的class文件 | 运行时依赖 |
| 源码JAR | `-sources.jar` | 原始Java源代码和注释 | IDE调试、代码查看 |
| 文档JAR | `-javadoc.jar` | 生成的API文档 | 开发文档 |

## 🔧 配置说明

### Maven编译器配置
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>8</source>
        <target>8</target>
        <!-- 保留参数名 -->
        <parameters>true</parameters>
        <!-- 保留调试信息 -->
        <debug>true</debug>
        <!-- 保留源文件信息 -->
        <debuglevel>lines,vars,source</debuglevel>
    </configuration>
</plugin>
```

### 源码JAR配置
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-source-plugin</artifactId>
    <version>3.2.1</version>
    <executions>
        <execution>
            <id>attach-sources</id>
            <goals>
                <goal>jar-no-fork</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Javadoc配置
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.4.1</version>
    <configuration>
        <encoding>UTF-8</encoding>
        <charset>UTF-8</charset>
        <docencoding>UTF-8</docencoding>
        <failOnError>false</failOnError>
        <show>private</show>
        <author>true</author>
        <version>true</version>
    </configuration>
</plugin>
```

## 📊 Profile说明

### dev-with-docs Profile（默认）
- ✅ 生成源码JAR
- ✅ 生成Javadoc JAR
- ✅ 保留调试信息
- ✅ 保留参数名
- 🎯 **适用于**：开发环境、测试环境

### prod Profile
- ❌ 不生成源码JAR
- ❌ 不生成Javadoc JAR
- ❌ 不保留调试信息
- ✅ 减小JAR包大小
- 🎯 **适用于**：生产环境部署

## 🔍 验证JAR包内容

### 查看JAR包结构
```bash
# 查看基本JAR包内容
jar -tf target/workflow-common-0.0.1-SNAPSHOT.jar | head -20

# 查看源码JAR包内容
jar -tf target/workflow-common-0.0.1-SNAPSHOT-sources.jar | head -20

# 查看Javadoc JAR包内容
jar -tf target/workflow-common-0.0.1-SNAPSHOT-javadoc.jar | head -20
```

### 提取并查看源码
```bash
# 提取源码JAR到临时目录
mkdir temp-sources
cd temp-sources
jar -xf ../target/workflow-common-0.0.1-SNAPSHOT-sources.jar

# 查看Java源文件（包含注释）
cat com/epiroc/workflow/common/service/DatabaseTableInitService.java
```

## 🚀 IDE集成

### IntelliJ IDEA
1. 在项目依赖中，IDEA会自动识别`-sources.jar`
2. 可以直接查看源码和注释
3. 支持断点调试到源码级别

### Eclipse
1. 右键项目 → Properties → Java Build Path
2. 展开依赖的JAR → 选择Source attachment
3. 指向对应的`-sources.jar`文件

## 💡 最佳实践

### 开发阶段
```bash
# 开发时使用默认配置，便于调试
mvn clean install
```

### 测试阶段
```bash
# 测试时也使用完整配置，便于问题定位
mvn clean install -Pdev-with-docs
```

### 生产部署
```bash
# 生产环境使用精简配置，减小部署包大小
mvn clean install -Pprod
```

### CI/CD管道
```bash
# 在CI/CD中可以根据分支选择不同的profile
# 开发分支
mvn clean install -Pdev-with-docs

# 发布分支
mvn clean install -Pprod
```

## 🛠️ 故障排除

### 1. Javadoc生成失败
```bash
# 如果Javadoc生成失败，可以跳过
mvn clean install -Dmaven.javadoc.skip=true
```

### 2. 编码问题
确保在`pom.xml`中设置了正确的编码：
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
```

### 3. Java版本兼容性
如果使用Java 9+，需要添加额外的Javadoc配置：
```xml
<additionalJOptions>
    <additionalJOption>-Xdoclint:none</additionalJOption>
    <additionalJOption>--add-modules</additionalJOption>
    <additionalJOption>ALL-SYSTEM</additionalJOption>
</additionalJOptions>
```

## 📚 相关命令参考

```bash
# 清理构建
mvn clean

# 编译项目
mvn compile

# 运行测试
mvn test

# 打包项目
mvn package

# 安装到本地仓库
mvn install

# 部署到远程仓库
mvn deploy

# 查看有效POM
mvn help:effective-pom

# 查看依赖树
mvn dependency:tree

# 查看可用的profiles
mvn help:all-profiles
```

---

**注意**：源码JAR和Javadoc JAR主要用于开发和调试，在生产环境中可以选择不生成以减小部署包大小。 