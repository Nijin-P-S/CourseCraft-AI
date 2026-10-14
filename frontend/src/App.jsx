import { BrowserRouter, Route, Routes } from 'react-router-dom'
import './App.css'
import AppLayout from './layouts/AppLayout'
import CourseDetailPage from './pages/CourseDetailPage'
import CoursesPage from './pages/CoursesPage'
import HomePage from './pages/HomePage'
import LessonPage from './pages/LessonPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/courses" element={<CoursesPage />} />
          <Route path="/courses/:courseId" element={<CourseDetailPage />} />
          <Route
            path="/courses/:courseId/modules/:moduleId/lessons/:lessonId"
            element={<LessonPage />}
          />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
