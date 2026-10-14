import { NavLink, Outlet } from 'react-router-dom'

function AppLayout() {
  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="Primary navigation">
        <div className="brand">CourseCraft AI</div>
        <nav>
          <NavLink to="/" end>
            Generate
          </NavLink>
          <NavLink to="/courses">Courses</NavLink>
        </nav>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}

export default AppLayout
