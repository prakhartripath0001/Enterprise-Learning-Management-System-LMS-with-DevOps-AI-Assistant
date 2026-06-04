import { describe, it, expect, vi } from 'vitest'
import { parseJwt, formatDate, isStrongPassword } from '../utils/helpers'

describe('Utility Helper Functions', () => {
  describe('parseJwt', () => {
    it('returns null if token is falsy or invalid format', () => {
      expect(parseJwt('')).toBeNull()
      expect(parseJwt('invalidtoken')).toBeNull()
    })

    it('decodes and parses a valid JWT token', () => {
      // Mock window.atob
      const mockPayload = JSON.stringify({ userId: '123', roles: ['ROLE_STUDENT'] })
      const encodedPayload = window.btoa(mockPayload)
      const fakeToken = `header.${encodedPayload}.signature`

      const decoded = parseJwt(fakeToken)
      expect(decoded).not.toBeNull()
      expect(decoded.userId).toBe('123')
      expect(decoded.roles).toContain('ROLE_STUDENT')
    })
  })

  describe('formatDate', () => {
    it('returns empty string if date is missing or invalid', () => {
      expect(formatDate(null)).toBe('')
      expect(formatDate('invalid-date-string')).toBe('')
    })

    it('formats valid date string successfully', () => {
      const formatted = formatDate('2026-06-05T00:00:00Z')
      expect(formatted).toContain('2026')
    })
  })

  describe('isStrongPassword', () => {
    it('returns false for weak passwords', () => {
      expect(isStrongPassword('weak')).toBe(false)
      expect(isStrongPassword('12345678')).toBe(false)
      expect(isStrongPassword('Nocaps123')).toBe(false)
      expect(isStrongPassword('noSpecialChar1')).toBe(false)
    })

    it('returns true for passwords satisfying criteria', () => {
      expect(isStrongPassword('SecureP@ss123')).toBe(true)
    })
  })
})
