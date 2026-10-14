import { useEffect, useState } from 'react'
import CourseOverview from '../components/CourseOverview'
import { getCurrentAuthUser } from '../api/auth'
import { generateCourse } from '../api/courses'
import { getApiErrorMessage } from '../api/client'
import { useAuth } from '../context/useAuth'

function HomePage() {
  const { user } = useAuth()
  const [topic, setTopic] = useState('')
  const [course, setCourse] = useState(null)
  const [backendAuth, setBackendAuth] = useState(null)
  const [backendAuthError, setBackendAuthError] = useState('')
  const [isGenerating, setIsGenerating] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let ignore = false

    async function loadBackendAuth() {
      try {
        const authUser = await getCurrentAuthUser()
        if (!ignore) {
          setBackendAuth(authUser)
          setBackendAuthError('')
        }
      } catch (requestError) {
        if (!ignore) {
          setBackendAuth(null)
          setBackendAuthError(getApiErrorMessage(requestError))
        }
      }
    }

    loadBackendAuth()

    return () => {
      ignore = true
    }
  }, [])

  async function handleSubmit(event) {
    event.preventDefault()
    const trimmedTopic = topic.trim()
    if (!trimmedTopic) {
      setError('Enter a topic to generate a course.')
      return
    }

    setIsGenerating(true)
    setError('')

    try {
      const generatedCourse = await generateCourse(trimmedTopic)
      setCourse(generatedCourse)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    } finally {
      setIsGenerating(false)
    }
  }

  return (
    <div className="page-stack dashboard-page">
      <section className="dashboard-hero" id="generate">
        <div>
          <p className="eyebrow">Welcome back, {user?.name || 'learner'}</p>
          <h1>Create your next learning path.</h1>
          <p className="hero-copy">
            Generate a course outline, open each module, create rich lesson
            content, and track progress from this workspace.
          </p>
        </div>

        <div className="dashboard-metrics" aria-label="Learning summary">
          <article>
            <strong>1 prompt</strong>
            <span>to course outline</span>
          </article>
          <article>
            <strong>4 modules</strong>
            <span>typical MVP path</span>
          </article>
          <article>
            <strong>Live</strong>
            <span>progress tracking</span>
          </article>
        </div>

        <aside className="auth-status-card">
          <div>
            <p className="eyebrow">Backend auth check</p>
            {backendAuth ? (
              <h2>Connected as {backendAuth.userId}</h2>
            ) : (
              <h2>Backend session pending</h2>
            )}
          </div>
          {backendAuth ? (
            <p>
              API accepted the current session using{' '}
              <strong>{backendAuth.authMode}</strong> mode.
            </p>
          ) : (
            <p>{backendAuthError || 'Checking backend auth status...'}</p>
          )}
        </aside>

        <form className="prompt-card" onSubmit={handleSubmit}>
          <label htmlFor="topic">What do you want to learn?</label>
          <div className="prompt-row">
            <input
              id="topic"
              type="text"
              placeholder="Intro to React Hooks"
              value={topic}
              onChange={(event) => setTopic(event.target.value)}
              disabled={isGenerating}
            />
            <button type="submit" disabled={isGenerating}>
              {isGenerating ? 'Generating...' : 'Generate Course'}
            </button>
          </div>
          {error && <p className="error-message">{error}</p>}
        </form>
      </section>

      {isGenerating && (
        <section className="state-card">
          Building your course outline. This can take a moment when Gemini is
          enabled.
        </section>
      )}

      {course && <CourseOverview course={course} />}
    </div>
  )
}

export default HomePage
