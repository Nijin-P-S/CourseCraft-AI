import { useEffect, useState } from 'react'
import { generateLessonAudio } from '../api/courses'
import { getApiErrorMessage } from '../api/client'

function LessonAudioPlayer({ courseId, moduleId, lessonId, disabled }) {
  const [audioUrl, setAudioUrl] = useState('')
  const [isGenerating, setIsGenerating] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    return () => {
      if (audioUrl) {
        URL.revokeObjectURL(audioUrl)
      }
    }
  }, [audioUrl])

  async function handleGenerateAudio() {
    setIsGenerating(true)
    setError('')

    try {
      const audioBlob = await generateLessonAudio(courseId, moduleId, lessonId)
      if (audioUrl) {
        URL.revokeObjectURL(audioUrl)
      }
      setAudioUrl(URL.createObjectURL(audioBlob))
    } catch (requestError) {
      setError(await getAudioErrorMessage(requestError))
    } finally {
      setIsGenerating(false)
    }
  }

  return (
    <section className="lesson-tool-card">
      <p className="eyebrow">Hinglish support</p>
      <h2>Audio explanation</h2>
      <p className="muted">
        Generate a friendly Hinglish narration for this lesson using Gemini TTS.
      </p>
      <button
        className="button secondary"
        type="button"
        onClick={handleGenerateAudio}
        disabled={disabled || isGenerating}
      >
        {isGenerating ? 'Generating audio...' : 'Generate Hinglish audio'}
      </button>
      {disabled && <p className="muted">Generate the lesson before creating audio.</p>}
      {audioUrl && (
        <audio className="lesson-audio-player" controls src={audioUrl}>
          <track kind="captions" />
        </audio>
      )}
      {error && <p className="error-message">{error}</p>}
    </section>
  )
}

async function getAudioErrorMessage(error) {
  const data = error?.response?.data
  if (data instanceof Blob) {
    try {
      const text = await data.text()
      const parsed = JSON.parse(text)
      return parsed.message || parsed.error || text
    } catch {
      return getApiErrorMessage(error)
    }
  }

  return getApiErrorMessage(error)
}

export default LessonAudioPlayer
