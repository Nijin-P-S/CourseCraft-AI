import axios from 'axios'

let accessTokenGetter = null

export function setAccessTokenGetter(getter) {
  accessTokenGetter = getter
}

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use(async (config) => {
  if (!accessTokenGetter) {
    return config
  }

  const token = await accessTokenGetter()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

export function getApiErrorMessage(error) {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    'Something went wrong. Please try again.'
  )
}

export default apiClient
