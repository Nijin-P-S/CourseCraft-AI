import { useState } from 'react'
import CourseOverview from '../components/CourseOverview'
import { generateCourse } from '../api/courses'
import { getApiErrorMessage } from '../api/client'

function HomePage() {
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
    <div className="page-stack">
      <section className="hero-section" id="generate">
        <p className="eyebrow">AI-powered course generator</p>
        <h1>Turn any topic into a structured learning path.</h1>
        <p className="hero-copy">
          CourseCraft AI generates course outlines, rich lessons, quizzes,
          video recommendations, and progress tracking from a single prompt.
        </p>

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
