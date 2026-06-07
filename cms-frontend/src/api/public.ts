/**
 * 公共 API（无需登录）
 */
import request from './request'

export interface Banner {
  id: number
  title: string
  image: string
  link?: string
  sort: number
}

export interface Competition {
  id: number
  title: string
  category: string
  cover: string
  description: string
  organizer: string
  registerStart: string
  registerEnd: string
  contestTime: string
  maxTeamSize: number
  status: number
  teacherId?: number
  registrationCount?: number
}

export interface PageResult<T> {
  total: number
  list: T[]
  page: number
  size: number
}

export function getActiveBanners() {
  return request.get<Banner[]>('/public/banners/active')
}

export function getActiveHot() {
  return request.get('/public/hot/active')
}

export function getCompetitions(params: {
  page?: number
  size?: number
  keyword?: string
  category?: string
  status?: number
}) {
  return request.get<PageResult<Competition>>('/public/competitions', { params })
}

export function getCompetitionDetail(id: number) {
  return request.get<Competition>(`/public/competitions/${id}`)
}
