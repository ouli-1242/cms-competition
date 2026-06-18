# cms-competition 查缺补漏实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 修复审查报告中 P0-P2 级别的所有问题，让四个角色的体验达到可用状态

**Architecture:** 纯前端修复为主（后端接口已就绪），涉及 15+ 个 Vue 文件和 1 个新页面。每个 Task 独立可测试。

**Tech Stack:** Vue 3 + TypeScript + Element Plus + ECharts + Axios + Spring Boot 2.7

---

### Task 1: P0 — 修复全局静默错误吞噬

**Files:**
- Modify: `src/views/admin/Competitions.vue`
- Modify: `src/views/admin/Registrations.vue`
- Modify: `src/views/admin/Banners.vue`
- Modify: `src/views/student/Profile.vue`
- Modify: `src/views/student/MyRegistrations.vue`
- Modify: `src/views/student/TeamManage.vue`

**Pattern:** 所有 `try { await api() } catch (e) {}` + 后面的 `ElMessage.success()` 改为：成功才 success，失败不吞异常。

统一模式：
```ts
try {
  await someApi(...)
  ElMessage.success('操作成功')
  // 后续逻辑（关闭弹窗、刷新列表等）
} catch (e: any) {
  // axios 拦截器已处理错误提示，这里不做任何事
}
```

### Task 2: P0 — 修复文件上传流程

**Files:**
- Create: `src/api/upload.ts` — 封装 `/api/common/upload` 调用
- Modify: `src/views/admin/Competitions.vue` — 封面上传
- Modify: `src/views/admin/Banners.vue` — 轮播图上传
- Modify: `src/views/student/ProfileEdit.vue` — 头像上传
- Modify: `src/views/student/TeamSubmit.vue` — 附件上传

### Task 3: P0 — 构建教师报名审核页

**Files:**
- Rewrite: `src/views/teacher/Audit.vue` — 从占位符替换为完整功能页
- Modify: `src/layouts/TeacherLayout.vue` — 添加"我的竞赛"菜单项

### Task 4: P1 — 对接 Dashboard 和 Stats 真实数据

**Files:**
- Rewrite: `src/views/shared/Dashboard.vue` — 调 API 替换硬编码数据
- Rewrite: `src/views/admin/Stats.vue` — 调 API 替换硬编码数据
- Modify: `src/router/index.ts` — 添加 admin dashboard 路由

### Task 5: P1 — 修复 ProfileEdit

**Files:**
- Modify: `src/views/student/ProfileEdit.vue` — college 字段、store 刷新、头像上传

### Task 6: P1 — MyRegistrations 增加团队报名

**Files:**
- Modify: `src/views/student/MyRegistrations.vue` — 添加团队报名 Tab

### Task 7: P1 — 管理员团队报名管理

**Files:**
- Modify: `src/views/admin/Registrations.vue` — 添加团队报名 Tab + 审核

### Task 8: P1 — 修复竞赛详情页字段对齐

**Files:**
- Modify: `src/views/public/CompetitionDetail.vue` — 对齐后端 Competition 实体字段

### Task 9: P2 — 修复首页和竞赛列表

**Files:**
- Modify: `src/views/public/Home.vue` — 调用 getActiveBanners/getActiveHot
- Modify: `src/views/public/CompetitionList.vue` — 筛选改服务端分页

### Task 10: P2 — 修复侧边栏高亮 + 教师统计页

**Files:**
- Modify: `src/layouts/StudentLayout.vue` — 前缀匹配高亮
- Modify: `src/views/teacher/Stats.vue` — 对接真实 API

### Task 11: P2 — 热门推荐保存 + 占位文字清理

**Files:**
- Modify: `src/views/admin/Banners.vue` — 热门推荐调用真实 API
- Modify: 所有 admin 视图 — 替换"此页面显示"占位标题
