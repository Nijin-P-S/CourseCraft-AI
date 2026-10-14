import { Link } from 'react-router-dom'

function CourseOverview({ course, compact = false }) {
  if (!course) {
    return null
  }

  const modules = [...(course.modules || [])].sort(
    (a, b) => a.orderIndex - b.orderIndex,
  )

  return (
    <article className="course-overview">
      <div className="section-header">
        <div>
          <p className="eyebrow">Generated course</p>
          <h2>{course.title}</h2>
        </div>
        <Link className="button secondary" to={`/courses/${course.id}`}>
          Open course
        </Link>
      </div>

      {course.description && <p className="muted">{course.description}</p>}

      {course.tags?.length > 0 && (
        <div className="tag-row" aria-label="Course tags">
          {course.tags.map((tag) => (
            <span className="tag" key={tag}>
              {tag}
            </span>
          ))}
        </div>
      )}

      {!compact && (
        <div className="module-list">
          {modules.map((module) => (
            <section className="module-card" key={module.id}>
              <h3>{module.title}</h3>
              {module.description && <p>{module.description}</p>}
              <ul className="lesson-list">
                {[...(module.lessons || [])]
                  .sort((a, b) => a.orderIndex - b.orderIndex)
                  .map((lesson) => (
                    <li key={lesson.id}>
                      <Link
                        to={`/courses/${course.id}/modules/${module.id}/lessons/${lesson.id}`}
                      >
                        {lesson.title}
                      </Link>
                      {lesson.contentGenerated && (
                        <span className="status-pill">Generated</span>
                      )}
                    </li>
                  ))}
              </ul>
            </section>
          ))}
        </div>
      )}
    </article>
  )
}

export default CourseOverview
