import React, { useState } from 'react'
import { NavLink, Link } from 'react-router-dom'

export default function Navbar() {
  const [isOpen, setIsOpen] = useState(false)

  const toggleMenu = () => setIsOpen(!isOpen)
  const closeMenu = () => setIsOpen(false)

  return (
    <nav className="sticky top-0 z-40 w-full border-b border-gray-800 bg-gray-950/80 backdrop-blur-md">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          {/* Logo / Branding */}
          <Link to="/" className="flex items-center gap-2 text-xl font-extrabold text-white tracking-wider" onClick={closeMenu}>
            <span className="text-purple-500">▲</span>
            <span>AetherLMS</span>
          </Link>

          {/* Accessible Hamburger Menu Button */}
          <button
            className="hamburger flex flex-col justify-center gap-1.5 w-8 h-8 md:hidden text-gray-400 hover:text-white focus:outline-none"
            onClick={toggleMenu}
            aria-label="Toggle menu"
          >
            <span className={`block w-6 h-0.5 bg-current transition-transform duration-300 ${isOpen ? 'rotate-45 translate-y-2' : ''}`}></span>
            <span className={`block w-6 h-0.5 bg-current transition-opacity duration-300 ${isOpen ? 'opacity-0' : ''}`}></span>
            <span className={`block w-6 h-0.5 bg-current transition-transform duration-300 ${isOpen ? '-rotate-45 -translate-y-2' : ''}`}></span>
          </button>

          {/* Menu Wrapper for Links and Actions */}
          <div className={`nav-menu-wrapper ${isOpen ? 'active flex' : 'hidden'} md:flex absolute md:relative top-16 md:top-0 left-0 w-full md:w-auto flex-col md:flex-row items-center gap-6 p-6 md:p-0 bg-gray-950 md:bg-transparent border-b border-gray-800 md:border-none`}>
            <ul className="flex flex-col md:flex-row items-center gap-6 text-sm font-medium">
              <li>
                <NavLink
                  to="/"
                  className={({ isActive }) =>
                    `nav-link transition-colors duration-200 ${
                      isActive ? 'text-purple-400' : 'text-gray-300 hover:text-white'
                    }`
                  }
                  onClick={closeMenu}
                >
                  Home
                </NavLink>
              </li>
              <li>
                <NavLink
                  to="/about"
                  className={({ isActive }) =>
                    `nav-link transition-colors duration-200 ${
                      isActive ? 'text-purple-400' : 'text-gray-300 hover:text-white'
                    }`
                  }
                  onClick={closeMenu}
                >
                  About
                </NavLink>
              </li>
              <li>
                <NavLink
                  to="/courses"
                  className={({ isActive }) =>
                    `nav-link transition-colors duration-200 ${
                      isActive ? 'text-purple-400' : 'text-gray-300 hover:text-white'
                    }`
                  }
                  onClick={closeMenu}
                >
                  Courses
                </NavLink>
              </li>
              <li>
                <NavLink
                  to="/contact"
                  className={({ isActive }) =>
                    `nav-link transition-colors duration-200 ${
                      isActive ? 'text-purple-400' : 'text-gray-300 hover:text-white'
                    }`
                  }
                  onClick={closeMenu}
                >
                  Contact
                </NavLink>
              </li>
            </ul>

            <div className="flex items-center gap-4 border-t border-gray-800 md:border-none pt-4 md:pt-0 w-full md:w-auto justify-center">
              <Link
                to="/login"
                className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white transition-colors duration-200"
                onClick={closeMenu}
              >
                Login
              </Link>
              <Link
                to="/register"
                className="rounded-lg bg-purple-600 hover:bg-purple-700 px-4 py-2 text-sm font-medium text-white shadow-md shadow-purple-500/10 transition-all duration-200"
                onClick={closeMenu}
              >
                Register
              </Link>
            </div>
          </div>
        </div>
      </div>
    </nav>
  )
}
