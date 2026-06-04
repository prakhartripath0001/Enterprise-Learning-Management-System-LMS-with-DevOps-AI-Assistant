import React from 'react'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import ProtectedRoute from '../components/ProtectedRoute'

describe('ProtectedRoute Component', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('redirects to /login if no access token is present', () => {
    render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route
            path="/protected"
            element={
              <ProtectedRoute>
                <div>Private Content</div>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<div>Login Page Mock</div>} />
        </Routes>
      </MemoryRouter>
    )

    expect(screen.queryByText('Private Content')).not.toBeInTheDocument()
    expect(screen.getByText('Login Page Mock')).toBeInTheDocument()
  })

  it('renders children if access token is present', () => {
    localStorage.setItem('accessToken', 'valid-token')

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route
            path="/protected"
            element={
              <ProtectedRoute>
                <div>Private Content</div>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<div>Login Page Mock</div>} />
        </Routes>
      </MemoryRouter>
    )

    expect(screen.getByText('Private Content')).toBeInTheDocument()
    expect(screen.queryByText('Login Page Mock')).not.toBeInTheDocument()
  })
})
