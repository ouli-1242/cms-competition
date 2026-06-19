# 前端开发规范

本规范基于根目录 `AGENTS.md`，所有接口契约、响应格式、分页约定遵循总体规范。  
适用于 `cms-frontend/` 目录下的 Vue 3 前端工程。

## 1. 技术栈与版本

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue | 3.5.x |
| 构建 | Vite | 5.4.x |
| 语言 | TypeScript | 5.6.x |
| UI 组件库 | Element Plus | 2.8.x |
| 图标 | @element-plus/icons-vue | 2.3.x |
| 状态管理 | Pinia | 2.2.x |
| 路由 | Vue Router | 4.4.x |
| HTTP 客户端 | Axios | 1.7.x |
| 图表 | ECharts | 5.5.x |
| 样式 | SCSS | sass 1.79.x |
| 类型检查 | vue-tsc | 2.1.x |
| 运行环境 | Node.js | ≥ 18.0.0 |

## 2. 项目结构

采用 **按角色/功能组织** 的扁平结构，与后端角色权限对齐。

```
cms-frontend/
├── package.json
├── vite.config.ts                  # Vite 配置（代理、别名）
├── tsconfig.json                   # TypeScript 配置（项目引用模式）
├── tsconfig.app.json               # 应用代码 TS 配置
├── tsconfig.node.json              # Node 侧 TS 配置
│
└── src/
    ├── main.ts                     # 入口（注册 Pinia、Router、Element Plus）
    ├── App.vue                     # 根组件（<router-view />）
    ├── env.d.ts                    # Vite 环境类型声明
    │
    ├── api/                        # API 层（按功能域拆分）
    │   ├── request.ts              # Axios 实例与拦截器
    │   ├── auth.ts                 # 认证 + 学生个人中心
    │   ├── public.ts               # 公开接口（轮播、热门、竞赛列表）
    │   ├── admin.ts                # 管理员接口
    │   ├── registration.ts         # 报名（学生 + 教师）
    │   ├── team.ts                 # 团队操作
    │   ├── notification.ts         # 站内通知
    │   └── upload.ts               # 文件上传
    │
    ├── router/
    │   └── index.ts                # 路由表 + 导航守卫
    │
    ├── stores/
    │   └── user.ts                 # Pinia 用户状态（token、user、role）
    │
    ├── layouts/                    # 布局壳（按角色拆分）
    │   ├── PublicLayout.vue        # 公共布局（顶部导航 + 可折叠侧栏）
    │   ├── StudentLayout.vue       # 学生布局（左侧菜单）
    │   ├── TeacherLayout.vue       # 教师布局（左侧菜单，蓝色主题）
    │   └── AdminLayout.vue         # 管理员布局（左侧菜单，蓝色主题）
    │
    ├── views/                      # 页面（按角色分组）
    │   ├── auth/                   # 登录、注册
    │   ├── public/                 # 首页、竞赛列表、竞赛详情
    │   ├── student/                # 学生功能页（12 个）
    │   ├── teacher/                # 教师功能页（竞赛、审核、指导邀请、统计）
    │   ├── admin/                  # 管理员功能页（竞赛、报名、轮播、统计、用户、资料变更）
    │   ├── shared/                 # 多角色共用页（Dashboard、GuestPermission）
    │   └── NotFound.vue            # 404
    │
    ├── components/                 # 公共组件
    │   ├── PagePlaceholder.vue     # 占位组件
    │   └── NotificationBell.vue    # 通知铃铛（未读计数 + 下拉列表）
    │
    └── styles/
        ├── variables.scss          # 设计系统变量（颜色、间距、阴影、字号等）
        └── global.scss             # 全局样式、工具类、动画
```

## 3. 分层职责

```
views (页面：UI 组装、表单交互、调用 api)
  ↓
api (接口封装：调用 request，定义类型)
  ↓
request.ts (Axios 实例：拦截器、错误处理)
  ↓
后端
```

| 层级 | 职责 | 约束 |
|------|------|------|
| **views** | 页面 UI、表单交互、本地状态、调用 api | 不直接调用 `request`；页面状态用 `ref`/`reactive` |
| **api** | 单个功能域的 API 封装 + TS 类型定义 | 统一使用 `request` 实例；导出接口类型供 views 使用 |
| **stores** | 全局状态（用户信息、Token） | 通过 `useXxxStore()` 消费；页面级状态禁止上提 |
| **layouts** | 布局壳、导航菜单、角色区分 | 不包含业务逻辑 |
| **components** | 跨页面复用的 UI 组件 | 不依赖具体业务接口 |
| **styles** | 全局样式、设计变量 | 页面局部样式用 `<style scoped>` |

