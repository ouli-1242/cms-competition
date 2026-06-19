/**
 * 管理员后台 API
 * 对应 Admin*Controller（/api/admin/...）
 */
import request from './request'

// ===== 竞赛管理 (AdminCompetitionController /api/admin/competition) =====

/** 竞赛分页 GET /api/admin/competition/page */
export function getCompetitions(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
  type?: number
}) {
  return request.get('/admin/competition/page', { params })
}

// NOTE: 后端无 GET /api/admin/competition/{id}，如需详情可用公开接口 /competition/public/{id}
// export function getCompetitionById(id: number) {
//   return request.get(`/admin/competition/${id}`)
// }

/** 发布竞赛 POST /api/admin/competition */
export function createCompetition(data: {
  title: string
  description?: string
  category?: string
  type?: number
  maxTeamSize?: number
  minTeamSize?: number
  registerStart?: string
  registerEnd?: string
  compStart?: string
  compEnd?: string
  cover?: string
  attachment?: string
  teacherId?: number
}) {
  return request.post('/admin/competition', data)
}

/** 更新竞赛 PUT /api/admin/competition/{id} */
export function updateCompetition(id: number, data: any) {
  return request.put(`/admin/competition/${id}`, data)
}

/** 删除竞赛 DELETE /api/admin/competition/{id} */
export function deleteCompetition(id: number) {
  return request.delete(`/admin/competition/${id}`)
}

/** 变更竞赛状态 PUT /api/admin/competition/{id}/status  (status 为 @RequestParam) */
export function toggleCompetitionStatus(id: number, status: number) {
  return request.put(`/admin/competition/${id}/status`, null, { params: { status } })
}

// ===== 报名管理 (AdminRegistrationController /api/admin) =====

/** 个人报名分页 GET /api/admin/registration/page */
export function getRegistrations(params: {
  pageNum?: number
  pageSize?: number
  competitionId?: number
  status?: number
  studentName?: string
}) {
  return request.get('/admin/registration/page', { params })
}

/** 审核个人报名 PUT /api/admin/registration/{id}/review  (@RequestParam pass, remark) */
export function auditRegistration(id: number, pass: boolean, remark?: string) {
  return request.put(`/admin/registration/${id}/review`, null, { params: { pass, remark } })
}

/** 团队报名分页 GET /api/admin/team-registration/page */
export function getTeamRegistrations(params: {
  pageNum?: number
  pageSize?: number
  competitionId?: number
  status?: number
}) {
  return request.get('/admin/team-registration/page', { params })
}

/** 审核团队报名 PUT /api/admin/team-registration/{id}/review  (@RequestParam pass, remark) */
export function auditTeamRegistration(id: number, pass: boolean, remark?: string) {
  return request.put(`/admin/team-registration/${id}/review`, null, { params: { pass, remark } })
}

// ===== 轮播图管理 (AdminBannerController /api/admin/banner) =====

/** 轮播图分页 GET /api/admin/banner/page */
export function getBanners(params?: { pageNum?: number; pageSize?: number }) {
  return request.get('/admin/banner/page', { params })
}

/** 新增轮播图 POST /api/admin/banner */
export function createBanner(data: {
  title?: string
  imageUrl: string
  linkUrl?: string
  sort?: number
  status?: number
  startTime?: string
  endTime?: string
}) {
  return request.post('/admin/banner', data)
}

/** 更新轮播图 PUT /api/admin/banner/{id} */
export function updateBanner(id: number, data: any) {
  return request.put(`/admin/banner/${id}`, data)
}

/** 删除轮播图 DELETE /api/admin/banner/{id} */
export function deleteBanner(id: number) {
  return request.delete(`/admin/banner/${id}`)
}

// ===== 热门推荐管理 (AdminHotController /api/admin/hot) =====

/** 添加热门推荐 POST /api/admin/hot  (@RequestParam competitionId, sort) */
export function addHotRecommend(competitionId: number, sort: number) {
  return request.post('/admin/hot', null, { params: { competitionId, sort } })
}

/** 取消热门推荐 DELETE /api/admin/hot/{competitionId} */
export function removeHotRecommend(competitionId: number) {
  return request.delete(`/admin/hot/${competitionId}`)
}

/** 自动推荐 POST /api/admin/hot/auto  (@RequestParam topN) */
export function autoHotRecommend(topN: number = 10) {
  return request.post('/admin/hot/auto', null, { params: { topN } })
}

// ===== 数据统计 (AdminStatisticsController /api/admin/statistics) =====

/** 总览统计 GET /api/admin/statistics/overview */
export function getAdminStats() {
  return request.get('/admin/statistics/overview')
}

/** 单个竞赛统计 GET /api/admin/statistics/competition/{id} */
export function getCompetitionStats(id: number) {
  return request.get(`/admin/statistics/competition/${id}`)
}

// ===== 资料修改审批 (AdminProfileChangeController /api/admin/profile-change) =====

/** 修改申请分页 GET /api/admin/profile-change/page */
export function getProfileChanges(params: {
  pageNum?: number
  pageSize?: number
  status?: number
}) {
  return request.get('/admin/profile-change/page', { params })
}

/** 审批修改申请 PUT /api/admin/profile-change/{id}/review */
export function reviewProfileChange(id: number, pass: boolean, remark?: string) {
  return request.put(`/admin/profile-change/${id}/review`, null, { params: { pass, remark } })
}

// ===== 用户管理 (AdminUserController /api/admin/user) =====

/** 用户分页列表 GET /api/admin/user/page */
export function getUsers(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  role?: string
}) {
  return request.get('/admin/user/page', { params })
}

/** 创建教师账号 POST /api/admin/user */
export function createTeacher(data: {
  username: string
  realName?: string
  college?: string
  phone?: string
  email?: string
}) {
  return request.post('/admin/user', data)
}
