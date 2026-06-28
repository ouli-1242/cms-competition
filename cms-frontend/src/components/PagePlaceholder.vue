<script setup lang="ts">
defineProps<{ name?: string; loading?: boolean }>()
</script>

<template>
  <div class="placeholder">
    <div class="placeholder-icon">
      <svg viewBox="0 0 80 80" width="64" height="64" fill="none">
        <rect x="8" y="12" width="44" height="56" rx="8" stroke="#cbd5e0" stroke-width="3" fill="#f8fafc"/>
        <rect x="18" y="24" width="24" height="4" rx="2" fill="#e2e8f0">
          <animate attributeName="width" values="24;30;24" dur="2s" repeatCount="indefinite"/>
        </rect>
        <rect x="18" y="34" width="20" height="4" rx="2" fill="#e2e8f0"/>
        <rect x="18" y="44" width="14" height="4" rx="2" fill="#e2e8f0"/>
        <circle cx="66" cy="58" r="10" stroke="#cbd5e0" stroke-width="3" fill="#f8fafc"/>
        <path d="M63 63 l6-6" stroke="#cbd5e0" stroke-width="3" stroke-linecap="round"/>
        <text x="66" y="42" text-anchor="middle" font-size="14" fill="#a0aec0">⏳</text>
      </svg>
    </div>
    <h2 class="placeholder-title">{{ loading ? '加载中...' : (name || '页面') }}</h2>
    <p class="placeholder-desc">该模块内容正在建设中，敬请期待</p>
    <div class="progress-dots">
      <span class="dot" :style="{ animationDelay: '0s' }"></span>
      <span class="dot" :style="{ animationDelay: '0.2s' }"></span>
      <span class="dot" :style="{ animationDelay: '0.4s' }"></span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0); opacity: 0.3; }
  40% { transform: scale(1); opacity: 1; }
}

.placeholder {
  padding: 80px 40px;
  background: $bg-card;
  border-radius: $radius-md;
  text-align: center;
  box-shadow: $shadow-sm;
  animation: fadeIn 0.5s ease;
}

.placeholder-icon {
  margin-bottom: $space-4;
}

.placeholder-title {
  margin: 0 0 $space-2;
  font-size: $font-size-xl;
  color: $text-primary;
  font-weight: $font-weight-semibold;
}

.placeholder-desc {
  margin: 0 0 $space-5;
  color: $text-secondary;
  font-size: $font-size-sm;
}

.progress-dots {
  display: flex;
  justify-content: center;
  gap: $space-2;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $primary;
  animation: dotBounce 1.4s ease-in-out infinite both;
}
</style>
