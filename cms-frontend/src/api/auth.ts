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
  realName: string
  studentNo?: string
  college?: string
  phone?: string
  email?: string
  role?: 'STUDENT'
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

// 检查用户名/学号是否已注册
export function checkUsernameExists(username: string) {
  return request.get<boolean>('/auth/check-username', { params: { username } })
}

// 检查邮箱是否已注册
export function checkEmailExists(email: string) {
  return request.get<boolean>('/auth/check-email', { params: { email } })
}

// 更新当前用户资料
export function updateUserInfo(data: {
  realName?: string
  college?: string
  phone?: string
  avatar?: string
}) {
  return request.put('/user/profile', data)
}

// 修改密码
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/user/password', data)
}
