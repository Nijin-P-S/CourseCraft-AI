import { useState } from 'react'
import CourseOverview from '../components/CourseOverview'
import { generateCourse } from '../api/courses'
import { getApiErrorMessage } from '../api/client'
import { useAuth } from '../context/useAuth'

function HomePage() {
  const { user } = useAuth()
  const [topic, setTopic] = useState('')
  const [course, setCourse] = useState(null)
  const [isGenerating, setIsGenerating] = useState(false)
  const [error, setError] = useState('')

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
