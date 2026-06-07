<script setup lang="ts">
/**
 * 游客权限提示页
 * - 拦截游客访问受限操作（报名、创建团队、后台等）
 * - 提供 "返回" 和 "去登录" 两个操作
 */
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

function goLogin() {
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}
</script>

<template>
  <div class="guest-page">
    <!-- 背景装饰 -->
    <div class="bg-deco">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <!-- 顶部返回栏 -->
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <span>←</span>
      </button>
      <h1 class="page-title">竞赛平台</h1>
      <div class="placeholder"></div>
    </div>

    <!-- 提示卡片 -->
    <div class="prompt-card">
      <!-- 锁图标 -->
      <div class="icon-wrap">
        <div class="icon-bg">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="#f6ad55">
            <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
          </svg>
        </div>
      </div>

      <!-- 文字 -->
      <h2 class="title">请登录后进行报名操作</h2>
      <p class="desc">您当前为游客身份，登录后即可报名参加竞赛，享受完整的竞赛服务体验。</p>

      <!-- 按钮 -->
      <div class="actions">
        <button class="btn-secondary" @click="goBack">返回</button>
        <button class="btn-primary" @click="goLogin">去登录</button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.guest-page {
  position: relative;
  min-height: calc(100vh - 60px);
  padding: $space-4 $space-6 $space-8;
}

// ===== 背景 =====
.bg-deco {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}
.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}
.blob-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(246, 173, 85, 0.3), transparent);
  top: -100px;
  right: -100px;
}
.blob-2 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(43, 108, 176, 0.25), transparent);
  bottom: -80px;
  left: -80px;
}

// ===== 顶部返回栏 =====
.page-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-2 0 $space-5;
  max-width: 1280px;
  margin: 0 auto;
}

.back-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: $bg-card;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  color: $text-regular;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-fast;

  &:hover {
    background: $primary-50;
    color: $primary;
  }
}

.page-title {
  margin: 0;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.placeholder {
  width: 36px;
  height: 36px;
}

// ===== 提示卡片 =====
.prompt-card {
  position: relative;
  z-index: 1;
  max-width: 480px;
  margin: 60px auto 0;
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  padding: 48px 40px 40px;
  text-align: center;
  animation: cardEnter 0.5s $transition-base both;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: $space-6;
}

.icon-bg {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: #fff5e6;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(246, 173, 85, 0.15);
}

.title {
  margin: 0 0 $space-3;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.desc {
  margin: 0 0 $space-8;
  font-size: $font-size-sm;
  line-height: 1.7;
  color: $text-secondary;
}

.actions {
  display: flex;
  gap: $space-3;
  justify-content: center;
}

.btn-secondary {
  flex: 1;
  height: 44px;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-base;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $bg-page;
    border-color: $primary;
    color: $primary;
  }
}

.btn-primary {
  flex: 1;
  height: 44px;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
  transition: all $transition-fast;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
  }
  &:active {
    transform: scale(0.98);
  }
}
</style>
