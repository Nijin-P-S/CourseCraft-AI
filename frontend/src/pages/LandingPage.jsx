import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

const features = [
  {
    title: 'AI course blueprints',
    copy: 'Generate structured modules, lesson plans, objectives, and tags from a single topic prompt.',
  },
  {
    title: 'Rich lesson studio',
    copy: 'Turn each outline item into headings, explanations, quizzes, video search prompts, and notes.',
  },
  {
    title: 'Progress tracking',
    copy: 'Keep learners moving with completed lessons, assessment scores, and last-accessed context.',
  },
]

const previewLessons = [
  'Foundations',
  'Core concepts',
  'Hands-on practice',
  'Review quiz',
]

function LandingPage() {
  const { isAuthenticated, login, provider } = useAuth()
  const navigate = useNavigate()

  async function handleLogin() {
    await login()
    if (provider !== 'auth0') {
      navigate('/dashboard')
    }
  }

  return (
    <main className="marketing-page">
      <nav className="marketing-nav" aria-label="Marketing navigation">
        <Link className="marketing-brand" to="/">
          <span>CC</span>
          CourseCraft AI
        </Link>
        <div className="marketing-links">
          <a href="#features">Features</a>
          <a href="#workflow">Workflow</a>
          <a href="#pricing">MVP</a>
        </div>
        <div className="marketing-actions">
          {isAuthenticated ? (
            <Link className="button" to="/dashboard">
              Open dashboard
            </Link>
          ) : (
            <>
              <button className="text-button" type="button" onClick={handleLogin}>
                Log in
              </button>
              <button className="button" type="button" onClick={handleLogin}>
                {provider === 'auth0' ? 'Log in with Auth0' : 'Start free'}
              </button>
            </>
          )}
        </div>
      </nav>

      <section className="marketing-hero">
        <div className="hero-content">
          <p className="eyebrow">AI-powered learning paths</p>
          <h1>Build complete courses from an idea in minutes.</h1>
          <p>
            CourseCraft AI helps educators, creators, and self-learners turn any
            topic into a guided curriculum with rich lessons, quizzes, video
            prompts, and progress tracking.
          </p>
          <div className="hero-form">
            <input
              aria-label="Email address"
              placeholder="Personal email"
              type="email"
            />
            <button className="button" type="button" onClick={handleLogin}>
              Enroll for free
            </button>
          </div>
          <small>
            No credit card required. Use demo mode locally or Auth0 when your
            tenant variables are configured.
          </small>
        </div>

        <div className="product-preview" aria-label="CourseCraft AI product preview">
          <div className="preview-topbar">
            <strong>CourseCraft AI</strong>
            <span>Live workspace</span>
          </div>
          <div className="preview-grid">
            <aside className="preview-sidebar">
              <span className="active">Dashboard</span>
              <span>My Courses</span>
              <span>Progress</span>
              <span>Lessons</span>
            </aside>
            <section className="preview-main">
              <div className="preview-search">Search courses, topics, lessons</div>
              <div className="metric-row">
                <MetricCard label="Generated courses" value="24" />
                <MetricCard label="Completed lessons" value="128" />
                <MetricCard label="Avg. quiz score" value="87%" />
              </div>
              <div className="preview-course">
                <div>
                  <p className="eyebrow">Current course</p>
                  <h3>Full-stack AI App Development</h3>
                  <p>65% complete</p>
                </div>
                <div className="progress-bar">
                  <span />
                </div>
              </div>
              <div className="lesson-strip">
                {previewLessons.map((lesson) => (
                  <span key={lesson}>{lesson}</span>
                ))}
              </div>
            </section>
          </div>
        </div>
      </section>

      <section className="feature-section" id="features">
        <div className="section-kicker">
          <p className="eyebrow">What learners get</p>
          <h2>Everything needed to move from prompt to progress.</h2>
        </div>
        <div className="feature-grid">
          {features.map((feature) => (
            <article className="feature-card" key={feature.title}>
              <h3>{feature.title}</h3>
              <p>{feature.copy}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="workflow-section" id="workflow">
        <div>
          <p className="eyebrow">Authenticated workspace</p>
          <h2>After login, learners get the full product experience.</h2>
        </div>
        <div className="workflow-grid">
          <WorkflowStep count="01" title="Generate a course" />
          <WorkflowStep count="02" title="Open modules and lessons" />
          <WorkflowStep count="03" title="Generate rich lesson content" />
          <WorkflowStep count="04" title="Track completion and scores" />
        </div>
      </section>

      <section className="cta-section" id="pricing">
        <p className="eyebrow">MVP access</p>
        <h2>Ready to craft your first course?</h2>
        <button className="button" type="button" onClick={handleLogin}>
          Enter the dashboard
        </button>
      </section>
    </main>
  )
}

function MetricCard({ label, value }) {
  return (
    <article className="metric-card">
      <strong>{value}</strong>
      <span>{label}</span>
    </article>
  )
}

function WorkflowStep({ count, title }) {
  return (
    <article className="workflow-card">
      <span>{count}</span>
      <strong>{title}</strong>
    </article>
  )
}

export default LandingPage