## 4. 统一响应与分页（对接后端契约）

### 4.1 响应格式

后端返回 `Result<T>`：

```typescript
interface Result<T> {
  code: number;      // 200 成功
  message: string;
  data: T;
}
```

`request.ts` 已封装：
- `code === 200` 视为成功，解包返回 `data`
- 非 200 弹 `ElMessage.error(message)`
- HTTP 401 清除 Token 并跳转 `/login`
- HTTP 403 提示"无权限"

### 4.2 分页格式

请求参数：

```typescript
interface PageParams {
  pageNum: number;   // 默认 1
  pageSize: number;  // 默认 10
  // ...业务筛选字段
}
```

响应数据（MyBatis-Plus `Page<T>`）：

```typescript
interface PageResult<T> {
  records: T[];      // ⚠️ 注意是 records 不是 list
  total: number;
  size: number;
  current: number;
  pages: number;
}
```

**Element Plus 分页组件适配**：

```vue
<el-pagination
  v-model:current-page="pageNum"
  v-model:page-size="pageSize"
  :total="total"
  @current-change="fetchData"
  @size-change="fetchData"
/>
```

```typescript
const fetchData = async () => {
  const res = await getCompetitions({ pageNum, pageSize, ...filters });
  list.value = res.records;   // ← records，不是 list
  total.value = res.total;
};
```

> **注意**：部分后端接口分页参数名为 `size`（如 `getMyRegistrations`），需查看具体 API 定义。

## 5. 路由与权限

### 5.1 路由配置

路由表在 `src/router/index.ts` 中集中管理，按角色分组：

| 路径前缀 | 布局 | 角色要求 |
|----------|------|----------|
| `/login`、`/register` | 独立页面 | 无 |
| `/`、`/competitions` | PublicLayout | 无 |
| `/student/*`、`/student-center/*` | PublicLayout | STUDENT |
| `/teacher/*` | TeacherLayout | TEACHER |
| `/admin/*` | AdminLayout | ADMIN |

### 5.2 路由守卫

`beforeEach` 中执行三层校验：

1. 已登录用户访问 `/login`、`/register` → 重定向首页
2. 未登录用户访问需认证页面 → 重定向 `/login`
3. 角色不匹配（`meta.roles` 校验）→ 重定向首页

### 5.3 路由 meta 字段

```typescript
interface RouteMeta {
  title?: string;       // 页面标题
  requiresAuth?: boolean; // 是否需要登录
  roles?: string[];     // 允许的角色列表
}
```

## 6. 状态管理

### 6.1 Pinia Store

当前仅有一个 store：`stores/user.ts`

```typescript
interface UserInfo {
  id: number;
  username: string;
  role: 'STUDENT' | 'TEACHER' | 'ADMIN' | 'VISITOR';
  realName?: string;
  studentNo?: string;
  college?: string;
  phone?: string;
  email?: string;
  avatar?: string;
}
```

- Token 持久化：`localStorage['cms_token']`
- 用户信息持久化：`localStorage['cms_user']`
- 消费方式：`const userStore = useUserStore()`

### 6.2 状态分层

| 范围 | 管理方式 | 示例 |
|------|----------|------|
| 页面级 | `ref` / `reactive` | 表单数据、列表、分页、弹窗状态 |
| 全局级 | Pinia store | 用户信息、Token、角色 |
| 组件通信 | `props` + `emit` | 父子组件传值 |

> 避免：将仅单页面使用的状态放入 Pinia store。

## 7. 命名规范

### 7.1 标识符

| 标识符 | 规范 | 示例 |
|--------|------|------|
| 变量/函数 | camelCase | `pageNum`、`fetchData()`、`handleSubmit()` |
| 组件名 | PascalCase | `PagePlaceholder`、`AdminLayout` |
| 接口/类型 | PascalCase | `Competition`、`PageResult<T>`、`LoginParams` |
| 常量 | UPPER_SNAKE_CASE 或 camelCase | `MAX_FILE_SIZE`、`defaultPageSize` |
| CSS 类 | kebab-case | `.card-grid`、`.text-primary` |

### 7.2 文件命名

