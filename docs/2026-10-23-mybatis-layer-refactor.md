# MyBatis 分层调整方案

## 目标

将项目接口从 `repository` 分层调整为 MyBatis 风格的 `controller`、`service`、`service.impl`、`mapper` 和 `mapper.xml`。

## 调整内容

- `ProjectController` 保留在 `controller`。
- `ProjectService` 改为接口，放在 `service`。
- 新增 `ProjectServiceImpl`，放在 `service.impl`。
- 删除 `ProjectRepository`，新增 `ProjectMapper`，放在 `mapper`。
- SQL 统一放在 `src/main/resources/mapper/ProjectMapper.xml`。
- 使用 MyBatis Spring Boot Starter，保留 MySQL 数据源和现有 RESTful 路由。
- 运行完整测试和打包验证，用户确认后再提交推送。
