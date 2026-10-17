import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getCourses } from '../api/courses'
import { getApiErrorMessage } from '../api/client'

function CoursesPage() {
  const [courses, setCourses] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let ignore = false

    async function loadCourses() {
      setIsLoading(true)
      setError('')

      try {
        const data = await getCourses()
        if (!ignore) {
          setCourses(data)
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

    loadCourses()

    return () => {
      ignore = true
    }
  }, [])

  if (isLoading) {
    return <section className="state-card">Loading courses...</section>
  }

  if (error) {
    return <section className="state-card error-message">{error}</section>
  }

  return (
    <section className="page-stack courses-page">
      <div className="section-header">
        <div>
          <p className="eyebrow">Your library</p>
          <h1>Courses</h1>
          <p className="muted">
            Continue learning paths generated for your goals and track each
            lesson from outline to completion.
          </p>
        </div>
        <Link className="button" to="/dashboard">
          Generate new course
        </Link>
      </div>

      {courses.length === 0 ? (
        <div className="state-card">
          No courses yet. Generate your first course from the dashboard.
        </div>
      ) : (
        <div className="course-grid">
          {courses.map((course) => (
            <article className="course-card" key={course.id}>
              <div className="course-card-cover">
                <span>{course.tags?.[0] || 'AI course'}</span>
              </div>
              <div className="course-card-body">
                <h2>{course.title}</h2>
                {course.description && <p>{course.description}</p>}
                <div className="course-card-meta">
                  <span>{course.modules?.length || 0} modules</span>
                  <span>
                    {(course.modules || []).reduce(
                      (total, module) => total + (module.lessons?.length || 0),
                      0,
                    )}{' '}
                    lessons
                  </span>
                </div>
                <Link className="course-card-action" to={`/courses/${course.id}`}>
                  Open course
                </Link>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}

export default CoursesPage
