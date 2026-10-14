import apiClient from './client'

const courseCache = new Map()
const lessonCache = new Map()
const progressCache = new Map()

const lessonCacheKey = (courseId, moduleId, lessonId) =>
  `${courseId}:${moduleId}:${lessonId}`

export async function generateCourse(topic) {
  const { data } = await apiClient.post('/api/courses/generate', { topic })
  courseCache.set(data.id, data)
  courseCache.delete('__list__')
  return data
}

export async function getCourses({ force = false } = {}) {
  if (!force && courseCache.has('__list__')) {
    return courseCache.get('__list__')
  }

  const { data } = await apiClient.get('/api/courses')
  courseCache.set('__list__', data)
  data.forEach((course) => courseCache.set(course.id, course))
  return data
}

export async function getCourse(courseId, { force = false } = {}) {
  if (!force && courseCache.has(courseId)) {
    return courseCache.get(courseId)
  }

  const { data } = await apiClient.get(`/api/courses/${courseId}`)
  courseCache.set(courseId, data)
  return data
}

export async function generateLessonContent(courseId, moduleId, lessonId) {
  const { data } = await apiClient.post(
    `/api/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}/generate`,
  )
  lessonCache.set(lessonCacheKey(courseId, moduleId, lessonId), data)
  courseCache.delete(courseId)
  courseCache.delete('__list__')
  return data
}

export async function getLessonContent(
  courseId,
  moduleId,
  lessonId,
  { force = false } = {},
) {
  const cacheKey = lessonCacheKey(courseId, moduleId, lessonId)
  if (!force && lessonCache.has(cacheKey)) {
    return lessonCache.get(cacheKey)
  }

  const { data } = await apiClient.get(
    `/api/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}`,
  )
  lessonCache.set(cacheKey, data)
  return data
}

export async function getCourseProgress(courseId, { force = false } = {}) {
  if (!force && progressCache.has(courseId)) {
    return progressCache.get(courseId)
  }

  const { data } = await apiClient.get(`/api/courses/${courseId}/progress`)
  progressCache.set(courseId, data)
  return data
}

export async function updateLessonProgress(courseId, lessonId, payload) {
  const { data } = await apiClient.put(
    `/api/courses/${courseId}/progress/lessons/${lessonId}`,
    payload,
  )
  progressCache.set(courseId, data)
  return data
}
