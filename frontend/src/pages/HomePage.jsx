import { useEffect, useState } from 'react'
import CourseOverview from '../components/CourseOverview'
import { generateCourse } from '../api/courses'
import { getApiErrorMessage } from '../api/client'
import { useAuth } from '../context/useAuth'

const promptExamples = [
  'Spring Boot microservices',
  'Intro to React Hooks',
  'System design for beginners',
  'Python for data analysis',
  'Cloud deployment with Docker',
]

const dashboardInsights = [
  {
    title: 'AI outline',
    description: 'Turn a topic into a structured course with modules and lesson goals.',
  },
  {
    title: 'Lesson studio',
    description: 'Generate rich lesson content, quizzes, notes, and recommended videos.',
  },
  {
    title: 'Progress loop',
    description: 'Track completion and quiz scores as every learning path grows.',
  },
]

function HomePage() {
  const { user } = useAuth()
  const [topic, setTopic] = useState('')
  const [course, setCourse] = useState(null)
  const [activePromptIndex, setActivePromptIndex] = useState(0)
  const [isGenerating, setIsGenerating] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const intervalId = window.setInterval(() => {
      setActivePromptIndex((currentIndex) =>
        (currentIndex + 1) % promptExamples.length,
      )
    }, 2800)

    return () => {
      window.clearInterval(intervalId)
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
        <div className="dashboard-hero-card">
          <p className="eyebrow">Welcome back, {user?.name || 'learner'}</p>
          <h1>Create your next learning path.</h1>
          <p className="hero-copy">
            Generate a course outline, open each module, create rich lesson
            content, and track progress from this workspace.
          </p>
          <div className="hero-highlight-row">
            <span>Personalized modules</span>
            <span>Interactive checks</span>
            <span>Video recommendations</span>
          </div>
        </div>

        <div className="dashboard-metrics" aria-label="Learning summary">
          <article>
            <strong>Prompt to path</strong>
            <span>AI turns your topic into a structured course outline.</span>
          </article>
          <article>
            <strong>Adaptive modules</strong>
            <span>Course structure adjusts to the topic and learning depth.</span>
          </article>
          <article>
            <strong>Progress-ready</strong>
            <span>Lessons, checks, and completion tracking stay connected.</span>
          </article>
        </div>

        <aside className="dashboard-insight-card">
          <p className="eyebrow">Today&apos;s workspace</p>
          <h2>Build a focused course in minutes.</h2>
          <div className="insight-list">
            {dashboardInsights.map((insight) => (
              <article key={insight.title}>
                <strong>{insight.title}</strong>
                <span>{insight.description}</span>
              </article>
            ))}
          </div>
        </aside>

        <form className="prompt-card" onSubmit={handleSubmit}>
          <div className="prompt-card-header">
            <div>
              <p className="eyebrow">Course generator</p>
              <label htmlFor="topic">What do you want to learn?</label>
            </div>
            <span className="prompt-example-pill" key={promptExamples[activePromptIndex]}>
              Try: {promptExamples[activePromptIndex]}
            </span>
          </div>
          <div className="prompt-row">
            <input
              id="topic"
              type="text"
              placeholder="Tell CourseCraft what you want to master..."
              value={topic}
              onChange={(event) => setTopic(event.target.value)}
              disabled={isGenerating}
            />
            <button type="submit" disabled={isGenerating}>
              {isGenerating ? 'Generating...' : 'Generate Course'}
            </button>
          </div>
          <div className="prompt-suggestion-row" aria-label="Prompt suggestions">
            {promptExamples.map((example) => (
              <button
                type="button"
                key={example}
                className={example === promptExamples[activePromptIndex] ? 'active' : ''}
                onClick={() => setTopic(example)}
                disabled={isGenerating}
              >
                {example}
              </button>
            ))}
          </div>
          {error && <p className="error-message">{error}</p>}
        </form>
      </section>

      {isGenerating && (
        <section className="state-card">
          Building your course outline. This can take a moment while AI
          prepares the modules and lessons.
        </section>
      )}

      {course && <CourseOverview course={course} />}
    </div>
  )
}

export default HomePage
