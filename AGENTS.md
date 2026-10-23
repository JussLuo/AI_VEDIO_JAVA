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
- 后端使用 Spring Boot、SQLite 和 controller/service/repository/model 分层结构。
- 数据库迁移放在 `src/main/resources/db/migration/`。
- AI 密钥只从环境变量读取，禁止写入源码、日志或提交记录。
- FFmpeg 由后端调用，长时间任务使用后台任务队列。

## 验证和 Git

- 修改 Java 后端必须运行指定 Maven 的 `test`。
- 按功能批次提交，Commit Message 使用中文。
- 未经用户确认，不得执行 `git commit` 或 `git push`。
