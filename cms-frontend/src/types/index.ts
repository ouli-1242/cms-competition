/**
 * 全局类型定义
 * 与后端实体、DTO 对应
 */

// ====== 用户 ======
export type Role = 'STUDENT' | 'TEACHER' | 'ADMIN' | 'VISITOR'

export interface UserInfo {
  id: number
  username: string
  role: Role
  realName?: string
  nickname?: string
  college?: string
  phone?: string
  email?: string
  avatar?: string
}

// ====== 竞赛 ======
export interface Competition {
  id: number
  title: string
  category: string
  cover: string
  description: string
  type: number       // 1=个人, 2=团队
  maxTeamSize: number
  minTeamSize?: number
  registerStart: string
  registerEnd: string
  compStart: string
  compEnd: string
  status: number     // 0=下架, 1=上架
  teacherId?: number
  registrationCount?: number
  createTime?: string
}

// ====== 报名 ======
export interface Registration {
  id: number
  competitionId: number
  userId: number
  status: number     // 0=待审核, 1=已通过, 2=已拒绝
  description?: string
  attachment?: string
  registerTime: string
  reviewRemark?: string
  reviewTime?: string
  studentName?: string
  competitionTitle?: string
  competitionName?: string
}

export interface TeamRegistration {
  id: number
  competitionId: number
  teamId: number
  status: number
  description?: string
  attachment?: string
  registerTime: string
  reviewRemark?: string
  reviewTime?: string
  teamName?: string
  competitionTitle?: string
}

// ====== 团队 ======
export interface Team {
  id: number
  teamName: string
  competitionId: number
  captainId: number
  maxSize: number
  status: number
  slogan?: string
  inviteCode: string
  dissolvedAt?: string
}

export interface TeamMember {
  id: number
  userId: number
  teamId: number
  role: number       // 0=队员, 1=队长
  status: number     // 0=待审核, 1=已加入, 2=已拒绝
  joinTime: string
  realName?: string
  username?: string
  avatar?: string
  college?: string
  memberId?: number
}

// ====== 轮播 ======
export interface Banner {
  id: number
  title: string
  imageUrl: string
  linkUrl?: string
  sort: number
  status: number
}

// ====== 分页 ======
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

// ====== 统计 ======
export interface AdminStats {
  competitionCount: number
  totalCount: number
  individualCount: number
  teamCount: number
  passedCount: number
  pendingCount: number
  rejectedCount?: number
  passRate: number
}

export interface CompetitionStats extends AdminStats {
  id: number
  title: string
}

// ====== 通知 ======
export interface Notification {
  id: number
  title: string
  content: string
  type: string
  isRead: number
  createTime: string
  targetId?: number
}
