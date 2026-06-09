import { reactive } from 'vue'
import { getCurrentUser } from '../api/auth'

export const session = reactive({
  user: null,
  loading: false
})

export async function loadSession() {
  session.loading = true
  try {
    const response = await getCurrentUser()
    session.user = response.data || null
  } finally {
    session.loading = false
  }
}

export function setSessionUser(user) {
  session.user = user
}

export function clearSession() {
  session.user = null
}
