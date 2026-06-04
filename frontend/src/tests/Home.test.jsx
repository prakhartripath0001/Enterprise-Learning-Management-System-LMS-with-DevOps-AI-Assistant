import React from 'react'
import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect } from 'vitest'
import Home from '../pages/Home'

describe('Home Page Component', () => {
  const renderHome = () => {
    return render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>
    )
  }

  it('renders hero title and main slogan', () => {
    renderHome()
    expect(screen.getByText(/Empower Your Mind With/i)).toBeInTheDocument()
    expect(screen.getByText('AetherLMS')).toBeInTheDocument()
  })

  it('renders call to action buttons', () => {
    renderHome()
    expect(screen.getByRole('link', { name: /get started/i })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /explore courses/i })).toBeInTheDocument()
  })

  it('renders core system feature highlights', () => {
    renderHome()
    expect(screen.getByText('Stateless Security')).toBeInTheDocument()
    expect(screen.getByText('High Performance')).toBeInTheDocument()
    expect(screen.getByText('DevOps Ready')).toBeInTheDocument()
  })
})
