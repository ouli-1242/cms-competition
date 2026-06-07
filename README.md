# 高校学科竞赛报名管理系统（CMS-Competition）

一套面向高校的竞赛全生命周期管理平台，支持游客、学生、老师、管理员四类角色，提供竞赛发布、个人/团队报名、审核、数据统计、首页运营等完整能力。

## 技术栈

| 端 | 技术 |
|---|---|
| 前端 | Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus + Axios + ECharts |
| 后端 | Spring Boot 2.7.18 + MyBatis-Plus 3.5.5 + Spring Security + JWT + Redis |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 5+ |

---

## 一、环境要求

### 1. 必备软件

| 软件 | 版本 | 说明 |
|---|---|---|
| **JDK** | 1.8 及以上 | 建议 JDK 8 / 11 / 17 |
| **Maven** | 3.6+ | IDEA 自带 Maven Wrapper 也可 |
| **Node.js** | 16+ (推荐 18 或 20) | 需自带 npm |
| **MySQL** | 5.7 / 8.0 | 默认账号 `root` / `root`，端口 `3306` |
| **Redis** | 5.0+ | 默认无密码，端口 `6379` |

### 2. 推荐 IDE

- **后端**：IntelliJ IDEA（2020+）
- **前端**：VSCode / WebStorm

---

## 二、初始化数据库

### 1. 启动 MySQL 服务
确保 MySQL 已启动并可使用 `root / root` 登录。

### 2. 执行初始化脚本
脚本位于 [sql/init.sql](sql/init.sql)，会创建数据库 `cms_competition` 及所有表，并插入测试数据。

**方式一：命令行**
```bash
mysql -u root -proot < sql/init.sql
```

**方式二：MySQL 客户端**
```sql
SOURCE /path/to/cms-competition/sql/init.sql;
```

### 3. 默认测试账号（密码均为 `123456`）

| 角色 | 用户名 |
|---|---|
| 管理员 | `admin` |
| 教师 | `teacher1` / `teacher2` |
| 学生 | `stu1` / `stu2` / `stu3` |

---

## 三、启动 Redis

### Windows
下载 Redis Windows 版（推荐 [redis-windows/redis-windows](https://github.com/redis-windows/redis-windows/releases)），解压后执行：
```powershell
.\redis-server.exe redis.conf
```

### Linux / macOS
```bash
redis-server
```

验证：
```bash
redis-cli ping    # 返回 PONG 表示成功
```

---

## 四、启动后端

### 1. IDEA 打开项目
用 IDEA 打开 `cms-backend` 目录，IDEA 会自动识别为 Maven 项目并下载依赖。

### 2. 修改配置（如需要）
编辑 [src/main/resources/application-dev.yml](cms-backend/src/main/resources/application-dev.yml)：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cms_competition?...
    username: root
    password: root
  redis:
    host: localhost
    port: 6379
```

### 3. 启动应用
- 在 [src/main/java/com/cms/CmsApplication.java](cms-backend/src/main/java/com/cms/CmsApplication.java) 上右键 → Run
- 控制台出现 `========== CMS Backend Started ==========` 即启动成功
- API 地址：`http://localhost:8080`

### 4. 命令行启动（可选）
```bash
cd cms-backend
mvn spring-boot:run
```

---

## 五、启动前端

### 1. 安装依赖
```bash
cd cms-frontend
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```

启动成功后浏览器自动打开 `http://localhost:5173`。

### 3. 生产构建（可选）
```bash
npm run build      # 输出到 dist/
npm run preview    # 本地预览生产包
```

---

## 六、项目结构

```
cms-competition/
├── cms-backend/          # Spring Boot 后端
│   ├── src/main/java/com/cms/
│   │   ├── common/       # 公共模块（异常、返回结果、工具）
│   │   ├── config/       # 配置类（Security、Redis、MyBatis-Plus）
│   │   ├── controller/   # 控制器（按角色分目录：admin/student/teacher/public_）
│   │   ├── dto/          # 请求 DTO
│   │   ├── entity/       # 数据库实体
│   │   ├── interceptor/  # JWT 拦截器
│   │   ├── mapper/       # MyBatis-Plus Mapper
│   │   ├── service/      # 业务服务
│   │   └── vo/           # 视图对象
│   └── src/main/resources/application*.yml
├── cms-frontend/         # Vue 3 前端
│   └── src/
│       ├── api/          # Axios 接口封装
│       ├── components/   # 公共组件
│       ├── layouts/      # 布局（4 种角色）
│       ├── router/       # 路由 + 权限守卫
│       ├── stores/       # Pinia 状态
│       └── views/        # 页面（按角色分目录）
├── sql/
│   └── init.sql          # 数据库初始化脚本
└── README.md
```

---

## 七、常见问题

### Q1：后端启动报 `RedisTemplate` 找不到？
已通过 [RedisConfig.java](cms-backend/src/main/java/com/cms/config/RedisConfig.java) 显式声明 `RedisTemplate<String, Object>` 的 Bean。

### Q2：前端接口 404？
- 确认后端已启动（`http://localhost:8080`）
- Vite 已配置代理：`/api/*` → `http://localhost:8080`

### Q3：登录后报错 / 取不到数据？
- 浏览器 DevTools → Network 看响应，`Result.message` 字段会显示具体原因
- 后端日志在 IDEA 控制台，`@RestControllerAdvice` 统一处理异常

### Q4：MySQL 密码不是 root/root？
修改 [application-dev.yml](cms-backend/src/main/resources/application-dev.yml) 中的 `spring.datasource.password`。

---

## 八、技术亮点

- **统一返回格式**：`Result<T>` 包装 `{code, message, data}`，配合 `@RestControllerAdvice` 全局异常处理
- **JWT 鉴权**：`JwtAuthenticationFilter` + Spring Security，支持游客/学生/老师/管理员四级权限
- **Redis 缓存**：首页热门推荐、轮播图等热点数据缓存 5 分钟
- **MyBatis-Plus**：LambdaQueryWrapper + 逻辑删除 + 分页插件
- **团队管理**：邀请码加入、队长转让、踢人、提交报名一站式

---

## License

仅供学习交流使用。
