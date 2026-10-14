import { Link } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

function RequireAuth({ children }) {
  const { isAuthenticated, login } = useAuth()

  if (isAuthenticated) {
    return children
  }

  return (
    <section className="auth-required">
      <p className="eyebrow">Members only</p>
      <h1>Log in to access your learning workspace.</h1>
      <p>
        Course generation, lesson content, progress tracking, and your course
        library are available after sign in.
      </p>
      <div className="action-row">
        <button className="button" type="button" onClick={login}>
          Log in with demo account
        </button>
        <Link className="button secondary" to="/">
          Back to overview
        </Link>
      </div>
    </section>
  )
}

export default RequireAuth
