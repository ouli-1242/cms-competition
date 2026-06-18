/**
 * 通知消息 API
 * 对应 NotificationController（/api/notification）
 */
import request from './request'

/** 消息列表 GET /api/notification/list */
export function getNotifications(params: {
  pageNum?: number
  pageSize?: number
  isRead?: number
}) {
  return request.get('/notification/list', { params })
}

/** 未读消息数 GET /api/notification/unread-count */
export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

/** 标记已读 PUT /api/notification/{id}/read */
export function markNotificationRead(id: number) {
  return request.put(`/notification/${id}/read`)
}

/** 全部已读 PUT /api/notification/read-all */
export function markAllNotificationsRead() {
  return request.put('/notification/read-all')
}
