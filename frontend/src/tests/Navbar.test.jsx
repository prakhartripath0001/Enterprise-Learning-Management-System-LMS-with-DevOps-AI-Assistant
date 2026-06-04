import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect } from 'vitest'
import Navbar from '../layouts/Navbar'

describe('Navbar Component', () => {
  const renderNavbar = () => {
    return render(
      <BrowserRouter>
        <Navbar />
      </BrowserRouter>
    )
  }

  it('renders branding and main links successfully', () => {
    renderNavbar()
    expect(screen.getByText('AetherLMS')).toBeInTheDocument()
    expect(screen.getByText('Home')).toBeInTheDocument()
    expect(screen.getByText('About')).toBeInTheDocument()
    expect(screen.getByText('Courses')).toBeInTheDocument()
    expect(screen.getByText('Contact')).toBeInTheDocument()
  })

  it('renders login and register action buttons', () => {
    renderNavbar()
    expect(screen.getByText('Login')).toBeInTheDocument()
    expect(screen.getByText('Register')).toBeInTheDocument()
  })

  it('toggles mobile menu class on hamburger button click', async () => {
    renderNavbar()
    const hamburger = screen.getByRole('button', { name: /toggle menu/i })
    expect(hamburger).toBeInTheDocument()
    
    // Simulate user click
    await userEvent.click(hamburger)
    const navMenuWrapper = hamburger.nextElementSibling
    expect(navMenuWrapper).toHaveClass('active')
  })
})
