/**
 * 认证 API 类型与函数导出测试
 * 验证接口定义的正确性
 */
import { describe, it, expect } from 'vitest'
import {
  login,
  register,
  getUserInfo,
  logout,
  updateUserInfo,
  changePassword,
  submitProfileChange,
  getMyProfileChanges
} from '../auth'
import type { LoginParams, LoginResult, RegisterParams } from '../auth'

describe('auth API 模块', () => {
  describe('函数导出', () => {
    const expectedFunctions = [
      'login', 'register', 'getUserInfo', 'logout',
      'updateUserInfo', 'changePassword',
      'submitProfileChange', 'getMyProfileChanges'
    ]

    expectedFunctions.forEach((fn) => {
      it(`${fn} 应为可导出的函数`, () => {
        const functions: Record<string, unknown> = {
          login, register, getUserInfo, logout,
          updateUserInfo, changePassword,
          submitProfileChange, getMyProfileChanges
        }
        expect(typeof functions[fn]).toBe('function')
      })
    })
  })

  describe('LoginParams 接口', () => {
    it('应包含 username 和 password 字段', () => {
      const params: LoginParams = {
        username: 'testuser',
        password: '123456'
      }
      expect(params.username).toBe('testuser')
      expect(params.password).toBe('123456')
    })
  })

  describe('LoginResult 接口', () => {
    it('应包含核心返回字段', () => {
      const result: LoginResult = {
        token: 'jwt-token',
        userId: 1,
        role: 'STUDENT',
        realName: '张三'
      }
      expect(result.token).toBeDefined()
      expect(result.userId).toBe(1)
      expect(result.role).toBe('STUDENT')
    })

    it('应支持三种角色类型', () => {
      const roles: LoginResult['role'][] = ['STUDENT', 'TEACHER', 'ADMIN']
      roles.forEach((role) => {
        const result: LoginResult = { token: 'x', userId: 1, role }
        expect(result.role).toBe(role)
      })
    })
  })

  describe('RegisterParams 接口', () => {
    it('username 和 password 为必填，其余字段可选', () => {
      const minimal: RegisterParams = {
        username: 'newuser',
        password: 'pass123'
      }
      expect(minimal.username).toBe('newuser')
      expect(minimal.nickname).toBeUndefined() // 可选字段

      const full: RegisterParams = {
        username: 'newuser',
        password: 'pass123',
        nickname: '小明',
        realName: '王小明',
        school: '某某大学',
        college: '计算机学院',
        phone: '13800138000',
        email: 'test@example.com'
      }
      expect(full.college).toBe('计算机学院')
    })
  })
})
