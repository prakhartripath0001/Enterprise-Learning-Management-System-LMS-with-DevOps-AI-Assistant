/**
 * Helper utility functions for AetherLMS frontend
 */

/**
 * Decode JWT token payload (does not verify signature)
 * @param {string} token - JWT string
 * @returns {object|null} parsed payload or null
 */
export function parseJwt(token) {
  if (!token) return null
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      window.atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (e) {
    return null
  }
}

/**
 * Format timestamp into standard local date string
 * @param {string|Date} date - date input
 * @returns {string} formatted date string
 */
export function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  return d.toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

/**
 * Validate password strength
 * @param {string} password - password string
 * @returns {boolean} true if strong password, false otherwise
 */
export function isStrongPassword(password) {
  if (!password) return false
  // At least 8 chars, 1 uppercase letter, 1 lowercase letter, 1 number, 1 special character
  const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/
  return regex.test(password)
}
