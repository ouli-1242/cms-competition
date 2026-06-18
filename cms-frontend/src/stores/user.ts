/**
 * 用户状态管理（Pinia）
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/auth'

const TOKEN_KEY = 'cms_token'
const USER_KEY = 'cms_user'

export type Role = 'STUDENT' | 'TEACHER' | 'ADMIN' | 'VISITOR'

export interface UserInfo {
  id: number
  username: string
  role: Role
  realName?: string
  nickname?: string
  studentNo?: string
  school?: string
  college?: string
  phone?: string
  email?: string
  avatar?: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<UserInfo | null>(null)
  try {
    const stored = localStorage.getItem(USER_KEY)
    if (stored) user.value = JSON.parse(stored) as UserInfo
  } catch {
    user.value = null
  }

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role || 'VISITOR')
  const isStudent = computed(() => role.value === 'STUDENT')
  const isTeacher = computed(() => role.value === 'TEACHER')
  const isAdmin = computed(() => role.value === 'ADMIN')

  async function login(username: string, password: string) {
    const res: any = await loginApi({ username, password })
    token.value = res.token
    const u = res.user || res
    user.value = {
      id: u.id,
      username: u.username || username,
      role: u.role,
      realName: u.realName || u.nickname,
      nickname: u.nickname,
      college: u.college,
      phone: u.phone,
      email: u.email,
      avatar: u.avatar
    }
    localStorage.setItem(TOKEN_KEY, res.token)
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    return user.value
  }

  async function fetchInfo() {
    if (!token.value) return null
    const info = (await getUserInfo()) as any
    user.value = info
    localStorage.setItem(USER_KEY, JSON.stringify(info))
    return info
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return {
    token,
    user,
    isLoggedIn,
    role,
    isStudent,
    isTeacher,
    isAdmin,
    login,
    fetchInfo,
    logout
  }
})
