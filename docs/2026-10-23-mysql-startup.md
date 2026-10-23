# MySQL 项目接口启动说明

本机数据库 `vedio_java` 已创建。其他环境先执行：

```sql
CREATE DATABASE IF NOT EXISTS vedio_java CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

应用启动时会执行 `src/main/resources/schema.sql` 创建 `projects` 表。数据库连接默认地址为 `jdbc:mysql://localhost:3306/vedio_java`，默认用户为 `root`。密码不保存在仓库。

```powershell
$env:JAVA_HOME="D:\SORT\JDK\OPENJDK21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:DB_PASSWORD="<本机 MySQL 密码>"
& 'D:\download\maven\apache-maven-3.9.4\bin\mvn.cmd' `
  '-Dmaven.repo.local=D:\download\maven\apache-maven-3.9.4\self_repo' `
  spring-boot:run
```

接口：`GET /api/health`、`POST /api/projects`、`GET /api/projects`、`GET /api/projects/{id}`。创建项目返回 `201 Created` 与 `Location` 头。创建项目示例请求体：`{"name":"测试项目","description":"剧本"}`。
