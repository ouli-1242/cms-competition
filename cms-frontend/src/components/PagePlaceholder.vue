<script setup lang="ts">
defineProps<{ name?: string }>()
</script>

<template>
  <div class="placeholder">
    <h2>{{ name || '页面' }} - 待开发</h2>
    <p>该模块页面将在后续迭代中实现。</p>
  </div>
</template>

<style lang="scss" scoped>
.placeholder {
  padding: 60px;
  background: #fff;
  border-radius: 8px;
  text-align: center;
  color: #4a5568;
  h2 { margin: 0 0 12px; color: #1a202c; }
  p { margin: 0; color: #a0aec0; }
}
</style>
