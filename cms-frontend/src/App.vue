<script setup lang="ts">
// 根组件：使用 router-view 渲染路由
</script>

<template>
  <router-view v-slot="{ Component, route }">
    <transition name="page-fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<style lang="scss">
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei',
    'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: #f5f7fa;
  color: #1a202c;
}

// 页面过渡动画
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

// Element Plus 全局主题微调
:root {
  --el-color-primary: #2b6cb0;
  --el-color-primary-light-3: #5a93d0;
  --el-color-primary-light-5: #89b5e1;
  --el-color-primary-light-7: #b8d3ef;
  --el-color-primary-light-8: #d6e8ff;
  --el-color-primary-light-9: #ebf4fb;
  --el-border-radius-base: 6px;
  --el-border-radius-small: 4px;
  --el-font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
}

// Element Plus 按钮优化
.el-button {
  border-radius: 6px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.97);
  }
}

.el-button--primary {
  box-shadow: 0 2px 8px rgba(43, 108, 176, 0.25);

  &:hover {
    box-shadow: 0 4px 16px rgba(43, 108, 176, 0.35);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0) scale(0.97);
  }
}

// Element Plus 卡片优化
.el-card {
  border-radius: 8px;
  border: 1px solid #edf2f7;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

// Element Plus 表格微调
.el-table {
  border-radius: 8px;
  overflow: hidden;

  th.el-table__cell {
    background: #f8fafc;
    color: #2d3748;
    font-weight: 600;
    font-size: 13px;
    border-bottom: 1px solid #e2e8f0;
  }
}

.el-table__body tr {
  transition: background-color 0.15s ease;

  &:hover > td.el-table__cell {
    background-color: #f8fafc;
  }
}

// Element Plus 标签微调
.el-tag {
  border-radius: 4px;
  font-weight: 500;
}

// Element Plus 分页微调
.el-pagination {
  justify-content: center;
  margin-top: 16px;
}

// Element Plus 输入框微调
.el-input__wrapper {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: box-shadow 0.2s ease, transform 0.15s ease;

  &:hover {
    box-shadow: 0 0 0 1px #a0aec0 inset;
  }
}

.el-input.is-focus .el-input__wrapper {
  box-shadow: 0 0 0 1px #2b6cb0 inset, 0 0 0 3px rgba(43, 108, 176, 0.12);
}

.el-textarea__inner {
  border-radius: 6px;
  transition: box-shadow 0.2s ease;

  &:focus {
    box-shadow: 0 0 0 1px #2b6cb0 inset, 0 0 0 3px rgba(43, 108, 176, 0.12);
  }
}

// Element Plus 选择器微调
.el-select .el-input__wrapper {
  border-radius: 6px;
}

// Element Plus Dialog 微调
.el-dialog {
  border-radius: 10px;
  overflow: hidden;

  .el-dialog__header {
    border-bottom: 1px solid #f0f2f5;
    padding: 18px 24px;
    margin-right: 0;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    border-top: 1px solid #f0f2f5;
    padding: 14px 24px;
  }
}

// Element Plus 消息提示圆角
.el-message {
  border-radius: 8px;
}

// 骨架屏优化
.el-skeleton {
  --el-skeleton-color: #f0f2f5;
  --el-skeleton-to-color: #e8ebf0;
}

// 加载遮罩优化
.el-loading-mask {
  border-radius: 8px;
}
</style>
