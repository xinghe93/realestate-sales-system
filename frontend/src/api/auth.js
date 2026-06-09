import http from './http'

export function login(payload) {
  return http.post('/auth/login', payload, { skipGlobalError: true })
}

export function register(payload) {
  return http.post('/auth/register', payload)
}

export function logout() {
  return http.post('/auth/logout')
}

export function getCurrentUser() {
  return http.get('/auth/me')
}

export function getCaptcha() {
  return http.get('/auth/captcha', { skipGlobalError: true })
}
