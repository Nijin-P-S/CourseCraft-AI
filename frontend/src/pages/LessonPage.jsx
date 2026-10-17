import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import {
  generateLessonContent,
  getCourse,
  getCourseProgress,
  getLessonContent,
  updateLessonProgress,
} from '../api/courses'
import LessonAudioPlayer from '../components/LessonAudioPlayer'
import LessonPDFExporter from '../components/LessonPDFExporter'
import LessonRenderer from '../components/LessonRenderer'

function LessonPage() {
  const { courseId, moduleId, lessonId } = useParams()
  const [course, setCourse] = useState(null)
  const [lessonContent, setLessonContent] = useState(null)
  const [progress, setProgress] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isGenerating, setIsGenerating] = useState(false)
  const [isSavingProgress, setIsSavingProgress] = useState(false)
  const [error, setError] = useState('')
  const [lessonNotice, setLessonNotice] = useState('')

  useEffect(() => {
    let ignore = false

    async function loadLesson() {
      setIsLoading(true)
      setError('')
      setLessonNotice('')

      try {
        const [courseData, progressData, contentData] = await Promise.all([
          getCourse(courseId),
          getCourseProgress(courseId).catch(() => null),
          getLessonContent(courseId, moduleId, lessonId).catch(() => null),
        ])

        if (!ignore) {
          setCourse(courseData)
          setProgress(progressData)
          setLessonContent(contentData)
          if (!contentData) {
            setLessonNotice('Generate this lesson to view the full content.')
          }
        }
      } catch (requestError) {
        if (!ignore) {
          setError(getApiErrorMessage(requestError))
        }
      } finally {
        if (!ignore) {
          setIsLoading(false)
        }
      }
    }

    loadLesson()

    return () => {
      ignore = true
    }
  }, [courseId, moduleId, lessonId])

  const currentModule = useMemo(
    () => course?.modules?.find((module) => module.id === moduleId),
    [course, moduleId],
  )

  const lessonSummary = useMemo(
    () => currentModule?.lessons?.find((lesson) => lesson.id === lessonId),
    [currentModule, lessonId],
  )

  const completedLessonIds = progress?.completedLessonIds || []
  const isCompleted = completedLessonIds.includes(lessonId)
  const lessonScore = progress?.assessmentScores?.[lessonId]

  async function handleGenerateLesson() {
    setIsGenerating(true)
    setError('')
    setLessonNotice('')

    try {
      const data = await generateLessonContent(courseId, moduleId, lessonId)
      setLessonContent(data)
      setCourse(await getCourse(courseId, { force: true }))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    } finally {
      setIsGenerating(false)
    }
  }

  async function handleProgressChange(event) {
    const completed = event.target.checked
    setIsSavingProgress(true)
    setError('')

    try {
      const updatedProgress = await updateLessonProgress(courseId, lessonId, {
        completed,
        assessmentScore: null,
      })
      setProgress(updatedProgress)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    } finally {
      setIsSavingProgress(false)
    }
  }

  async function handleAssessmentComplete(score) {
    const updatedProgress = await updateLessonProgress(courseId, lessonId, {
      completed: score === 100 || isCompleted,
      assessmentScore: score,
    })
    setProgress(updatedProgress)
  }

  if (isLoading) {
    return <section className="state-card">Loading lesson...</section>
  }

  if (!course || !currentModule || !lessonSummary) {
    return <section className="state-card">Lesson not found.</section>
  }

  return (
    <section className="page-stack lesson-page">
      <Link className="text-link" to={`/courses/${courseId}`}>
        Back to course
      </Link>

      <header className="section-header">
        <div>
          <p className="eyebrow">{currentModule.title}</p>
          <h1>{lessonContent?.title || lessonSummary.title}</h1>
        </div>
        <button
          className="button"
          type="button"
          onClick={handleGenerateLesson}
          disabled={isGenerating}
        >
          {isGenerating ? 'Generating...' : 'Generate lesson content'}
        </button>
      </header>

      <div className="lesson-workspace">
        <main className="lesson-main-panel">
          {error && <section className="state-card error-message">{error}</section>}
          {lessonNotice && <section className="state-card">{lessonNotice}</section>}
          <LessonRenderer
            blocks={lessonContent?.contentBlocks || []}
            onAssessmentComplete={handleAssessmentComplete}
          />
        </main>

        <aside className="lesson-side-panel">
          <section className="progress-card">
            <p className="eyebrow">Lesson status</p>
            <label>
              <input
                type="checkbox"
                checked={isCompleted}
                onChange={handleProgressChange}
                disabled={isSavingProgress}
              />{' '}
              Mark lesson complete
            </label>
            {lessonScore !== undefined && (
              <span className="score-pill">Quiz score: {lessonScore}%</span>
            )}
            {isSavingProgress && <span className="muted">Saving...</span>}
          </section>

          {lessonContent?.objectives?.length > 0 && (
            <section className="objectives-card">
              <p className="eyebrow">Objectives</p>
              <h2>What you will learn</h2>
              <ul>
                {lessonContent.objectives.map((objective) => (
                  <li key={objective}>{objective}</li>
                ))}
              </ul>
            </section>
          )}

          <LessonPDFExporter
            lessonContent={lessonContent}
            lessonTitle={lessonSummary.title}
            moduleTitle={currentModule.title}
          />

          <LessonAudioPlayer
            courseId={courseId}
            moduleId={moduleId}
            lessonId={lessonId}
            disabled={!lessonContent}
          />
        </aside>
      </div>
    </section>
  )
}

export default LessonPage