| 类型 | 命名 | 示例 |
|------|------|------|
| 页面组件 | PascalCase `.vue` | `Competitions.vue`、`TeamCreate.vue` |
| 布局 | `XxxLayout.vue` | `AdminLayout.vue` |
| API 文件 | 功能域小写 `.ts` | `admin.ts`、`registration.ts` |
| 类型文件 | 与 API 同文件或 `.d.ts` | `public.ts` 中导出 interface |
| 样式 | `.scss` | `variables.scss`、`global.scss` |

## 8. API 调用约定

### 8.1 request.ts

```typescript
import request from '@/api/request';

// GET 查询
export function getCompetitions(params: PageParams) {
  return request.get('/competition/public/page', { params });
}

// POST 创建
export function createCompetition(data: CompetitionForm) {
  return request.post('/admin/competition', data);
}

// PUT 更新
export function updateCompetition(id: number, data: CompetitionForm) {
  return request.put(`/admin/competition/${id}`, data);
}

// DELETE 删除
export function deleteCompetition(id: number) {
  return request.delete(`/admin/competition/${id}`);
}
```

### 8.2 请求配置

- 基础 URL：`/api`（Vite 代理到 `http://localhost:8080`）
- 超时：15000ms
- 请求拦截：自动附加 `Authorization: Bearer <token>`
- 响应拦截：解包 `Result<T>`，code 200 返回 data，其余弹错

### 8.3 类型定义

每个 API 文件导出所需的接口类型：

```typescript
// public.ts
export interface Competition {
  id: number;
  title: string;
  category: string;
  type: number;        // 1=个人, 2=团队
  status: number;      // 0=下架, 1=上架
  // ...
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}
```

## 9. 编码规范

### 9.1 组件编写

- **使用 `<script setup lang="ts">`**（Composition API）
- **单文件组件**：template + script + style 集中在 `.vue` 文件
- **响应式**：基本类型用 `ref`，对象用 `reactive`
- **生命周期**：`onMounted` 中发起初始数据请求
- **模板**：使用 `v-if`/`v-for`/`v-model`，避免复杂表达式（提取为 computed）

### 9.2 TypeScript

- 所有 API 返回值必须声明类型
- 组件 props 使用 `defineProps<T>()` 泛型声明
- 减少 `any` 使用，不确定类型用 `unknown` + 类型收窄
- `ref` 初始值为 null 时声明泛型：`ref<UserInfo | null>(null)`

### 9.3 错误处理

- 所有 API 调用考虑失败分支（loading 状态、空数据展示）
- 网络错误由 `request.ts` 拦截器统一处理
- 业务错误（如"已报名"、"队伍已满"）在页面中用 `ElMessage` 或 Dialog 提示

### 9.4 样式

- 全局样式在 `styles/global.scss` 和 `styles/variables.scss`
- 页面样式用 `<style scoped lang="scss">`
- 引用变量：`@use '@/styles/variables' as *;`
- 响应式断点：768px（移动端适配，布局组件已处理）

## 10. 开发流程

1. **确认接口**：对照后端 Controller 和 AGENTS.md 中的路径规范
2. **定义类型**：在 API 文件中声明请求/响应类型
3. **封装 API**：在对应 API 文件中添加函数
4. **编写页面**：在 `views/` 对应角色目录下创建 `.vue` 文件
5. **注册路由**：在 `router/index.ts` 中添加路由，设置 meta 权限
6. **自测验证**：覆盖正常/异常/空数据/移动端场景
7. **类型检查**：执行 `vue-tsc -b`，零错误再提交

## 11. 严格禁止

- **禁止**直接使用 `axios` 或 `fetch`，统一使用 `@/api/request` 实例
- **禁止**在组件中直接拼 URL 调用接口，必须通过 api 层封装
- **禁止**将 API 响应中的 `records` 误用为 `list`
- **禁止**在 `views/` 页面间直接 import 彼此的函数或状态
- **禁止** `localStorage` 操作散落在各组件中，统一由 `stores/user.ts` 管理 Token
- **禁止**在模板中使用复杂表达式（超过一个运算符），提取为 `computed`
- **禁止**硬编码角色判断（如 `role === 'admin'`），使用 store 的 `isAdmin`/`isTeacher`/`isStudent`
- **禁止**保留大量注释掉的死代码
- **禁止**页面级状态上提到 Pinia store

## 12. 验证与构建

```bash
npm run dev           # 开发环境（代理到 localhost:8080）
npm run build         # 生产构建（vue-tsc + vite build）
npm run preview       # 预览构建产物
```

类型检查命令：

```bash
npx vue-tsc -b        # 单独执行类型检查，零错误方可提交
```
