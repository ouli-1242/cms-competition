/**
 * 状态显示工具函数
 * 统一各组件中重复的 statusText/statusType
 */

/** 审核状态 → 文案 */
export function statusText(status: number): string {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  return '已拒绝'
}

/** 审核状态 → Element Plus tag 颜色类型 */
export function statusType(status: number): 'warning' | 'success' | 'danger' {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  return 'danger'
}

/** 竞赛报名状态 → 数字 (0=即将开始, 1=报名中, 2=已截止) */
export function computeStatus(c: { registerStart?: string; registerEnd?: string }): number {
  const now = Date.now()
  const start = c.registerStart ? new Date(c.registerStart).getTime() : 0
  const end = c.registerEnd ? new Date(c.registerEnd).getTime() : 0
  if (now < start) return 0
  if (now > end) return 2
  return 1
}

/** 竞赛报名状态 → 文案 */
export function competitionStatusText(c: { registerStart?: string; registerEnd?: string }): string {
  const s = computeStatus(c)
  if (s === 1) return '报名中'
  if (s === 2) return '已截止'
  return '即将开始'
}

/** 竞赛报名状态 → Element Plus tag 颜色类型 */
export function competitionStatusType(c: { registerStart?: string; registerEnd?: string }): 'success' | 'info' | 'warning' {
  const s = computeStatus(c)
  if (s === 1) return 'success'
  if (s === 2) return 'info'
  return 'warning'
}

/** 团队邀请/成员状态 → 文案 */
export function teamStatusText(status: number): string {
  if (status === 0) return '待审核'
  if (status === 1) return '已确认'
  return '已拒绝'
}

/** 团队邀请/成员状态 → Element Plus tag 颜色类型 */
export function teamStatusType(status: number): 'warning' | 'success' | 'danger' {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  return 'danger'
}
