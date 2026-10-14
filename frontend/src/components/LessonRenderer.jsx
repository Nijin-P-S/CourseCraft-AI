function LessonRenderer({ blocks = [] }) {
  if (blocks.length === 0) {
    return <p className="muted">No lesson content has been generated yet.</p>
  }

  return (
    <div className="lesson-content">
      {blocks.map((block, index) => (
        <ContentBlock block={block} key={`${block.type}-${index}`} />
      ))}
    </div>
  )
}

function ContentBlock({ block }) {
  switch (block.type) {
    case 'heading':
      return <h2>{block.text}</h2>
    case 'paragraph':
      return <p>{block.text}</p>
    case 'video':
      return <VideoBlock block={block} />
    case 'mcq':
      return <McqBlock block={block} />
    case 'code':
      return (
        <pre className="code-block">
          <code>{block.code || block.text}</code>
        </pre>
      )
    case 'image':
      return (
        <figure className="media-card">
          <img src={block.url} alt={block.alt || block.caption || ''} />
          {block.caption && <figcaption>{block.caption}</figcaption>}
        </figure>
      )
    case 'note':
      return <aside className="note-block">{block.text}</aside>
    default:
      return (
        <aside className="note-block">
          Unsupported content block: <code>{block.type || 'unknown'}</code>
        </aside>
      )
  }
}

function VideoBlock({ block }) {
  const query = block.query || block.title || ''
  const youtubeUrl = `https://www.youtube.com/results?search_query=${encodeURIComponent(
    query,
  )}`

  return (
    <aside className="video-block">
      <strong>Video search</strong>
      <p>{query || 'Video recommendation pending.'}</p>
      {query && (
        <a className="text-link" href={youtubeUrl} rel="noreferrer" target="_blank">
          Search on YouTube
        </a>
      )}
    </aside>
  )
}

function McqBlock({ block }) {
  const answerIndex = Number(block.answerIndex)

  return (
    <section className="mcq-block">
      <h3>{block.question}</h3>
      <ol>
        {(block.options || []).map((option, index) => (
          <li className={index === answerIndex ? 'correct-answer' : ''} key={option}>
            {option}
          </li>
        ))}
      </ol>
      {block.explanation && <p className="muted">{block.explanation}</p>}
    </section>
  )
}

export default LessonRenderer
