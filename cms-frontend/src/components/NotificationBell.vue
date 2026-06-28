<script setup lang="ts">
/**
 * 通知铃铛组件
 * - 显示未读数
 * - 下拉展示最近通知
 * - 支持标记已读 / 全部已读
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { getNotifications, getUnreadCount, markNotificationRead, markAllNotificationsRead } from '@/api/notification'
import { ElMessage } from 'element-plus'

const unreadCount = ref(0)
const notifications = ref<any[]>([])
const loading = ref(false)
const showPopover = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

async function loadUnread() {
  try {
    const res: any = await getUnreadCount()
    unreadCount.value = typeof res === 'number' ? res : 0
  } catch {
    // silent
  }
}

async function loadNotifications() {
  loading.value = true
  try {
    const res: any = await getNotifications({ pageNum: 1, pageSize: 10 })
    notifications.value = res?.records || []
  } catch {
    notifications.value = []
  } finally {
    loading.value = false
  }
}

function onOpen() {
  loadNotifications()
}

async function handleRead(item: any) {
  if (item.isRead === 0) {
    try {
      await markNotificationRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch {}
  }
}

async function handleReadAll() {
  try {
    await markAllNotificationsRead()
    notifications.value.forEach((n: any) => n.isRead = 1)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch {}
}

function formatTime(t: string) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function typeTag(t: string) {
  const map: Record<string, string> = { REGISTRATION: '报名', TEAM: '团队', SYSTEM: '系统' }
  return map[t] || '通知'
}

onMounted(() => {
  loadUnread()
  timer = setInterval(loadUnread, 30000) // 每30秒轮询
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <el-popover
    :visible="showPopover"
    placement="bottom-end"
    :width="340"
    trigger="click"
    popper-class="notification-popover"
  >
    <template #reference>
      <div class="bell-wrap" :class="{ 'has-unread': unreadCount > 0 }" @click="showPopover = !showPopover; onOpen()">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
          <el-icon :size="20"><Bell /></el-icon>
        </el-badge>
      </div>
    </template>

    <div class="notif-panel">
      <div class="notif-header">
        <span class="notif-title">通知</span>
        <button v-if="unreadCount > 0" class="read-all-btn" @click="handleReadAll">全部已读</button>
      </div>

      <div v-if="loading" class="notif-loading">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="notifications.length === 0" class="notif-empty">
        <p>暂无通知</p>
      </div>

      <div v-else class="notif-list">
        <div
          v-for="n in notifications"
          :key="n.id"
          class="notif-item"
          :class="{ unread: n.isRead === 0 }"
          @click="handleRead(n)"
        >
          <div class="notif-meta">
            <el-tag size="small" effect="plain" round>{{ typeTag(n.type) }}</el-tag>
            <span class="notif-time">{{ formatTime(n.createTime) }}</span>
          </div>
          <div class="notif-title">{{ n.title }}</div>
          <div class="notif-content">{{ n.content }}</div>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.bell-wrap {
  position: relative;
  cursor: pointer;
  padding: $space-2;
  border-radius: $radius-base;
  transition: all $transition-base;
  color: $text-regular;

  &:hover {
    background: $primary-50;
    color: $primary;
    transform: scale(1.08);
  }

  &:active {
    transform: scale(0.95);
  }
}

@keyframes bellRing {
  0%, 100% { transform: rotate(0); }
  15% { transform: rotate(12deg); }
  30% { transform: rotate(-10deg); }
  45% { transform: rotate(8deg); }
  60% { transform: rotate(-6deg); }
  75% { transform: rotate(3deg); }
  90% { transform: rotate(-1deg); }
}

.bell-wrap.has-unread:hover .el-icon {
  animation: bellRing 0.6s ease-in-out;
}

.notif-panel {
  max-height: 420px;
  display: flex;
  flex-direction: column;
}

.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: $space-2;
  border-bottom: 1px solid $border-light;
  margin-bottom: $space-2;
}

.notif-title {
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.read-all-btn {
  border: none;
  background: transparent;
  color: $primary;
  font-size: $font-size-xs;
  cursor: pointer;
  &:hover {
    text-decoration: underline;
  }
}

.notif-loading {
  padding: $space-3 0;
}

.notif-empty {
  text-align: center;
  padding: $space-6 0;
  color: $text-placeholder;
  font-size: $font-size-sm;
  p { margin: 0; }
}

.notif-list {
  overflow-y: auto;
  max-height: 340px;
}

.notif-item {
  padding: $space-2 $space-1;
  border-bottom: 1px solid $border-light;
  cursor: pointer;
  transition: background $transition-fast;
  &:last-child { border-bottom: none; }
  &:hover { background: $bg-page; }
  &.unread {
    background: rgba(43, 108, 176, 0.04);
    .notif-title { font-weight: $font-weight-semibold; }
  }
}

.notif-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.notif-time {
  font-size: $font-size-xs;
  color: $text-placeholder;
}

.notif-item .notif-title {
  font-size: $font-size-sm;
  color: $text-primary;
  margin-bottom: 2px;
}

.notif-content {
  font-size: $font-size-xs;
  color: $text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
