import { useMemo, useState } from 'react'
import AuthContextValue from './AuthContextValue'

const STORAGE_KEY = 'coursecraft.auth.user'

const demoUser = {
  name: 'Nijin',
  email: 'nijin@example.com',
  plan: 'MVP Learner',
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const storedUser = window.localStorage.getItem(STORAGE_KEY)
    return storedUser ? JSON.parse(storedUser) : null
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
