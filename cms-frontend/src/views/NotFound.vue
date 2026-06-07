<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="notfound">
    <h1>404</h1>
    <p>页面不存在</p>
    <button class="btn" @click="router.push('/')">返回首页</button>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;
.notfound {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: $bg-page;
  h1 { font-size: 96px; color: $primary; margin: 0; }
  p { color: $text-secondary; margin: 0 0 24px; }
}
.btn {
  padding: 10px 32px;
  background: $primary;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
</style>
