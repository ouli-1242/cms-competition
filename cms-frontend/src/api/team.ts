/**
 * 团队相关 API
 */
import request from './request'

// 获取我的团队列表
export function getMyTeams() {
  return request.get('/student/teams')
}

// 获取团队详情
export function getTeamDetail(id: number) {
  return request.get(`/student/teams/${id}`)
}

// 创建团队
export function createTeam(data: {
  competitionId: number
  teamName: string
  description?: string
}) {
  return request.post('/student/teams', data)
}

// 加入团队（通过邀请码）
export function joinTeam(data: { competitionId: number; inviteCode: string }) {
  return request.post('/student/teams/join', data)
}

// 退出团队
export function quitTeam(id: number) {
  return request.post(`/student/teams/${id}/quit`)
}

// 审核加入申请（队长）
export function auditJoinRequest(data: { requestId: number; approve: boolean }) {
  return request.post('/student/teams/audit', data)
}

// 移除团队成员（队长）
export function removeMember(data: { teamId: number; userId: number }) {
  return request.delete(`/student/teams/${data.teamId}/members/${data.userId}`)
}

// 提交团队报名
export function submitTeamRegistration(data: {
  teamId: number
  attachments: string[]
  remark?: string
}) {
  return request.post('/student/teams/submit', data)
}
