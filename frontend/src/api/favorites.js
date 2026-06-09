import http from './http'

export function getFavorites() {
  return http.get('/favorites')
}

export function addFavorite(propertyId) {
  return http.post(`/favorites/${propertyId}`)
}

export function removeFavorite(propertyId) {
  return http.delete(`/favorites/${propertyId}`)
}
