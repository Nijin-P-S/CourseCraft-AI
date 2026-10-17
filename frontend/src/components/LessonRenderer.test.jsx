import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import LessonRenderer from './LessonRenderer'

describe('LessonRenderer', () => {
  it('renders an empty state when lesson content is missing', () => {
    render(<LessonRenderer />)

    expect(
      screen.getByText('No lesson content has been generated yet.'),
    ).toBeInTheDocument()
  })

  it('renders unsupported content blocks without crashing', () => {
    render(<LessonRenderer blocks={[{ type: 'diagram' }]} />)

    expect(screen.getByText(/Unsupported content block:/)).toBeInTheDocument()
    expect(screen.getByText('diagram')).toBeInTheDocument()
  })

  it('submits MCQ answers and reports the calculated score', async () => {
    const user = userEvent.setup()
    const onAssessmentComplete = vi.fn().mockResolvedValue(undefined)

    render(
      <LessonRenderer
        blocks={[
          {
            type: 'mcq',
            question: 'Which option is correct?',
            options: ['A', 'B'],
            answerIndex: 1,
            explanation: 'B is the correct answer.',
          },
        ]}
        onAssessmentComplete={onAssessmentComplete}
      />,
    )

    await user.click(screen.getByRole('radio', { name: 'B' }))
    await user.click(screen.getByRole('button', { name: 'Submit answer' }))

    expect(onAssessmentComplete).toHaveBeenCalledWith(100)
    expect(await screen.findByText('Correct. Score saved.')).toBeInTheDocument()
    expect(screen.getByText('B is the correct answer.')).toBeInTheDocument()
  })

  it('shows a save error when the assessment callback fails', async () => {
    const user = userEvent.setup()
    const onAssessmentComplete = vi
      .fn()
      .mockRejectedValue(new Error('Unable to save score'))

    render(
      <LessonRenderer
        blocks={[
          {
            type: 'mcq',
            question: 'Choose one.',
            options: ['First', 'Second'],
            answerIndex: 1,
          },
        ]}
        onAssessmentComplete={onAssessmentComplete}
      />,
    )

    await user.click(screen.getByRole('radio', { name: 'First' }))
    await user.click(screen.getByRole('button', { name: 'Submit answer' }))

    expect(onAssessmentComplete).toHaveBeenCalledWith(0)
    expect(
      await screen.findByText('Unable to save score'),
    ).toBeInTheDocument()
  })
})
