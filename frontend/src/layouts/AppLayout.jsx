import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

function AppLayout() {
  const { user, logout } = useAuth()

  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="Primary navigation">
        <NavLink className="brand" to="/">
          <span>CC</span>
          CourseCraft AI
        </NavLink>
        <nav>
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/courses">Courses</NavLink>
        </nav>
        <div className="sidebar-footer">
          <div className="user-chip">
            <span>{user?.name?.charAt(0) || 'U'}</span>
            <div>
              <strong>{user?.name || 'Learner'}</strong>
              <small>{user?.plan || 'CourseCraft'}</small>
            </div>
          </div>
          <button className="text-button" type="button" onClick={logout}>
            Log out
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}

export default AppLayout
