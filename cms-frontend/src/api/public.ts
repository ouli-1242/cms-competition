/**
 * 公共 API（无需登录）
 * 对应 PublicController（/api）
 */
import request from './request'

export interface Banner {
  id: number
  title: string
  imageUrl: string
  linkUrl?: string
  sort: number
  status: number
  startTime?: string
  endTime?: string
}

export interface Competition {
  id: number
  title: string
  category: string
  cover: string
  description: string
  type: number
  maxTeamSize: number
  minTeamSize?: number
  registerStart: string
  registerEnd: string
  compStart: string
  compEnd: string
  status: number
  teacherId?: number
  registrationCount?: number
  createTime?: string
}

export interface PageResult<T> {
  total: number
  records: T[]
  current: number
  size: number
  pages: number
}

/** 首页轮播图 GET /api/banner/public/list */
export function getActiveBanners() {
  return request.get<Banner[]>('/banner/public/list')
}

/** 首页热门推荐 GET /api/hot-recommend/public/list */
export function getActiveHot() {
  return request.get<Competition[]>('/hot-recommend/public/list')
}

/** 公开竞赛分页 GET /api/competition/public/page */
export function getCompetitions(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  category?: string
  registrationStatus?: number
  type?: number
  excludeEnded?: boolean
}) {
  return request.get<PageResult<Competition>>('/competition/public/page', { params })
}

/** 公开竞赛详情 GET /api/competition/public/{id} */
export function getCompetitionDetail(id: number) {
  return request.get<Competition>(`/competition/public/${id}`)
}
