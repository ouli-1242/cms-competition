# 后端开发规范

本规范基于根目录 `AGENTS.md`，所有接口契约、响应格式、分页约定遵循总体规范。  
适用于 `cms-backend/` 目录下的 Spring Boot 后端工程。

## 1. 技术栈与版本

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 2.7.18 |
| Java | OpenJDK | 21（source/target 兼容 8） |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0+ |
| 连接池 | HikariCP | 默认 |
| 认证 | JWT (jjwt) | 0.11.5 |
| 安全 | Spring Security | 默认（随 Spring Boot） |
| 工具库 | Hutool | 5.8.25 |
| 表格导出 | EasyExcel | 3.3.3 |
| 简化 | Lombok | 默认 |

## 2. 项目结构

采用 **按角色水平分层 + 公共模块** 结构。本项目为中小型系统，不做模块级垂直切片，统一在 `com.cms` 包下按职责分层。

```
cms-backend/
├── pom.xml
├── src/main/java/com/cms/
│   ├── CmsApplication.java              # 启动类（@EnableAsync, @EnableScheduling）
│   │
│   ├── common/                          # 跨层通用
│   │   ├── constant/                    # 常量（RoleConstant）
│   │   ├── exception/                   # 异常（BusinessException, GlobalExceptionHandler）
│   │   ├── result/                      # 统一响应（Result<T>）
│   │   └── util/                        # 工具（JwtUtil, SecurityUtil）
│   │
│   ├── config/                          # 全局配置
│   │   ├── MybatisPlusConfig.java       # 分页插件
│   │   ├── MyMetaObjectHandler.java     # 自动填充 createTime/updateTime
│   │   ├── SecurityConfig.java          # Spring Security 配置
│   │   ├── WebMvcConfig.java            # 静态资源映射（/file/**）
│   │   └── XssFilter.java              # XSS 过滤器
│   │
│   ├── interceptor/                     # 拦截器
│   │   └── JwtAuthenticationFilter.java # JWT 认证过滤器
│   │
│   ├── controller/                      # REST 接口层
│   │   ├── AuthController.java          # /api/auth/*
│   │   ├── CommonController.java        # /api/common/*
│   │   ├── admin/                       # 管理员接口（@PreAuthorize ADMIN）
│   │   ├── student/                     # 学生接口（@PreAuthorize STUDENT）
│   │   ├── teacher/                     # 教师接口（@PreAuthorize TEACHER）
│   │   └── public_/                     # 公开接口（无需登录）
│   │
│   ├── dto/                             # 数据传输对象（入参/出参）
│   │   ├── BannerDTO.java
│   │   ├── CompetitionDTO.java
│   │   ├── LoginDTO.java
│   │   ├── ProfileDTO.java
│   │   ├── RegisterDTO.java
│   │   └── TeamDTO.java
│   │
│   ├── entity/                          # 数据库实体
│   │   ├── User.java, Competition.java, Team.java
│   │   ├── TeamMember.java, Registration.java, TeamRegistration.java
│   │   ├── Banner.java, HotRecommend.java, Message.java, TeamLog.java
│   │
│   ├── mapper/                          # MyBatis-Plus Mapper
│   │   └── XxxMapper.java（共 10 个）
│   │
│   ├── service/                         # 业务接口
│   │   ├── XxxService.java（共 10 个接口）
│   │   └── impl/                        # 业务实现
│   │       └── XxxServiceImpl.java（共 10 个实现）
│   │
│   └── vo/                              # 视图对象（复杂查询返回）
│       └── StatisticsVO.java
│
└── src/main/resources/
    ├── application.yml                  # 基础配置
    ├── application-dev.yml              # 开发环境
    ├── application-prod.yml             # 生产环境
    └── mapper/                          # MyBatis XML（当前为空，全部使用注解/内置方法）
```

## 3. 分层职责

```
controller (接收请求、参数校验、调用 Service、返回 Result)
    ↓
service (业务逻辑、事务管理、数据组装)
    ↓
mapper (数据访问，继承 BaseMapper<T>)
```

| 层级 | 职责 | 约束 |
|------|------|------|
| **controller** | 接收请求、参数校验（`@Valid` + JSR-303）、调用 Service、返回 `Result` | 禁止业务逻辑，禁止直接操作 Mapper |
| **service** | 业务逻辑、事务管理、数据组装 | 必须通过 Mapper 访问数据库，返回 DTO/VO 而非 Entity（简单场景可返回 Entity） |
| **mapper** | 数据访问接口，继承 `BaseMapper<T>` | 复杂 SQL 用 `@Select` 注解或 XML |
| **entity** | 数据库表映射（`@TableName`），字段 camelCase | 使用 `@TableLogic` 标记软删除字段 |
| **dto** | 入参/出参数据 | DTO 与 Entity 分离，避免直接暴露数据库结构 |

## 4. 认证与安全

### 4.1 Spring Security 配置

- **CSRF**：禁用（前后端分离 + JWT）
- **Session**：STATELESS
- **CORS**：允许所有来源（开发阶段），生产环境须收紧
- **公开端点**：`/api/auth/login`、`/api/auth/register`、`/api/auth/logout`、`/api/common/**`、`/api/competition/public/**`、`/api/banner/public/**`、`/api/hot-recommend/public/**`、`/file/**`
- **其余端点**：需认证 + JWT Token

### 4.2 JWT

- 签名算法：HMAC-SHA256
- 有效期：86400 秒（24 小时）
- Claims：`userId`、`role`
- 请求头：`Authorization: Bearer <token>`
- `JwtAuthenticationFilter` 解析 Token 并设置 `SecurityContext`

### 4.3 安全措施

