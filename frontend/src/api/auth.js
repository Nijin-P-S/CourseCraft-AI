import apiClient from './client'

export async function getCurrentAuthUser() {
  const { data } = await apiClient.get('/api/auth/me')
  return data
}
