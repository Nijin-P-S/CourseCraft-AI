import { useRef, useState } from 'react'
import html2canvas from 'html2canvas'
import { jsPDF } from 'jspdf'

function LessonPDFExporter({ lessonContent, lessonTitle, moduleTitle }) {
  const exportRef = useRef(null)
  const [isExporting, setIsExporting] = useState(false)
  const [error, setError] = useState('')
  const hasContent = (lessonContent?.contentBlocks || []).length > 0

  async function handleExport() {
    if (!exportRef.current || !hasContent) {
      return
    }

    setIsExporting(true)
    setError('')

    try {
      const canvas = await html2canvas(exportRef.current, {
        backgroundColor: '#ffffff',
        scale: 2,
        useCORS: true,
      })
      const imageData = canvas.toDataURL('image/png')
      const pdf = new jsPDF('p', 'mm', 'a4')
      const pageWidth = pdf.internal.pageSize.getWidth()
      const pageHeight = pdf.internal.pageSize.getHeight()
      const imageWidth = pageWidth
      const imageHeight = (canvas.height * imageWidth) / canvas.width
      let heightLeft = imageHeight
      let position = 0

      pdf.addImage(imageData, 'PNG', 0, position, imageWidth, imageHeight)
      heightLeft -= pageHeight

      while (heightLeft > 0) {
        position = heightLeft - imageHeight
        pdf.addPage()
        pdf.addImage(imageData, 'PNG', 0, position, imageWidth, imageHeight)
        heightLeft -= pageHeight
      }

      pdf.save(`${toFileName(lessonContent?.title || lessonTitle)}.pdf`)
    } catch (exportError) {
      setError(exportError?.message || 'Could not export this lesson as a PDF.')
    } finally {
      setIsExporting(false)
    }
  }

  return (
    <section className="lesson-tool-card">
      <p className="eyebrow">Offline study</p>
      <h2>Download lesson PDF</h2>
      <p className="muted">
        Export the generated lesson with objectives, explanations, code, and quizzes.
      </p>
      <button
        className="button secondary"
        type="button"
        onClick={handleExport}
        disabled={!hasContent || isExporting}
      >
        {isExporting ? 'Preparing PDF...' : 'Download PDF'}
      </button>
      {!hasContent && <p className="muted">Generate the lesson before exporting.</p>}
      {error && <p className="error-message">{error}</p>}

      <div className="pdf-export-wrapper" aria-hidden="true">
        <article className="pdf-export-surface" ref={exportRef}>
          <p className="pdf-kicker">{moduleTitle}</p>
          <h1>{lessonContent?.title || lessonTitle}</h1>
          {lessonContent?.objectives?.length > 0 && (
            <section>
              <h2>Objectives</h2>
              <ul>
                {lessonContent.objectives.map((objective) => (
                  <li key={objective}>{objective}</li>
                ))}
              </ul>
            </section>
          )}
          <PdfBlocks blocks={lessonContent?.contentBlocks || []} />
        </article>
      </div>
    </section>
  )
}

function PdfBlocks({ blocks }) {
  return blocks.map((block, index) => {
    const key = `${block.type || 'block'}-${index}`

    switch (block.type) {
      case 'heading':
        return <h2 key={key}>{block.text}</h2>
      case 'paragraph':
        return <p key={key}>{block.text}</p>
      case 'code':
        return (
          <pre className="pdf-code-block" key={key}>
            <code>{block.code || block.text}</code>
          </pre>
        )
      case 'video':
        return (
          <section className="pdf-callout" key={key}>
            <strong>Recommended video search</strong>
            <p>{block.query || block.title || block.url}</p>
          </section>
        )
      case 'mcq':
        return (
          <section className="pdf-mcq" key={key}>
            <h3>{block.question}</h3>
            <ol>
              {(block.options || []).map((option) => (
                <li key={option}>{option}</li>
              ))}
            </ol>
            {block.explanation && <p>{block.explanation}</p>}
          </section>
        )
      case 'note':
        return (
          <section className="pdf-callout" key={key}>
            <p>{block.text}</p>
          </section>
        )
      default:
        return null
    }
  })
}

function toFileName(value) {
  return (value || 'lesson')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
}

export default LessonPDFExporter
