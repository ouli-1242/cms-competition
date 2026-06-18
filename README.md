# 高校学科竞赛报名管理系统（CMS-Competition）

面向高校的竞赛全生命周期管理平台，支持学生、教师、管理员三类角色，覆盖竞赛发布、个人/团队报名、审核、数据统计、首页运营等完整流程。

## 技术栈

| 端 | 技术 |
|---|---|
| 前端 | Vue 3.5 + TypeScript 5.6 + Vite 5 + Pinia 2 + Element Plus 2.8 + ECharts 5 |
| 后端 | Spring Boot 2.7.18 + MyBatis-Plus 3.5.5 + Spring Security + JWT (jjwt 0.11.5) |
| 数据库 | MySQL 8.0 |

## 一、环境要求

| 软件 | 版本 | 说明 |
|---|---|---|
| JDK | 21 | pom.xml 中 java.version=1.8（源码兼容），实际运行 JDK 21 |
| Maven | 3.6+ | IDEA 自带 Maven 也可 |
| Node.js | 18+ | 需自带 npm |
| MySQL | 8.0 | 端口 3306 |

推荐 IDE：后端 IntelliJ IDEA，前端 VSCode / WebStorm。

## 二、初始化数据库

### 1. 执行初始化脚本

脚本位于 [sql/init.sql](sql/init.sql)，会自动创建数据库 `cms_competition`、全部表结构及测试数据。

通过 Navicat 或其他 MySQL 客户端执行：

```sql
SOURCE /path/to/cms-competition/sql/init.sql;
```

### 2. 测试账号

所有测试账号密码均为 `password`：

| 角色 | 用户名 |
|---|---|
| 管理员 | `admin` |
| 教师 | `teacher1` / `teacher2` |
| 学生 | `stu1` / `stu2` / `stu3` |

## 三、启动后端

### 1. IDEA 打开项目

用 IDEA 打开 `cms-backend` 目录，自动识别 Maven 项目并下载依赖。

### 2. 修改数据库配置

编辑 [application-dev.yml](cms-backend/src/main/resources/application-dev.yml)，将 `password` 改为你的 MySQL root 密码：

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
├── cms-backend/                      # Spring Boot 后端
│   ├── src/main/java/com/cms/
│   │   ├── common/                   # 公共模块（异常处理、统一响应、JWT 工具）
│   │   ├── config/                   # 配置（Security、MyBatis-Plus、XSS 过滤）
│   │   ├── controller/               # 控制器（admin/ student/ teacher/ public_）
│   │   ├── dto/                      # 请求 DTO
│   │   ├── entity/                   # 数据库实体
│   │   ├── interceptor/              # JWT 认证过滤器
│   │   ├── mapper/                   # MyBatis-Plus Mapper
│   │   ├── service/                  # 业务服务接口与实现
│   │   └── vo/                       # 视图对象
│   └── src/main/resources/
│       ├── application.yml           # 基础配置
│       ├── application-dev.yml       # 开发环境
│       └── application-prod.yml      # 生产环境
├── cms-frontend/                     # Vue 3 前端
│   └── src/
│       ├── api/                      # Axios 接口封装（按功能域拆分）
│       ├── components/               # 公共组件
│       ├── layouts/                  # 布局（Public/Student/Teacher/Admin）
│       ├── router/                   # 路由 + 权限守卫
│       ├── stores/                   # Pinia 状态管理
│       └── views/                    # 页面（按角色分目录）
├── sql/
│   └── init.sql                      # 数据库初始化脚本
├── AGENTS.md                         # 总体开发规范
├── AGENTS-backend.md                 # 后端开发规范
├── AGENTS-frontend.md                # 前端开发规范
└── README.md
```

## 六、功能概览

| 模块 | 功能 |
|------|------|
| 首页 | 轮播图、热门推荐、竞赛搜索与筛选 |
| 竞赛管理 | 发布/编辑/上下线/删除（管理员）、浏览/详情（公开） |
| 个人报名 | 学生报名个人赛、查看报名记录 |
| 团队管理 | 创建队伍、邀请码加入、队长转让/踢人、团队报名 |
| 审核管理 | 教师/管理员审核个人与团队报名 |
| 数据统计 | 报名趋势图、分类统计、审核状态分布 |
| 内容运营 | 轮播图管理、热门推荐（手动/自动） |

## 七、常见问题

**前端接口 404？** 确认后端已启动，Vite 代理配置为 `/api` → `http://localhost:8080`。

**登录后报错？** 浏览器 DevTools → Network 查看响应，`Result.message` 字段显示具体原因。

**MySQL 密码不匹配？** 修改 `application-dev.yml` 中的 `spring.datasource.password`。

## License

仅供学习交流使用。
