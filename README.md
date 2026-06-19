# 高校学科竞赛报名管理系统（CMS-Competition）

面向高校的竞赛全生命周期管理平台，支持学生、教师、管理员三类角色，覆盖竞赛发布、个人/团队报名、审核、数据统计、首页运营等完整流程。

## 技术栈

| 端 | 技术 |
|---|---|
| 前端 | Vue 3.5 + TypeScript 5.6 + Vite 5 + Pinia 2 + Element Plus 2.8 + ECharts 5 + SCSS |
| 后端 | Spring Boot 2.7.18 + MyBatis-Plus 3.5.5 + Spring Security + JWT (jjwt 0.11.5) |
| 数据库 | MySQL 8.0 |

## 一、环境要求

| 软件 | 版本 | 说明 |
|---|---|---|
| JDK | 8+ | pom.xml 中 `java.version=1.8`，兼容 JDK 8~21 |
| Maven | 3.6+ | IDEA 自带 Maven 也可 |
| Node.js | 18+ | 需自带 npm |
| MySQL | 8.0 | 端口 3306 |

推荐 IDE：后端 IntelliJ IDEA，前端 VSCode / WebStorm。

## 二、初始化数据库

### 1. 执行初始化脚本

脚本位于 `sql/` 目录，核心脚本为 `init.sql`，会自动创建数据库 `cms_competition`、全部表结构及测试数据。

通过 Navicat 或其他 MySQL 客户端执行：

```sql
SOURCE /path/to/cms-competition/sql/init.sql;
```

如需补充数据，可按需执行其他脚本：

| 脚本 | 作用 |
|------|------|
| `init.sql` | 主库建表 + 基础测试数据 |
| `add_competitions.sql` | 追加 6 个竞赛记录 |
| `add_team_advisor.sql` | 指导老师表（已含在 init.sql 中） |
| `alter_user_add_dissolve_cooldown.sql` | 团队解散恢复字段 |
| `create_profile_change_request.sql` | 资料修改审批表 |
| `update_images.sql` | 更新种子数据图片路径 |

### 2. 测试账号

所有测试账号密码均为 `123456`：

| 角色 | 用户名 |
|---|---|
| 管理员 | `admin` |
| 教师 | `teacher1` / `teacher2` |
| 学生 | `stu1` / `stu2` / `stu3` |

## 三、启动后端

### 1. IDEA 打开项目

用 IDEA 打开 `cms-backend` 目录，自动识别 Maven 项目并下载依赖。

### 2. 修改数据库配置

编辑 `cms-backend/src/main/resources/application-dev.yml`，将 `password` 改为你的 MySQL root 密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cms_competition?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 你的MySQL密码
```

### 3. 启动应用

在 `CmsApplication.java` 上右键 → Run，控制台出现 `Started CmsApplication` 即成功。

API 地址：`http://localhost:8080`

命令行启动（可选）：

```bash
cd cms-backend
mvn spring-boot:run
```

## 四、启动前端

```bash
cd cms-frontend
npm install
npm run dev
```

浏览器自动打开 `http://localhost:5173`，Vite 已配置 `/api` 代理到后端 `http://localhost:8080`。

生产构建：

```bash
npm run build      # 输出到 dist/
npm run preview    # 本地预览
```

## 五、项目结构

```
cms-competition/
├── cms-backend/                          # Spring Boot 后端
│   ├── src/main/java/com/cms/
│   │   ├── CmsApplication.java           # 启动入口
│   │   ├── common/
│   │   │   ├── constant/RoleConstant     # 角色常量
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException     # 业务异常
│   │   │   │   └── GlobalExceptionHandler# 全局异常处理
│   │   │   ├── result/Result             # 统一响应格式
│   │   │   └── util/
│   │   │       ├── JwtUtil               # JWT 工具
│   │   │       └── SecurityUtil          # 安全上下文工具
│   │   ├── config/
│   │   │   ├── JacksonConfig             # 日期序列化配置
│   │   │   ├── MybatisPlusConfig         # 分页插件
│   │   │   ├── MyMetaObjectHandler       # 自动填充 createTime/updateTime
│   │   │   ├── SecurityConfig            # Spring Security 配置
│   │   │   ├── WebMvcConfig              # 静态资源映射
│   │   │   └── XssFilter                 # XSS 过滤
│   │   ├── controller/
│   │   │   ├── AuthController            # 登录/注册/用户信息
│   │   │   ├── CommonController          # 文件上传
│   │   │   ├── NotificationController    # 站内通知
│   │   │   ├── public_/PublicController  # 公开接口（无需登录）
│   │   │   ├── admin/                    # 管理员接口（7 个 Controller）
│   │   │   ├── student/                  # 学生接口（4 个 Controller）
│   │   │   └── teacher/                  # 教师接口（4 个 Controller）
│   │   ├── dto/                          # 请求 DTO（6 个）
│   │   ├── entity/                       # 数据库实体（11 个）
│   │   ├── interceptor/
│   │   │   └── JwtAuthenticationFilter   # JWT 认证过滤器
│   │   ├── mapper/                       # MyBatis-Plus Mapper（11 个）
│   │   ├── service/                      # 业务服务（11 个接口 + 实现）
│   │   └── vo/                           # 视图对象（4 个）
│   └── src/main/resources/
│       ├── application.yml               # 基础配置
│       ├── application-dev.yml           # 开发环境
│       └── application-prod.yml          # 生产环境
├── cms-frontend/                         # Vue 3 前端
│   └── src/
│       ├── api/                          # Axios 接口封装（8 个模块）
│       ├── components/                   # 公共组件（通知铃铛等）
│       ├── layouts/                      # 布局（Public/Admin/Teacher）
│       ├── router/                       # 路由 + 权限守卫
│       ├── stores/                       # Pinia 状态管理
│       ├── styles/                       # SCSS 变量 + 全局样式
│       └── views/                        # 页面（30 个）
│           ├── auth/                     # 登录、注册
│           ├── public/                   # 首页、竞赛列表、竞赛详情
│           ├── student/                  # 个人中心、报名、团队管理（12 页）
│           ├── teacher/                  # 竞赛管理、审核、统计（4 页）
│           └── admin/                    # 竞赛、报名、轮播图、统计、用户、资料变更（6 页）
├── sql/                                  # 数据库脚本
│   └── init.sql                          # 主初始化脚本
└── README.md
```

