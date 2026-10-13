function AppLayout({ children }) {
  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="Primary navigation">
        <div className="brand">CourseCraft AI</div>
        <nav>
          <a href="#generate">Generate</a>
          <a href="#courses">Courses</a>
          <a href="#progress">Progress</a>
        </nav>
      </aside>

      <main className="main-content">{children}</main>
    </div>
  )
}

export default AppLayout
