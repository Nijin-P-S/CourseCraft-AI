import { Auth0Provider, useAuth0 } from '@auth0/auth0-react'
import { useEffect, useMemo, useState } from 'react'
import { setAccessTokenGetter } from '../api/client'
import AuthContextValue from './AuthContextValue'

const STORAGE_KEY = 'coursecraft.auth.user'
const auth0Domain = import.meta.env.VITE_AUTH0_DOMAIN
const auth0ClientId = import.meta.env.VITE_AUTH0_CLIENT_ID
const auth0Audience = import.meta.env.VITE_AUTH0_AUDIENCE
const auth0Enabled = Boolean(auth0Domain && auth0ClientId)

const demoUser = {
  name: 'Nijin',
  email: 'nijin@example.com',
  plan: 'MVP Learner',
}

export function AuthProvider({ children }) {
  if (auth0Enabled) {
    return (
      <Auth0Provider
        domain={auth0Domain}
        clientId={auth0ClientId}
        authorizationParams={{
          audience: auth0Audience || undefined,
          redirect_uri: `${window.location.origin}/dashboard`,
        }}
      >
        <Auth0AuthProvider>{children}</Auth0AuthProvider>
      </Auth0Provider>
    )
  }

  return <LocalAuthProvider>{children}</LocalAuthProvider>
}

function LocalAuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const storedUser = window.localStorage.getItem(STORAGE_KEY)
    try {
      return storedUser ? JSON.parse(storedUser) : null
    } catch {
      window.localStorage.removeItem(STORAGE_KEY)
      return null
    }
  })

  function login() {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(demoUser))
    setUser(demoUser)
  }

  function logout() {
    window.localStorage.removeItem(STORAGE_KEY)
    setUser(null)
  }

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isLoading: false,
      provider: 'local',
      login,
      logout,
    }),
    [user],
  )

  return (
    <AuthContextValue.Provider value={value}>
      {children}
    </AuthContextValue.Provider>
  )
}

function Auth0AuthProvider({ children }) {
  const {
    getAccessTokenSilently,
    isAuthenticated,
    isLoading,
    loginWithRedirect,
    logout: auth0Logout,
    user: auth0User,
  } = useAuth0()

  useEffect(() => {
    if (!isAuthenticated) {
      setAccessTokenGetter(null)
      return undefined
    }

    setAccessTokenGetter(async () =>
      getAccessTokenSilently({
        authorizationParams: {
          audience: auth0Audience || undefined,
        },
      }),
    )

    return () => setAccessTokenGetter(null)
  }, [getAccessTokenSilently, isAuthenticated])

  const value = useMemo(
    () => ({
      user: auth0User
        ? {
            name: auth0User.name || auth0User.email || 'Learner',
            email: auth0User.email,
            picture: auth0User.picture,
            plan: 'Authenticated learner',
          }
        : null,
      isAuthenticated,
      isLoading,
      provider: 'auth0',
      login: () => loginWithRedirect(),
      logout: () =>
        auth0Logout({
          logoutParams: {
            returnTo: window.location.origin,
          },
        }),
    }),
    [auth0Logout, auth0User, isAuthenticated, isLoading, loginWithRedirect],
  )

  return (
    <AuthContextValue.Provider value={value}>
      {children}
    </AuthContextValue.Provider>
  )
}
