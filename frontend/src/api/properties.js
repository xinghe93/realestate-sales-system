import http from './http'

export function getProperties(params) {
  return http.get('/properties', { params })
}

export function getProperty(id) {
  return http.get(`/properties/${id}`)
}

export function createProperty(payload) {
  return http.post('/properties', payload)
}

export function updateProperty(id, payload) {
  return http.put(`/properties/${id}`, payload)
}

export function updatePropertyStatus(id, status) {
  return http.post(`/properties/${id}/audit`, { status })
}

export function deleteProperty(id) {
  return http.delete(`/properties/${id}`)
}
