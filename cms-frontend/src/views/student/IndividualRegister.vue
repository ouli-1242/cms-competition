<script setup lang="ts">
/**
 * 个人赛一键报名页
 * - 状态 1：可报名（显示一键报名按钮）
 * - 状态 2：已截止（弹窗提示）
 * - 状态 3：已报名（弹窗提示）
 * - 状态 4：未登录（跳登录）
 * - 状态 5：人数已满（弹窗提示）
 * - 状态 6：报名成功（弹窗提示）
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCompetitionDetail } from '@/api/public'
import { registerIndividual } from '@/api/registration'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ====== 数据 ======
const competition = ref<any>(null)
const loading = ref(false)

// ====== 弹窗状态 ======
type DialogType = 'none' | 'confirm' | 'ended' | 'duplicate' | 'login' | 'full' | 'success'
const dialogType = ref<DialogType>('none')
const submitting = ref(false)

// ====== 占位数据 ======
const placeholder: any = {
  id: 1,
  title: '2025年校园科技创新大赛',
  type: '个人赛',
  category: '学科竞赛',
  registerStart: '2025-03-03 00:00',
  registerEnd: '2025-04-10 23:59',
  compStart: '2025-04-20 00:00',
  compEnd: '2025-05-10 23:59',
  currentCount: 45,
  maxCount: 100,
  status: 1
}

async function loadData() {
  loading.value = true
  try {
    const data = await getCompetitionDetail(Number(route.params.id))
    competition.value = data || placeholder
  } catch (e) {
    competition.value = placeholder
  } finally {
    loading.value = false
  }
}


// ====== 报名按钮状态 ======
function isRegistrationPeriod(c: any): boolean {
  if (!c) return false
  const now = Date.now()
  const start = new Date(c.registerStart).getTime()
  const end = new Date(c.registerEnd).getTime()
  return now >= start && now <= end
}

async function onRegisterClick() {
  // 未登录 → 跳登录
  if (!userStore.isLoggedIn) {
    dialogType.value = 'login'
    return
  }

  // 检查是否在报名期
  if (!isRegistrationPeriod(competition.value)) {
    dialogType.value = 'ended'
    return
  }

  // 检查人数是否已满
  if (isCompetitionFull(competition.value)) {
    dialogType.value = 'full'
    return
  }

  // 确认弹窗
  dialogType.value = 'confirm'
}

// 判断人数是否已满
function isCompetitionFull(c: any): boolean {
  if (!c) return false
  const current = c.currentCount ?? 0
  const max = c.maxCount ?? 0
  return max > 0 && current >= max
}

async function handleConfirm() {
  if (submitting.value) return
  submitting.value = true
  try {
    await registerIndividual(competition.value.id)
    // 报名成功弹窗（状态 6）
    dialogType.value = 'none'
    setTimeout(() => {
      dialogType.value = 'success'
    }, 200)
  } catch (e) {
    // 错误已拦截
  } finally {
    submitting.value = false
  }
}

function goLogin() {
  dialogType.value = 'none'
  router.push({ name: 'Login', query: { redirect: route.fullPath } })
}

function closeDialog() {
  dialogType.value = 'none'
}

// 6 个弹窗的显隐（计算属性）
const dialogVisible = computed(() => ({
  confirm: dialogType.value === 'confirm',
  ended: dialogType.value === 'ended',
  duplicate: dialogType.value === 'duplicate',
  login: dialogType.value === 'login',
  full: dialogType.value === 'full',
  success: dialogType.value === 'success'
}))

// 成功弹窗按钮操作
function goMyRegistrations() {
  dialogType.value = 'none'
  router.push('/student-center/my-registrations')
}

function statusText(c: any): string {
  if (!c) return ''
  if (!isRegistrationPeriod(c)) {
    return new Date(c.registerEnd).getTime() < Date.now() ? '报名已截止' : '报名未开始'
  }
  return '报名进行中'
}

function statusColor(c: any): string {
  if (!c) return ''
  if (!isRegistrationPeriod(c)) return '#a0aec0'
  return '#48bb78'
}

function formatDate(d: string): string {
  if (!d) return ''
  return d.replace(' 00:00', '').replace(' 23:59', '')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="reg-page">
    <!-- 顶部返回栏 -->
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <span>←</span>
      </button>
      <h1 class="page-title">校园活动管理平台</h1>
      <div class="placeholder"></div>
    </div>

    <!-- 返回链接 -->
    <div class="back-link" @click="router.back()">
      <span class="back-icon">←</span>返回活动列表
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-block">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="competition">
      <!-- 竞赛详情卡 -->
      <div class="info-card">
        <h2 class="card-title">竞赛详情</h2>
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12">
            <div class="info-row">
              <span class="info-label">竞赛名称</span>
              <span class="info-value highlight">{{ competition.title }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">竞赛类型</span>
              <span class="info-value highlight">{{ competition.type || '个人赛' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">报名时间</span>
              <span class="info-value">
                {{ formatDate(competition.registerStart) }} 至 {{ formatDate(competition.registerEnd) }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">比赛时间</span>
              <span class="info-value">
                {{ formatDate(competition.compStart) }} 至 {{ formatDate(competition.compEnd) }}
              </span>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12">
            <div class="info-row">
              <span class="info-label">团体人数</span>
              <span class="info-value">
                {{ competition.currentCount || 45 }} / {{ competition.maxCount || 100 }} 人
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">报名状态</span>
              <span class="info-value" :style="{ color: statusColor(competition) }">
                ● {{ statusText(competition) }}
              </span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 个人赛报名卡 -->
      <div class="action-card">
        <h3 class="action-title">个人赛报名</h3>
        <p class="action-desc">此为个人赛，点击下方按钮即可完成报名。</p>
        <button
          class="btn-primary"
          :disabled="!isRegistrationPeriod(competition)"
          @click="onRegisterClick"
        >
          ⚡ 一键报名
        </button>
      </div>
    </template>

    <!-- ============ 弹窗 1: 报名确认 ============ -->
    <el-dialog
      :model-value="dialogVisible.confirm"
      @update:model-value="(val: boolean) => { if (!val) dialogType = 'none' }"
      width="480px"
      :show-close="true"
      :close-on-click-modal="false"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <h3>报名确认</h3>
        </div>
      </template>
      <div class="dialog-body">
        <div class="alert alert-info">
          <div class="alert-icon">i</div>
          <div>
            <p class="alert-title">确认报名该竞赛吗？</p>
            <p class="alert-desc">报名提交后将进入审核流程，请确认信息无误。</p>
          </div>
        </div>

        <div v-if="competition" class="confirm-info">
          <div class="confirm-row">
            <span class="confirm-label">竞赛名称：</span>
            <span class="confirm-value">{{ competition.title }}</span>
          </div>
          <div class="confirm-row">
            <span class="confirm-label">竞赛类型：</span>
            <span class="confirm-value">{{ competition.category || '学科竞赛' }}（个人赛）</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-confirm" :disabled="submitting" @click="handleConfirm">
            {{ submitting ? '提交中...' : '确认报名' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- ============ 弹窗 2: 报名已截止 ============ -->
    <el-dialog
      :model-value="dialogVisible.ended"
      @update:model-value="(val: boolean) => { if (!val) dialogType = 'none' }"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <h3>提示</h3>
        </div>
      </template>
      <div class="dialog-body">
        <div class="alert alert-danger">
          <div class="alert-icon">⊗</div>
          <div>
            <p class="alert-title">报名已截止</p>
            <p class="alert-desc">该竞赛不在报名期内，无法进行报名操作。</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok" @click="closeDialog">知道了</button>
        </div>
      </template>
    </el-dialog>

    <!-- ============ 弹窗 3: 不可重复报名 ============ -->
    <el-dialog
      :model-value="dialogVisible.duplicate"
      @update:model-value="(val: boolean) => { if (!val) dialogType = 'none' }"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <h3>提示</h3>
        </div>
      </template>
      <div class="dialog-body">
        <div class="alert alert-warning">
          <div class="alert-icon">⚠</div>
          <div>
            <p class="alert-title">不可重复报名</p>
            <p class="alert-desc">您已报名该竞赛，无需重复提交。</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok" @click="closeDialog">知道了</button>
        </div>
      </template>
    </el-dialog>

    <!-- ============ 弹窗 4: 未登录 ============ -->
    <el-dialog
      :model-value="dialogVisible.login"
      @update:model-value="(val: boolean) => { if (!val) dialogType = 'none' }"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <h3>请先登录</h3>
        </div>
      </template>
      <div class="dialog-body">
        <div class="alert alert-info">
          <div class="alert-icon">!</div>
          <div>
            <p class="alert-title">您还未登录</p>
            <p class="alert-desc">报名需要先登录账号，是否前往登录？</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-confirm" @click="goLogin">去登录</button>
        </div>
      </template>
    </el-dialog>

    <!-- ============ 弹窗 5: 人数已满 ============ -->
    <el-dialog
      :model-value="dialogVisible.full"
      @update:model-value="(val: boolean) => { if (!val) dialogType = 'none' }"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <h3>提示</h3>
        </div>
      </template>
      <div class="dialog-body">
        <div class="alert alert-danger">
          <div class="alert-icon">⊗</div>
          <div>
            <p class="alert-title">人数已满</p>
            <p class="alert-desc">该竞赛报名人数已达上限，无法继续报名。</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok" @click="closeDialog">知道了</button>
        </div>
      </template>
    </el-dialog>

    <!-- ============ 弹窗 6: 报名成功 ============ -->
    <el-dialog
      :model-value="dialogVisible.success"
      @update:model-value="(val: boolean) => { if (!val) dialogType = 'none' }"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <h3>报名结果</h3>
        </div>
      </template>
      <div class="dialog-body">
        <div class="alert alert-success">
          <div class="alert-icon">✓</div>
          <div>
            <p class="alert-title">报名成功</p>
            <p class="alert-desc">您的报名已提交，请等待审核。可在报名记录中查看进度。</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">关闭</button>
          <button class="btn-confirm" @click="goMyRegistrations">查看报名记录</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

@keyframes btnPulse {
  0%, 100% { box-shadow: 0 4px 16px rgba(43, 108, 176, 0.3); }
  50% { box-shadow: 0 4px 24px rgba(43, 108, 176, 0.5); }
}

.reg-page {
  max-width: 560px;
  margin: 0 auto;
  padding: $space-4 $space-6 $space-8;
}

// ===== 顶部返回栏 =====
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-2 0 $space-3;
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

// ===== 返回链接 =====
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin: 0 0 $space-3;
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  transition: color $transition-fast;

  &:hover {
    color: $primary;
  }
}

.back-icon {
  font-size: 16px;
}

// ===== 加载 =====
.loading-block {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-6;
}

// ===== 详情卡 =====
.info-card {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-6;
  margin-bottom: $space-4;
}

.card-title {
  margin: 0 0 $space-5;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  padding-bottom: $space-3;
  border-bottom: 1px solid $border-light;
}

.info-row {
  display: flex;
  align-items: center;
  padding: $space-3 0;
  border-bottom: 1px dashed $border-light;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  width: 100px;
  font-size: $font-size-sm;
  color: $text-secondary;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  font-size: $font-size-base;
  color: $text-primary;
}

.highlight {
  color: $primary;
  font-weight: $font-weight-medium;
}

// ===== 报名卡 =====
.action-card {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-6;
}

.action-title {
  margin: 0 0 $space-3;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.action-desc {
  margin: 0 0 $space-5;
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.7;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: $space-2;
  height: 48px;
  padding: 0 $space-8;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.3);
  transition: all $transition-base;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.15) 50%, transparent 100%);
    transform: translateX(-100%);
    transition: transform 0.6s ease;
  }

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(43, 108, 176, 0.45);
    animation: btnPulse 2s ease-in-out infinite;

    &::after {
      transform: translateX(100%);
    }
  }
  &:active:not(:disabled) {
    transform: translateY(0) scale(0.97);
    animation: none;
  }
  &:disabled {
    background: $bg-disabled;
    color: $text-disabled;
    cursor: not-allowed;
    box-shadow: none;
  }
}

// ===== 弹窗通用 =====
:deep(.el-dialog__header) {
  padding: $space-5 $space-6 0;
  margin-right: 0;
}

.dialog-header {
  h3 {
    margin: 0;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $text-primary;
  }
}

.dialog-body {
  padding: $space-3 0 $space-2;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  padding-top: $space-3;

  &.center {
    justify-content: flex-end;
  }
}

// ===== 警告条 =====
.alert {
  display: flex;
  gap: $space-3;
  padding: $space-4 $space-5;
  border-radius: $radius-base;
  margin-bottom: $space-4;

  &.alert-info {
    background: #eaf2fb;
    color: $primary;
  }
  &.alert-danger {
    background: #fef5f5;
    color: $danger;
  }
  &.alert-warning {
    background: #fff5e6;
    color: $warning;
  }
  &.alert-success {
    background: #e6f7ed;
    color: $success;
  }
  &.alert-danger {
    background: #fef5f5;
    color: $danger;
  }
}

.alert-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: $font-weight-bold;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
}

.alert-title {
  margin: 0 0 4px;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
}

.alert-desc {
  margin: 0;
  font-size: $font-size-sm;
  opacity: 0.85;
}

// ===== 确认信息 =====
.confirm-info {
  background: $bg-page;
  border-radius: $radius-base;
  padding: $space-4 $space-5;
}

.confirm-row {
  display: flex;
  padding: $space-1 0;
  font-size: $font-size-sm;

  &:first-child {
    margin-bottom: $space-2;
  }
}

.confirm-label {
  color: $text-secondary;
  white-space: nowrap;
  flex-shrink: 0;
}

.confirm-value {
  color: $text-primary;
  flex: 1;
}

// ===== 按钮 =====
.btn-cancel {
  height: 40px;
  padding: 0 $space-5;
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

.btn-confirm {
  height: 40px;
  padding: 0 $space-5;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
  transition: all $transition-fast;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
  }
  &:disabled {
    background: $bg-disabled;
    color: $text-disabled;
    cursor: not-allowed;
    box-shadow: none;
  }
}

.btn-ok {
  height: 40px;
  padding: 0 $space-6;
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

// ===== 响应式 =====
@media (max-width: 768px) {
  .reg-page {
    padding: $space-3 $space-4;
  }
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: $space-1;
  }
  .info-label {
    width: auto;
  }
}
</style>
