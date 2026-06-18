# CMS-Competition 项目开发规范

> 高校竞赛报名管理系统 — 总体规范  
> 本文件为项目最高优先级约定，后端与前端规范均基于此派生。

## 1. 项目概述

| 维度 | 说明 |
|------|------|
| 项目名称 | CMS-Competition（高校竞赛报名管理系统） |
| 架构模式 | 前后端分离 SPA + REST API |
| 后端 | Spring Boot 2.7.18 + MyBatis-Plus 3.5.5 + MySQL 8.0 |
| 前端 | Vue 3.5 + Vite 5 + TypeScript 5.6 + Element Plus 2.8 |
| 认证 | JWT（jjwt 0.11.5）+ Spring Security |
| 运行环境 | Java 21（源码兼容级别 8）、Node.js ≥ 18 |

## 2. 核心原则

- **契约先行**：前后端以本规范定义的接口路径、响应格式、分页约定为准，任何变更须同步更新。
- **最小复杂度**：不引入不必要的抽象层，不过度设计。
- **分层职责**：后端 controller → service → dal；前端 views → api → stores，禁止越层调用。
- **安全第一**：密码 BCrypt 加密、SQL 参数化查询、XSS 过滤、JWT 认证。

## 3. 统一响应格式

后端统一返回 `Result<T>`：

```java
public class Result<T> {
    private Integer code;     // 状态码
    private String message;   // 提示文案
    private T data;           // 返回数据
}
```

| code | 含义 |
|------|------|
| 200  | 成功 |
| 400  | 参数校验失败 |
| 401  | 未登录或 Token 失效 |
| 403  | 无权限 |
| 500  | 服务器内部错误 |

前端 `request.ts` 已封装：code 200 视为成功，其余弹 `ElMessage.error` 提示。

## 4. 统一分页格式

**请求参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 当前页码，默认 1 |
| pageSize | int | 每页条数，默认 10 |

**响应格式**：后端使用 MyBatis-Plus `Page<T>`，前端接收时注意字段为 `records`（非 `list`）：

```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

> **前端注意**：从 `data.records` 取列表，从 `data.total` 取总数。

## 5. 接口路径规范

- 基础路径：`/api/{模块}/{资源}`
- 按角色分组：

| 角色 | 路径前缀 | 示例 |
|------|----------|------|
| 公开 | `/api/banner/public/`, `/api/competition/public/`, `/api/hot-recommend/public/` | 无需登录 |
| 认证 | `/api/auth/` | 登录、注册、注销 |
| 学生 | `/api/student/` | 个人中心、报名、团队 |
| 教师 | `/api/teacher/` | 审核、统计 |
| 管理员 | `/api/admin/` | 全局管理 |
| 通用 | `/api/common/` | 文件上传 |

- HTTP 方法：主要使用 `GET`（查询）和 `POST`（创建）/ `PUT`（更新）/ `DELETE`（删除）
- 日期格式：`yyyy-MM-dd HH:mm:ss`
- 字段命名：Java 侧 camelCase，数据库 snake_case，前端 camelCase

## 6. 角色与权限

| 角色 | 值 | 后端守卫 | 前端路由 meta.roles |
|------|----|----------|---------------------|
| 管理员 | ADMIN | `@PreAuthorize("hasRole('ADMIN')")` | `['ADMIN']` |
| 教师 | TEACHER | `@PreAuthorize("hasRole('TEACHER')")` | `['TEACHER']` |
| 学生 | STUDENT | `@PreAuthorize("hasRole('STUDENT')")` | `['STUDENT']` |

- JWT Token 存于 `localStorage['cms_token']`，请求头 `Authorization: Bearer <token>`
- 前端路由守卫在 `router/index.ts` 的 `beforeEach` 中校验角色

## 7. 数据库规范

- 数据库：`cms_competition`，字符集 `utf8mb4_unicode_ci`
- 表名/字段名：小写 + 下划线（snake_case）
- 主键：`id BIGINT AUTO_INCREMENT`
- 软删除：`is_deleted TINYINT DEFAULT 0`（核心业务表使用，关联表可直接删除）
- 时间字段：`create_time`、`update_time`，由 MyBatis-Plus MetaObjectHandler 自动填充
- 禁止 `SELECT *`，更新/删除必须带 WHERE 条件
- SQL 脚本存放于 `sql/` 目录

## 8. 开发流程

1. **澄清需求**：明确功能、接口、字段、验收标准
2. **核对契约**：确认前后端路径、参数、响应格式一致
3. **设计方案**：数据库变更、Service 流程、DTO 定义
4. **编码实现**：后端先 → 前端对接
5. **自测验证**：覆盖正常/异常/边界场景
6. **代码质量**：后端 `mvn clean compile`，前端 `vue-tsc -b`，零错误再提交

## 9. 子规范索引

| 文件 | 适用 |
|------|------|
| `AGENTS-backend.md` | 后端 Spring Boot 工程（`cms-backend/`） |
| `AGENTS-frontend.md` | 前端 Vue 3 工程（`cms-frontend/`） |
