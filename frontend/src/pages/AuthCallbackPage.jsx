import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

function AuthCallbackPage() {
  const { isAuthenticated, isLoading, login } = useAuth()

  if (isLoading) {
    return (
      <section className="auth-required">
        <p className="eyebrow">Signing you in</p>
        <h1>Preparing your workspace.</h1>
        <p>Auth0 is confirming your session before opening the dashboard.</p>
      </section>
    )
  }

  if (isAuthenticated) {
    return <Navigate replace to="/dashboard" />
  }

  return (
    <section className="auth-required">
      <p className="eyebrow">Sign-in incomplete</p>
      <h1>We could not finish the Auth0 redirect.</h1>
      <p>Start sign-in again to request a fresh Auth0 session.</p>
      <button className="button" type="button" onClick={login}>
        Try login again
      </button>
    </section>
  )
}

export default AuthCallbackPage
