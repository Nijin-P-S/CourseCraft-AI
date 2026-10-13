function HomePage() {
  return (
    <section className="hero-section" id="generate">
      <p className="eyebrow">AI-powered course generator</p>
      <h1>Turn any topic into a structured learning path.</h1>
      <p className="hero-copy">
        CourseCraft AI will generate course outlines, rich lessons, quizzes,
        video recommendations, progress tracking, PDFs, and multilingual
        explanations from a single prompt.
      </p>

      <div className="prompt-card" aria-label="Prompt form placeholder">
        <label htmlFor="topic">What do you want to learn?</label>
        <div className="prompt-row">
          <input id="topic" type="text" placeholder="Intro to React Hooks" />
          <button type="button">Generate Course</button>
        </div>
      </div>
    </section>
  )
}

export default HomePage
