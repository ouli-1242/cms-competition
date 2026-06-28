<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const countdown = ref(10)
let timer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer!)
      router.push('/')
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="notfound">
    <div class="notfound-card">
      <div class="error-code">404</div>
      <div class="error-illustration">
        <svg viewBox="0 0 200 120" width="200" height="120">
          <circle cx="100" cy="60" r="50" fill="#eaf2fb" stroke="#2b6cb0" stroke-width="2" stroke-dasharray="6 4"/>
          <text x="100" y="68" text-anchor="middle" font-size="36" font-weight="700" fill="#2b6cb0">?</text>
          <circle cx="80" cy="40" r="4" fill="#2b6cb0" opacity="0.3">
            <animate attributeName="cy" values="40;35;40" dur="2s" repeatCount="indefinite"/>
          </circle>
          <circle cx="120" cy="45" r="3" fill="#2b6cb0" opacity="0.3">
            <animate attributeName="cy" values="45;38;45" dur="2.5s" repeatCount="indefinite"/>
          </circle>
        </svg>
      </div>
      <p class="notfound-text">页面不存在或已被移除</p>
      <p class="notfound-countdown">{{ countdown }} 秒后自动返回首页</p>
      <div class="notfound-actions">
        <button class="btn btn-primary" @click="router.push('/')">
          <span class="btn-icon">&#8592;</span> 返回首页
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

@keyframes floatUp {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.notfound {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8fafc 0%, #eaf2fb 100%);
  padding: $space-6;
}

.notfound-card {
  text-align: center;
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $space-10 $space-12;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  animation: floatUp 3s ease-in-out infinite;
  max-width: 480px;
  width: 100%;
}

.error-code {
  font-size: 120px;
  font-weight: $font-weight-bold;
  background: linear-gradient(135deg, $primary, $primary-hover);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: $space-2;
}

.error-illustration {
  margin-bottom: $space-4;
}

.notfound-text {
  font-size: $font-size-lg;
  color: $text-primary;
  margin: 0 0 $space-1;
}

.notfound-countdown {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin: 0 0 $space-6;

  &::before {
    content: '⏱ ';
  }
}

.notfound-actions {
  display: flex;
  justify-content: center;
  gap: $space-3;
}

.btn {
  height: 42px;
  padding: 0 $space-8;
  border: none;
  border-radius: $radius-base;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: $space-2;
  transition: all $transition-base;
  position: relative;
  overflow: hidden;

  .btn-icon {
    font-size: 18px;
    transition: transform $transition-base;
  }

  &:hover .btn-icon {
    transform: translateX(-3px);
  }
}

.btn-primary {
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.3);

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.12) 50%, transparent 100%);
    transform: translateX(-100%);
    transition: transform 0.5s ease;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(43, 108, 176, 0.4);

    &::after {
      transform: translateX(100%);
    }
  }

  &:active {
    transform: translateY(0) scale(0.97);
  }
}
</style>
