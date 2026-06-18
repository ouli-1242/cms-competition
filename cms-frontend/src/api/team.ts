/**
 * 团队相关 API
 * 对应 StudentTeamController（/api/student/team）
 */
import request from './request'

/** 创建团队 POST /api/student/team  (@RequestBody TeamDTO) */
export function createTeam(data: {
  teamName: string
  inviteCode?: string
  competitionId: number
  maxSize?: number
  slogan?: string
  studentIds?: number[]
  teacherId?: number
}) {
  return request.post('/student/team', data)
}

/** 加入团队 POST /api/student/team/join  (@RequestParam inviteCode) */
export function joinTeam(inviteCode: string) {
  return request.post('/student/team/join', null, { params: { inviteCode } })
}

/** 我的团队 GET /api/student/team/my */
export function getMyTeams() {
  return request.get('/student/team/my')
}

/** 审核成员 PUT /api/student/team/review  (@RequestParam memberId, pass) */
export function reviewMember(memberId: number, pass: boolean) {
  return request.put('/student/team/review', null, { params: { memberId, pass } })
}

/** 踢出成员 PUT /api/student/team/kick  (@RequestParam teamId, userId) */
export function kickMember(teamId: number, userId: number) {
  return request.put('/student/team/kick', null, { params: { teamId, userId } })
}

/** 转让队长 PUT /api/student/team/transfer  (@RequestParam teamId, newCaptainId) */
export function transferCaptain(teamId: number, newCaptainId: number) {
  return request.put('/student/team/transfer', null, { params: { teamId, newCaptainId } })
}

/** 提交团队报名 POST /api/student/team/submit  (@RequestParam teamId, description, attachment) */
export function submitTeamRegistration(teamId: number, description?: string, attachment?: string) {
  return request.post('/student/team/submit', null, {
    params: { teamId, description, attachment }
  })
}

// --- 以下接口后端不存在，暂时注释 ---
// export function auditJoinRequest(data: { requestId: number; approve: boolean }) {
//   return request.post('/student/team/audit', data)
// }

/** 团队详情 GET /api/student/team/{id} */
export function getTeamDetail(id: number) {
  return request.get(`/student/team/${id}`)
}

/** 退出团队 POST /api/student/team/{id}/quit */
export function quitTeam(id: number) {
  return request.post(`/student/team/${id}/quit`)
}

/** 解散团队 POST /api/student/team/{id}/dissolve */
export function dissolveTeam(id: number) {
  return request.post(`/student/team/${id}/dissolve`)
}

/** 恢复已解散团队 POST /api/student/team/{id}/recover */
export function recoverTeam(id: number) {
  return request.post(`/student/team/${id}/recover`)
}

// ===== 搜索学生/教师 =====

/** 搜索学生 GET /api/student/team/search-students */
export function searchStudents(keyword?: string) {
  return request.get('/student/team/search-students', { params: { keyword } })
}

/** 搜索教师 GET /api/student/team/search-teachers */
export function searchTeachers(keyword?: string) {
  return request.get('/student/team/search-teachers', { params: { keyword } })
}

/** 邀请指导老师 POST /api/student/team/{teamId}/invite-teacher */
export function inviteTeacher(teamId: number, teacherId: number, remark?: string) {
  return request.post(`/student/team/${teamId}/invite-teacher`, null, {
    params: { teacherId, remark }
  })
}

/** 团队邀请记录 GET /api/student/team/{teamId}/advisor-invitations */
export function getAdvisorInvitations(teamId: number) {
  return request.get(`/student/team/${teamId}/advisor-invitations`)
}

/** 撤销邀请 DELETE /api/student/team/advisor-invite/{inviteId} */
export function cancelAdvisorInvite(inviteId: number) {
  return request.delete(`/student/team/advisor-invite/${inviteId}`)
}
