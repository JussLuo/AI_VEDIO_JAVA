# RESTful 接口与包结构调整方案

## 目标

项目接口采用资源式 URI 和标准 HTTP 语义，Java 类按职责进入 `controller`、`dto`、`model`、`repository`、`service` 包。

## 调整

1. `ProjectController` 移入 `controller`，`CreateProjectRequest` 移入 `dto`，`Project` 移入 `model`，`ProjectRepository` 移入 `repository`，`ProjectService` 移入 `service`。
2. `POST /api/projects` 返回 201 和 `Location: /api/projects/{id}`；保留 `GET /api/projects`。
3. 增加 `GET /api/projects/{id}`，存在返回 200，不存在返回 404。
4. 更新接口测试和 `AGENTS.md` 包结构约定，运行 Maven 测试、打包和代码审核。
5. 用户确认代码后再提交和推送。
