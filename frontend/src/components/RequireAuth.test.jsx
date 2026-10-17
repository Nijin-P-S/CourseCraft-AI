import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import AuthContextValue from '../context/AuthContextValue'
import RequireAuth from './RequireAuth'

function renderWithAuth(authValue) {
  return render(
    <MemoryRouter>
      <AuthContextValue.Provider value={authValue}>
        <RequireAuth>
          <div>Protected workspace</div>
        </RequireAuth>
      </AuthContextValue.Provider>
    </MemoryRouter>,
  )
}

describe('RequireAuth', () => {
  it('shows the loading state while authentication is being checked', () => {
    renderWithAuth({ isLoading: true })

    expect(screen.getByText('Checking sign-in...')).toBeInTheDocument()
  })

  it('renders children for authenticated users', () => {
    renderWithAuth({ isAuthenticated: true, isLoading: false })

    expect(screen.getByText('Protected workspace')).toBeInTheDocument()
  })

  it('prompts unauthenticated users to sign in', async () => {
    const user = userEvent.setup()
    const login = vi.fn()

    renderWithAuth({
      isAuthenticated: false,
      isLoading: false,
      login,
      provider: 'local',
    })

    await user.click(
      screen.getByRole('button', { name: 'Log in with demo account' }),
    )

    expect(login).toHaveBeenCalledOnce()
    expect(screen.getByRole('link', { name: 'Back to overview' })).toHaveAttribute(
      'href',
      '/',
    )
  })
})
