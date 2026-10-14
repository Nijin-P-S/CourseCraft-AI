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
    <section className="page-stack">
      <div className="section-header">
        <div>
          <p className="eyebrow">Your library</p>
          <h1>Courses</h1>
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
              <h2>{course.title}</h2>
              {course.description && <p>{course.description}</p>}
              <Link className="text-link" to={`/courses/${course.id}`}>
                View course
              </Link>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}

export default CoursesPage