| 措施 | 实现 | 位置 |
|------|------|------|
| 密码加密 | BCryptPasswordEncoder | SecurityConfig |
| XSS 过滤 | XssFilter（HtmlUtils.htmlEscape） | config/XssFilter.java |
| SQL 注入防护 | MyBatis-Plus 参数化查询，禁止 `inSql()` 拼接 | 全局 |
| 权限控制 | `@PreAuthorize("hasRole('XXX')")` | Controller |
| 密码保护 | `@JsonIgnore` 防止序列化泄露 | User.java |

## 5. 数据库规范

- **表名/字段名**：小写 + 下划线（`user`、`team_member`、`create_time`）
- **主键**：`id BIGINT AUTO_INCREMENT`，Entity 用 `@TableId(type = IdType.AUTO)`
- **基础字段**：

```sql
id BIGINT AUTO_INCREMENT PRIMARY KEY,
create_time DATETIME DEFAULT NULL COMMENT '创建时间',
update_time DATETIME DEFAULT NULL COMMENT '更新时间',
is_deleted TINYINT DEFAULT 0 COMMENT '软删除标记'
```

- **自动填充**：`MyMetaObjectHandler` 在 insert 时填 `createTime`，insert/update 时填 `updateTime`
- **软删除**：`@TableLogic` 标记 `isDeleted` 字段，MyBatis-Plus 自动处理
- **禁止 `SELECT *`**：列表查询使用分页
- **禁止循环内查库**：改用批量查询或 `in` 条件
- **SQL 脚本**：存放于项目根目录 `sql/` 下

## 6. 接口开发约定

### 6.1 路径规范

| 操作 | 方法 | URL 示例 |
|------|------|----------|
| 分页查询 | GET | `/api/admin/competition/page?pageNum=1&pageSize=10` |
| 单条详情 | GET | `/api/competition/public/{id}` |
| 列表查询 | GET | `/api/banner/public/list` |
| 新增 | POST | `/api/admin/competition` |
| 更新 | PUT | `/api/admin/competition/{id}` |
| 删除 | DELETE | `/api/admin/competition/{id}` |
| 特殊操作 | PUT/POST | `/api/student/team/join`、`/api/admin/competition/{id}/status` |

### 6.2 参数校验

- 使用 `@Valid` + JSR-303 注解（`@NotNull`、`@NotBlank`、`@Size`、`@Min` 等）
- `@RequestBody` 参数必须加 `@Valid`
- 校验失败返回 code 400

### 6.3 日期格式

- 日期统一 `yyyy-MM-dd HH:mm:ss`
- Java 侧使用 `LocalDateTime`，Jackson 自动格式化
- application.yml 已配置 `spring.jackson.date-format` 和 `spring.jackson.time-zone`

## 7. 异常处理

`GlobalExceptionHandler`（`@RestControllerAdvice`）统一处理：

| 异常类型 | 返回 code | 说明 |
|----------|-----------|------|
| `BusinessException` | 自定义（默认 500） | 业务异常，直接返回 message |
| `MethodArgumentNotValidException` | 400 | JSR-303 校验失败，拼接字段错误信息 |
| `BindException` | 400 | 表单绑定失败 |
| `AccessDeniedException` | 403 | Spring Security 权限不足 |
| `Exception`（兜底） | 500 | 未知异常，返回"服务器内部错误"，日志记录详情 |

**约束**：
- 业务异常统一使用 `BusinessException`，禁止直接抛 `RuntimeException`
- 兜底 handler 不暴露堆栈信息给前端
- Service 层使用 `@Transactional(rollbackFor = Exception.class)` 管理事务

## 8. 文件上传

- 接口：`POST /api/common/upload`，参数 `file` + `biz`（业务类型）
- 存储路径：`D:/upload/cms/`（由 `cms.upload.path` 配置）
- 访问路径：`/file/**`（WebMvcConfig 映射）
- 限制：单文件 ≤ 50MB，`biz` 参数使用白名单校验

## 9. 命名规范

| 标识符 | 规范 | 示例 |
|--------|------|------|
| Controller | `XxxController` | `AdminCompetitionController` |
| Service 接口 | `XxxService` | `TeamService` |
| Service 实现 | `XxxServiceImpl` | `TeamServiceImpl` |
| Mapper | `XxxMapper` | `RegistrationMapper` |
| Entity | 与表名对应，PascalCase | `TeamMember`、`Registration` |
| DTO | `XxxDTO` | `CompetitionDTO`、`LoginDTO` |
| VO | `XxxVO` | `StatisticsVO` |
| 常量类 | `XxxConstant` | `RoleConstant` |
| 配置类 | `XxxConfig` | `SecurityConfig` |

## 10. 严格禁止

- `BeanUtil.copyProperties()` 盲用（须确认字段映射正确性）
- Controller 中写业务逻辑或 SQL
- 循环内查询数据库（改用批量查询或 `in` 条件）
- `inSql()` 字符串拼接（SQL 注入风险，改用两步参数化查询）
- 事务中 `try-catch` 吞掉异常（应抛出并由全局处理器回滚）
- Entity 的 password 字段未加 `@JsonIgnore`
- 生成未使用的空方法
- `@RequestBody` 参数缺少 `@Valid`

## 11. 验证与构建

提交前必须执行：

```bash
mvn clean compile       # 编译检查
mvn test                # 单元测试（如有）
mvn spring-boot:run     # 实际启动验证
```

> **Windows 注意**：含中文路径的项目须用 PowerShell 执行 Maven，cmd 可能编码失败。  
> IDEA 自带 Maven 路径：`"D:\Program Files\JetBrains\IDEA\plugins\maven\lib\maven3\bin\mvn.cmd"`
