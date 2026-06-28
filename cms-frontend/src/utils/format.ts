/**
 * 格式化工具函数
 */

/** 日期格式化：yyyy-MM-dd */
export function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 从 URL 提取竞赛 ID，如 /competitions/123 → 123 */
export function extractIdFromUrl(url: string): number {
  const match = url.match(/\/competitions\/(\d+)/)
  return match ? Number(match[1]) : 0
}
