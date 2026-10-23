# AGENTS.md

Java 版本 AI 漫剧制作工作台：React/Vite 前端与 Spring Boot 后端严格分离。旧项目 `vedio2` 仅供参考，不得删除或修改。

## 工作规则

1. 涉及代码修改时，先展示完整方案并获得用户确认。
2. 确认后的方案先保存到 `docs/`，再开始修改代码。
3. 修改完成后必须执行编译和测试验证。
4. 编译通过后必须进行代码审核。
5. 所有对话、代码注释、文档和 Commit Message 使用简体中文。
6. 修改完成后必须等待用户确认代码，才能提交或推送。
7. 不得修改 `vedio2`；Java 版本的全部实现放在当前目录。

## 固定开发环境

- JDK：`D:\SORT\JDK\OPENJDK21`
- Maven：`D:\download\maven\apache-maven-3.9.4\bin\mvn.cmd`
- Maven 本地仓库：`D:\download\maven\apache-maven-3.9.4\self_repo`
- Java 编译目标：Java 21。

执行 Maven 前设置 `JAVA_HOME` 为上述 JDK 路径，并使用上述 Maven 本地仓库。

## 架构约束

- 前端通过 HTTP REST API 调用后端，不得直接访问数据库、文件系统或 AI 密钥。
- 后端使用 Spring Boot、MySQL 和 controller/service/service.impl/mapper/entity 分层结构，SQL 放在 `mapper/*.xml`。
- 实体类统一放在 `entity` 包，必须添加中文 JavaDoc 和字段注释；使用 Lombok 生成访问器，禁止手写 getter/setter。Hutool 仅用于通用工具能力，不承担访问器生成。
- 请求 DTO 放在 `dto` 包，所有 REST 控制器放在 `controller` 包；资源使用复数名词路径及标准 HTTP 状态码。
- 当前基础建表脚本为 `src/main/resources/sql/schema.sql`，必须保持重复执行安全；后续结构变更采用版本化迁移。
- 新建或修改数据库表时，表和每个字段必须写中文数据库注释；已有表新增注释时要考虑存量数据与重复执行。
- AI 密钥只从环境变量读取，禁止写入源码、日志或提交记录。
- FFmpeg 由后端调用，长时间任务使用后台任务队列。

## 代码格式

- 不要把多个语句、方法或 XML 节点挤在同一行；每条语句和每个 XML 节点独立成行。
- 方法注解、方法签名和方法体分别换行；较长的参数列表、链式调用和 SQL 按逻辑分行并保持一致缩进。
- 方法请求入参不超过三个时，方法签名保持一行；超过三个入参时再按参数换行。
- 编写和审核代码时主动检查过长行，优先保证可读性。

## 验证和 Git

- 修改 Java 后端必须运行指定 Maven 的 `test`。
- 按功能批次提交，Commit Message 使用中文。
- `docs/` 和 `src/test/` 只保留在本地，不提交到 Git；每次暂存后检查 `git diff --cached --name-only`，发现这些路径必须取消暂存。
- 方案仍须先保存到本地 `docs/`，测试仍须编写并运行；忽略规则不代表跳过文档和验证。
- 未经用户确认，不得执行 `git commit` 或 `git push`。
