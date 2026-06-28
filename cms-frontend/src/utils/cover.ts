/**
 * 封面 / 卡片工具函数
 */

/** 卡片封面渐变色（按标题首个字 hash 选色） */
export function coverStyle(title: string): { background: string } {
  const colors = [
    ['#4a5568', '#2d3748'],
    ['#2b6cb0', '#2c5282'],
    ['#d69e2e', '#b7791f'],
    ['#9f7aea', '#6b46c1'],
    ['#e53e3e', '#c53030'],
    ['#48bb78', '#2f855a']
  ]
  const idx = (title?.charCodeAt(0) || 0) % colors.length
  return {
    background: `linear-gradient(135deg, ${colors[idx][0]}, ${colors[idx][1]})`
  }
}
