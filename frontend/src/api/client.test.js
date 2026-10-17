import { describe, expect, it } from 'vitest'
import { getApiErrorMessage } from './client'

describe('getApiErrorMessage', () => {
  it('prefers API response message values', () => {
    expect(
      getApiErrorMessage({
        response: {
          data: {
            message: 'Course not found',
            error: 'Not Found',
          },
        },
      }),
    ).toBe('Course not found')
  })

  it('falls back to API error, thrown error message, and default text', () => {
    expect(
      getApiErrorMessage({
        response: {
          data: {
            error: 'Validation failed',
          },
        },
      }),
    ).toBe('Validation failed')

    expect(getApiErrorMessage(new Error('Network Error'))).toBe('Network Error')
    expect(getApiErrorMessage({})).toBe(
      'Something went wrong. Please try again.',
    )
  })
})
