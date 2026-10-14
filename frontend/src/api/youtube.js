import apiClient from './client'

const videoCache = new Map()

export async function searchYouTubeVideo(query) {
  const normalizedQuery = query.trim()
  if (!normalizedQuery) {
    return null
  }

  if (videoCache.has(normalizedQuery)) {
    return videoCache.get(normalizedQuery)
  }

  const { data } = await apiClient.get('/api/youtube', {
    params: { query: normalizedQuery },
  })

  videoCache.set(normalizedQuery, data)
  return data
}
