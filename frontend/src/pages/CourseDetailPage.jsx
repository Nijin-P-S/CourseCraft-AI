import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { getCourse, getCourseProgress } from '../api/courses'
import CourseOverview from '../components/CourseOverview'

function CourseDetailPage() {
  const { courseId } = useParams()
  const [course, setCourse] = useState(null)
  const [progress, setProgress] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let ignore = false

    async function loadCourse() {
      setIsLoading(true)
      setError('')

      try {
        const [courseData, progressData] = await Promise.all([
          getCourse(courseId),
          getCourseProgress(courseId).catch(() => null),
        ])

        if (!ignore) {
          setCourse(courseData)
          setProgress(progressData)
        }
      } catch (requestError) {
        if (!ignore) {
          setError(getApiErrorMessage(requestError))
        }
      } finally {
        if (!ignore) {
          setIsLoading(false)
        }
      }
    }

    loadCourse()

    return () => {
      ignore = true
    }
  }, [courseId])

  if (isLoading) {
    return <section className="state-card">Loading course...</section>
  }

  if (error) {
    return <section className="state-card error-message">{error}</section>
  }

  if (!course) {
    return <section className="state-card">Course not found.</section>
  }

  const totalLessons = course.modules?.reduce(
    (total, module) => total + (module.lessons?.length || 0),
    0,
  )
  const completedLessons = progress?.completedLessonIds?.length || 0

  return (
    <section className="page-stack">
      <Link className="text-link" to="/courses">
        Back to courses
      </Link>

      <CourseOverview course={course} />

      <aside className="state-card">
        <strong>Progress:</strong>{' '}
        {progress
          ? `${completedLessons} of ${totalLessons} lessons complete`
          : 'Progress will appear after lesson activity.'}
      </aside>
    </section>
  )
}

export default CourseDetailPage
