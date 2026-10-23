# 实体类规范调整方案

## 目标

统一实体类注释和访问器规范，避免手写 getter/setter。

## 调整内容

- `AGENTS.md` 增加实体类必须使用中文 JavaDoc、字段注释和 Lombok 访问器的约束。
- `Project` 改用 Lombok `@Data`，保留无参和全参构造能力，不手写 getter/setter。
- 引入 Hutool 作为通用工具包依赖；需要说明 Hutool 不生成 Bean 访问器，访问器由 Lombok 处理。
- 运行 Maven 测试与打包，用户确认后再提交。
