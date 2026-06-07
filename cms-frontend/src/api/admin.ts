/**
 * 管理员后台 API
 */
import request from './request'

// ===== 竞赛管理 =====
export function getCompetitions(params: { page: number; size: number; status?: number; keyword?: string }) {
  return request.get('/admin/competitions', { params })
}

export function getCompetitionById(id: number) {
  return request.get(`/admin/competitions/${id}`)
}

export function createCompetition(data: any) {
  return request.post('/admin/competitions', data)
}

export function updateCompetition(id: number, data: any) {
  return request.put(`/admin/competitions/${id}`, data)
}

export function deleteCompetition(id: number) {
  return request.delete(`/admin/competitions/${id}`)
}

export function toggleCompetitionStatus(id: number, status: number) {
  return request.put(`/admin/competitions/${id}/status`, { status })
}

// ===== 报名管理 =====
export function getRegistrations(params: any) {
  return request.get('/admin/registrations', { params })
}

export function auditRegistration(id: number, approve: boolean, remark?: string) {
  return request.put(`/admin/registrations/${id}/audit`, { approve, remark })
}

// ===== 轮播图管理 =====
export function getBanners() {
  return request.get('/admin/banners')
}

export function createBanner(data: any) {
  return request.post('/admin/banners', data)
}

export function updateBanner(id: number, data: any) {
  return request.put(`/admin/banners/${id}`, data)
}

export function deleteBanner(id: number) {
  return request.delete(`/admin/banners/${id}`)
}

// ===== 数据统计 =====
export function getAdminStats() {
  return request.get('/admin/stats')
}
