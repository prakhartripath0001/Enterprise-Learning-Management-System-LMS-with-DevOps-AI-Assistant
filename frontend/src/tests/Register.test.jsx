import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import Register from '../pages/Register'

describe('Register Component', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  const renderRegister = () => {
    return render(
      <BrowserRouter>
        <Register />
      </BrowserRouter>
    )
  }

  it('renders all registration input fields', () => {
    renderRegister()
    expect(screen.getByLabelText(/first name/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/last name/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument()
    expect(screen.getAllByLabelText(/password/i)[0]).toBeInTheDocument()
    expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument()
  })

  it('shows error if password and confirm password do not match', async () => {
    renderRegister()
    const passwordInput = screen.getAllByLabelText(/password/i)[0]
    const confirmPasswordInput = screen.getByLabelText(/confirm password/i)
    const submitBtn = screen.getByRole('button', { name: /sign up/i })

    await userEvent.type(passwordInput, 'SecureP@ss123')
    await userEvent.type(confirmPasswordInput, 'differentpass')
    await userEvent.click(submitBtn)

    expect(screen.getByText('Passwords do not match')).toBeInTheDocument()
    expect(fetch).not.toHaveBeenCalled()
  })

  it('submits form successfully and displays success message', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        status: 'success',
        message: 'Registration successful',
        data: { id: 'some-uuid' }
      })
    })

    renderRegister()

    await userEvent.type(screen.getByLabelText(/first name/i), 'John')
    await userEvent.type(screen.getByLabelText(/last name/i), 'Doe')
    await userEvent.type(screen.getByLabelText(/email address/i), 'john.doe@example.com')
    await userEvent.type(screen.getByLabelText(/username/i), 'johndoe')
    await userEvent.type(screen.getAllByLabelText(/password/i)[0], 'SecureP@ss123')
    await userEvent.type(screen.getByLabelText(/confirm password/i), 'SecureP@ss123')

    await userEvent.click(screen.getByRole('button', { name: /sign up/i }))

    expect(fetch).toHaveBeenCalledTimes(1)
    const successAlert = await screen.findByText(/Registration successful! Please check your email/i)
    expect(successAlert).toBeInTheDocument()
  })
})
