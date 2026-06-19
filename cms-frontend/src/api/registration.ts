/**
 * 报名相关 API
 * 对应 StudentRegistrationController（/api/student/registration）
 * 以及 TeacherRegistrationController（/api/teacher/...）
 */
import request from './request'

// ===== 学生报名 (StudentRegistrationController /api/student/registration) =====

/** 个人赛报名 POST /api/student/registration  (@RequestParam competitionId, description, attachment) */
export function registerIndividual(competitionId: number, description?: string, attachment?: string) {
  return request.post('/student/registration', null, {
    params: { competitionId, description, attachment }
  })
}

/** 我的报名记录分页 GET /api/student/registration/page */
export function getMyRegistrations(params: {
  pageNum?: number
  pageSize?: number
  status?: number
}) {
  return request.get('/student/registration/page', { params })
}

/** 我的团队报名分页 GET /api/student/registration/team/page */
export function getMyTeamRegistrations(params: {
  pageNum?: number
  pageSize?: number
  status?: number
}) {
  return request.get('/student/registration/team/page', { params })
}

/** 取消报名 DELETE /api/student/registration/{id} */
export function cancelRegistration(id: number) {
  return request.delete(`/student/registration/${id}`)
}

/** 报名详情 GET /api/student/registration/{id} */
export function getRegistrationDetail(id: number) {
  return request.get(`/student/registration/${id}`)
}

/** 编辑报名 PUT /api/student/registration/{id} */
export function updateRegistration(id: number, description?: string, attachment?: string) {
  return request.put(`/student/registration/${id}`, null, { params: { description, attachment } })
}

/** 团队报名详情 GET /api/student/registration/team/{id} */
export function getTeamRegDetail(id: number) {
  return request.get(`/student/registration/team/${id}`)
}

/** 编辑团队报名 PUT /api/student/registration/team/{id} */
export function updateTeamRegistration(id: number, description?: string, attachment?: string) {
  return request.put(`/student/registration/team/${id}`, null, { params: { description, attachment } })
}

// --- 以下接口后端不存在，暂时注释 ---
// export function checkMyRegistration(competitionId: number) {
//   return request.get<boolean>('/student/registration/check', { params: { competitionId } })
// }

// ===== 教师审核报名 (TeacherRegistrationController /api/teacher) =====

/** 教师-个人报名分页 GET /api/teacher/registration/page */
export function getTeacherRegistrations(params: {
  pageNum?: number
  pageSize?: number
  competitionId?: number
  status?: number
  studentName?: string
}) {
  return request.get('/teacher/registration/page', { params })
}

/** 教师-审核个人报名 PUT /api/teacher/registration/{id}/review  (@RequestParam pass, remark) */
export function teacherAuditRegistration(id: number, pass: boolean, remark?: string) {
  return request.put(`/teacher/registration/${id}/review`, null, { params: { pass, remark } })
}

/** 教师-团队报名分页 GET /api/teacher/team-registration/page */
export function getTeacherTeamRegistrations(params: {
  pageNum?: number
  pageSize?: number
  competitionId?: number
  status?: number
  teamName?: string
}) {
  return request.get('/teacher/team-registration/page', { params })
}

/** 教师-审核团队报名 PUT /api/teacher/team-registration/{id}/review  (@RequestParam pass, remark) */
export function teacherAuditTeamRegistration(id: number, pass: boolean, remark?: string) {
  return request.put(`/teacher/team-registration/${id}/review`, null, { params: { pass, remark } })
}

// ===== 教师竞赛 (TeacherCompetitionController /api/teacher/competition) =====

/** 教师-我指导的竞赛分页 GET /api/teacher/competition/page */
export function getTeacherCompetitions(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}) {
  return request.get('/teacher/competition/page', { params })
}

/** 教师-竞赛详情 GET /api/teacher/competition/{id} */
export function getTeacherCompetitionDetail(id: number) {
  return request.get(`/teacher/competition/${id}`)
}

// ===== 教师统计 (TeacherStatisticsController /api/teacher/statistics) =====

/** 教师-我的统计 GET /api/teacher/statistics */
export function getTeacherStatistics() {
  return request.get('/teacher/statistics')
}

/** 教师-单个竞赛统计 GET /api/teacher/statistics/competition/{id} */
export function getTeacherCompetitionStats(id: number) {
  return request.get(`/teacher/statistics/competition/${id}`)
}

// ===== 教师指导邀请 (TeacherAdvisorController /api/teacher/advisor) =====

/** 教师-指导邀请列表 GET /api/teacher/advisor/invitations */
export function getAdvisorInvitations(status?: number) {
  return request.get('/teacher/advisor/invitations', { params: { status } })
}

/** 教师-接受邀请 POST /api/teacher/advisor/{id}/accept */
export function acceptAdvisorInvitation(id: number) {
  return request.post(`/teacher/advisor/${id}/accept`)
}

/** 教师-拒绝邀请 POST /api/teacher/advisor/{id}/reject */
export function rejectAdvisorInvitation(id: number) {
  return request.post(`/teacher/advisor/${id}/reject`)
}

/** 教师-我指导的团队 GET /api/teacher/advisor/teams */
export function getAdvisedTeams() {
  return request.get('/teacher/advisor/teams')
}
