/**
 * 报名相关 API
 */
import request from './request'

// 获取我的报名列表
export function getMyRegistrations(params: { page: number; size: number; status?: number }) {
  return request.get('/student/registrations', { params })
}

// 取消报名
export function cancelRegistration(id: number) {
  return request.delete(`/student/registrations/${id}`)
}

// 个人赛一键报名
export function registerIndividual(data: { competitionId: number; remark?: string }) {
  return request.post('/student/registrations/individual', data)
}

// 检查是否已报名某竞赛
export function checkMyRegistration(competitionId: number) {
  return request.get<boolean>('/student/registrations/check', {
    params: { competitionId }
  })
}

// 获取报名详情
export function getRegistrationDetail(id: number) {
  return request.get(`/student/registrations/${id}`)
}
