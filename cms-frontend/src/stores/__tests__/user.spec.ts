/**
 * 用户状态管理 (Pinia Store) 单元测试
 * 测试登录/登出/token 持久化/角色计算等核心逻辑
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: vi.fn((key: string) => store[key] ?? null),
    setItem: vi.fn((key: string, value: string) => { store[key] = value }),
    removeItem: vi.fn((key: string) => { delete store[key] }),
    clear: vi.fn(() => { store = {} }),
    get length() { return Object.keys(store).length },
    key: vi.fn((index: number) => Object.keys(store)[index] ?? null)
  }
})()

Object.defineProperty(globalThis, 'localStorage', { value: localStorageMock })

// Mock auth API
vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  getUserInfo: vi.fn()
}))

import { useUserStore } from '../user'
import { login as loginApi } from '@/api/auth'

describe('useUserStore', () => {
  beforeEach(() => {
    // 每次测试前重置
    localStorageMock.clear()
    vi.clearAllMocks()
    // 创建新的 Pinia 实例
    setActivePinia(createPinia())
  })

  describe('初始状态', () => {
    it('未登录时 isLoggedIn 应为 false', () => {
      const store = useUserStore()
      expect(store.isLoggedIn).toBe(false)
    })

    it('未登录时 role 应为 VISITOR', () => {
      const store = useUserStore()
      expect(store.role).toBe('VISITOR')
    })

    it('user 初始值应为 null', () => {
      const store = useUserStore()
      expect(store.user).toBeNull()
    })

    it('token 初始值应为空字符串', () => {
      const store = useUserStore()
      expect(store.token).toBe('')
    })
  })

  describe('角色计算属性', () => {
    it('role=STUDENT 时 isStudent 为 true', () => {
      const store = useUserStore()
      store.user = {
        id: 1,
        username: '2024001',
        role: 'STUDENT',
        realName: '张三'
      }
      store.token = 'fake-token'
      expect(store.isStudent).toBe(true)
      expect(store.isTeacher).toBe(false)
      expect(store.isAdmin).toBe(false)
    })

    it('role=TEACHER 时 isTeacher 为 true', () => {
      const store = useUserStore()
      store.user = {
        id: 2,
        username: 'T001',
        role: 'TEACHER',
        realName: '李老师'
      }
      store.token = 'fake-token'
      expect(store.isTeacher).toBe(true)
      expect(store.isStudent).toBe(false)
    })

    it('role=ADMIN 时 isAdmin 为 true', () => {
      const store = useUserStore()
      store.user = {
        id: 3,
        username: 'admin',
        role: 'ADMIN',
        realName: '管理员'
      }
      store.token = 'fake-token'
      expect(store.isAdmin).toBe(true)
    })
  })

  describe('login 方法', () => {
    it('登录成功后应设置 token 和 user', async () => {
      const mockResponse = {
        token: 'jwt-token-123',
        userId: 1,
        username: '2024001',
        role: 'STUDENT',
        realName: '张三'
      }
      vi.mocked(loginApi).mockResolvedValue(mockResponse)

      const store = useUserStore()
      const user = await store.login('2024001', 'password123')

      expect(store.token).toBe('jwt-token-123')
      expect(store.isLoggedIn).toBe(true)
      expect(store.role).toBe('STUDENT')
      expect(user?.username).toBe('2024001')
      expect(user?.realName).toBe('张三')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('cms_token', 'jwt-token-123')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('cms_user', expect.any(String))
    })

    it('登录后应持久化用户信息到 localStorage', async () => {
      const mockResponse = {
        token: 'jwt-token-456',
        userId: 2,
        username: 'T001',
        role: 'TEACHER',
        realName: '李老师'
      }
      vi.mocked(loginApi).mockResolvedValue(mockResponse)

      const store = useUserStore()
      await store.login('T001', 'pass456')

      expect(localStorageMock.setItem).toHaveBeenCalledWith('cms_token', 'jwt-token-456')
      const userArg = (localStorageMock.setItem as any).mock.calls.find(
        (call: any[]) => call[0] === 'cms_user'
      )
      expect(userArg).toBeDefined()
      const savedUser = JSON.parse(userArg[1])
      expect(savedUser.role).toBe('TEACHER')
      expect(savedUser.realName).toBe('李老师')
    })
  })

  describe('logout 方法', () => {
    it('登出后应清除 token 和 user', () => {
      const store = useUserStore()
      store.token = 'some-token'
      store.user = {
        id: 1,
        username: '2024001',
        role: 'STUDENT'
      }

      store.logout()

      expect(store.token).toBe('')
      expect(store.user).toBeNull()
      expect(store.isLoggedIn).toBe(false)
      expect(store.role).toBe('VISITOR')
    })

    it('登出后应清除 localStorage', () => {
      const store = useUserStore()
      store.token = 'some-token'
      store.user = { id: 1, username: 'test', role: 'STUDENT' }

      store.logout()

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('cms_token')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('cms_user')
    })
  })

  describe('localStorage 持久化', () => {
    it('应从 localStorage 恢复 token', () => {
      localStorageMock.getItem.mockReturnValueOnce('persisted-token')
      localStorageMock.getItem.mockReturnValueOnce(null)

      const store = useUserStore()

      expect(store.token).toBe('persisted-token')
    })

    it('应从 localStorage 恢复 user', () => {
      const savedUser = {
        id: 5,
        username: 'testuser',
        role: 'STUDENT',
        realName: '测试用户'
      }
      localStorageMock.getItem.mockReturnValueOnce('')
      localStorageMock.getItem.mockReturnValueOnce(JSON.stringify(savedUser))

      const store = useUserStore()

      expect(store.user).toEqual(savedUser)
    })

    it('localStorage 存储的用户数据损坏时应返回 null', () => {
      localStorageMock.getItem.mockReturnValueOnce('')
      localStorageMock.getItem.mockReturnValueOnce('{invalid-json')

      const store = useUserStore()

      expect(store.user).toBeNull()
    })
  })
})
