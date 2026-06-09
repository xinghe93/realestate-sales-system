import http from './http'

export function getProfile() {
  return http.get('/users/me')
}

export function updateProfile(payload) {
  return http.put('/users/me', payload)
}

export function getUsers() {
  return http.get('/users')
}

export function createUser(payload) {
  return http.post('/users', payload)
}

export function updateUser(id, payload) {
  return http.put(`/users/${id}`, payload)
}

export function disableUser(id) {
  return http.delete(`/users/${id}`)
}
