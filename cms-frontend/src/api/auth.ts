/**
 * 认证相关 API
 */
import request from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userId: number
  username?: string
  role: 'STUDENT' | 'TEACHER' | 'ADMIN'
  realName?: string
}

export interface RegisterParams {
  username: string
  password: string
  nickname?: string
  realName?: string
  school?: string
  phone?: string
}

export function login(data: LoginParams) {
  return request.post<LoginResult, LoginResult>('/auth/login', data)
}

export function register(data: RegisterParams) {
  return request.post('/auth/register', data)
}

export function getUserInfo() {
  return request.get('/auth/info')
}

export function logout() {
  return request.post('/auth/logout')
}

// --- 以下接口后端不存在，暂时注释 ---
// export function checkUsernameExists(username: string) {
//   return request.get<boolean>('/auth/check-username', { params: { username } })
// }

// export function checkEmailExists(email: string) {
//   return request.get<boolean>('/auth/check-email', { params: { email } })
// }

// ===== 学生资料（对应 StudentProfileController /api/student/profile）=====

export function updateUserInfo(data: {
  nickname?: string
  realName?: string
  school?: string
  phone?: string
  email?: string
  avatar?: string
}) {
  return request.put('/student/profile', data)
}

export function changePassword(oldPwd: string, newPwd: string) {
  return request.put('/student/profile/password', { oldPwd, newPwd })
}

// ===== 资料修改申请（需管理员审批）=====

/** 提交修改申请 POST /api/student/profile-change */
export function submitProfileChange(fieldName: string, newValue: string) {
  return request.post('/student/profile-change', { fieldName, newValue })
}

/** 我的修改申请 GET /api/student/profile-change/my */
export function getMyProfileChanges() {
  return request.get('/student/profile-change/my')
}
