import { BrowserRouter, Route, Routes } from 'react-router-dom'
import './App.css'
import AppLayout from './layouts/AppLayout'
import RequireAuth from './components/RequireAuth'
import { AuthProvider } from './context/AuthContext'
import CourseDetailPage from './pages/CourseDetailPage'
import CoursesPage from './pages/CoursesPage'
import HomePage from './pages/HomePage'
import LandingPage from './pages/LandingPage'
import LessonPage from './pages/LessonPage'

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route
            element={
              <RequireAuth>
                <AppLayout />
              </RequireAuth>
            }
          >
            <Route path="/dashboard" element={<HomePage />} />
            <Route path="/courses" element={<CoursesPage />} />
            <Route path="/courses/:courseId" element={<CourseDetailPage />} />
            <Route
              path="/courses/:courseId/modules/:moduleId/lessons/:lessonId"
              element={<LessonPage />}
            />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