## 六、功能概览

### 公共模块

| 功能 | 说明 |
|------|------|
| 首页 | 轮播图展示、热门推荐竞赛、最近发布竞赛 |
| 竞赛浏览 | 关键词搜索、分类/类型/报名状态多维筛选 |
| 竞赛详情 | 时间线展示、报名状态判定（截止/未开始/报名中）、游客状态适配 |
| 登录注册 | JWT 认证、BCrypt 加密、登录限流（5 次错误锁定 15 分钟） |
| 站内通知 | 铃铛组件、未读计数、30 秒轮询、标记已读 |

### 学生端

| 功能 | 说明 |
|------|------|
| 个人资料 | 查看/编辑个人信息、修改密码 |
| 资料修改审批 | 用户名/邮箱修改需提交申请，管理员审批后生效 |
| 个人报名 | 报名个人赛、查看报名记录、查看详情/编辑附件 |
| 团队管理 | 创建队伍、邀请码加入、队长审核成员、踢人/转让队长 |
| 团队解散 | 软删除 + 12 小时恢复窗口 |
| 团队报名 | 组队报名团队赛、查看团队报名状态 |
| 指导老师邀请 | 队长搜索教师并发送邀请，教师接受/拒绝 |

### 教师端

| 功能 | 说明 |
|------|------|
| 竞赛管理 | 查看被分配的竞赛列表及详情 |
| 报名审核 | 审核个人报名和团队报名（通过/拒绝 + 意见） |
| 指导老师 | 管理收到的团队指导邀请、查看指导的团队 |
| 数据统计 | 竞赛报名数据可视化（ECharts 柱状图 + 饼图） |

### 管理员端

| 功能 | 说明 |
|------|------|
| 竞赛管理 | 发布/编辑/上下线/删除竞赛、设置报名和比赛时间 |
| 报名审核 | 审核所有个人和团队报名，详情含报名信息展示 |
| 轮播图管理 | 增删改查轮播图，关联竞赛链接 |
| 热门推荐 | 手动添加/移除 + 一键自动推荐 |
| 数据统计 | 全局概览（竞赛数、报名数、通过率）+ 单竞赛统计 |
| 用户管理 | 用户列表、关键词搜索、角色筛选、创建教师账号 |
| 资料变更审批 | 审核学生提交的资料修改申请 |

## 七、安全设计

| 措施 | 实现 |
|------|------|
| 密码加密 | BCrypt（不可逆） |
| 认证机制 | JWT Token + Spring Security，无状态会话 |
| SQL 注入防护 | MyBatis-Plus `#{}` 占位符 |
| XSS 防护 | XssFilter 对所有请求参数 HTML 转义 |
| CSRF | 前后端分离架构，使用 Token 验证替代 Session |
| 接口限流 | 登录接口内存限流（5 次失败锁定 15 分钟） |
| 权限控制 | `@PreAuthorize` 方法级权限校验 |
| 统一响应 | `Result<T>` 封装（code/message/data） |
| 统一异常 | `GlobalExceptionHandler` 全局拦截 |

## 八、数据库设计

共 11 张表（代码实际使用）：

| 表名 | 说明 |
|------|------|
| `user` | 用户表（管理员/教师/学生） |
| `competition` | 竞赛表（个人赛/团队赛） |
| `team` | 团队表（邀请码、软删除） |
| `team_member` | 团队成员表（角色、审核状态） |
| `registration` | 个人报名表 |
| `team_registration` | 团队报名表 |
| `banner` | 轮播图表 |
| `hot_recommend` | 热门推荐表 |
| `message` | 站内通知表 |
| `team_advisor` | 指导老师邀请表 |
| `profile_change_request` | 资料修改申请表 |

> **注**：`init.sql` 中还包含 `team_log` 表（团队操作日志），但代码未使用，属预留表。`profile_change_request` 需单独执行 `create_profile_change_request.sql` 创建。

## 九、常见问题

**前端接口 404？** 确认后端已启动，Vite 代理配置为 `/api` → `http://localhost:8080`。

**登录后报错？** 浏览器 DevTools → Network 查看响应，`Result.message` 字段显示具体原因。

**MySQL 密码不匹配？** 修改 `application-dev.yml` 中的 `spring.datasource.password`。

**时间格式带 T？** 已配置 `JacksonConfig`，`LocalDateTime` 序列化为 `yyyy-MM-dd HH:mm:ss`。

## License

仅供学习交流使用。
