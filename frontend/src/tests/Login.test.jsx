import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import Login from '../pages/Login'

describe('Login Component', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  const renderLogin = () => {
    return render(
      <BrowserRouter>
        <Login />
      </BrowserRouter>
    )
  }

  it('renders login form inputs and submit button', () => {
    renderLogin()
    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })

  it('shows error alert when login fetch fails', async () => {
    fetch.mockResolvedValueOnce({
      ok: false,
      json: async () => ({ message: 'Invalid email or password' })
    })

    renderLogin()

    const emailInput = screen.getByLabelText(/email address/i)
    const passwordInput = screen.getByLabelText(/password/i)
    const submitBtn = screen.getByRole('button', { name: /sign in/i })

    await userEvent.type(emailInput, 'wrong@user.com')
    await userEvent.type(passwordInput, 'wrongpass')
    await userEvent.click(submitBtn)

    expect(fetch).toHaveBeenCalledTimes(1)
    const alert = await screen.findByText('Invalid email or password')
    expect(alert).toBeInTheDocument()
    expect(alert).toHaveClass('alert-error')
  })

  it('shows success alert when login fetch succeeds', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        status: 'success',
        data: {
          accessToken: 'fake.jwt.token',
          user: { email: 'john.doe@example.com' }
        }
      })
    })

    renderLogin()

    const emailInput = screen.getByLabelText(/email address/i)
    const passwordInput = screen.getByLabelText(/password/i)
    const submitBtn = screen.getByRole('button', { name: /sign in/i })

    await userEvent.type(emailInput, 'john.doe@example.com')
    await userEvent.type(passwordInput, 'SecureP@ss123')
    await userEvent.click(submitBtn)

    expect(fetch).toHaveBeenCalledTimes(1)
    const alert = await screen.findByText('Login successful! Welcome back.')
    expect(alert).toBeInTheDocument()
    expect(alert).toHaveClass('alert-success')
  })
})
