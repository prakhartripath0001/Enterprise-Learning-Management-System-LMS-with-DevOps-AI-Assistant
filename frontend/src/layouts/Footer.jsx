import React from 'react'
import { Link } from 'react-router-dom'

export default function Footer() {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="w-full border-t border-gray-800/80 bg-gray-950/60 py-12">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand info */}
          <div className="md:col-span-2">
            <Link to="/" className="flex items-center gap-2 text-lg font-bold text-white">
              <span className="text-purple-500">▲</span>
              <span>AetherLMS</span>
            </Link>
            <p className="mt-4 max-w-sm text-sm text-gray-400 leading-relaxed">
              Next-generation enterprise education platform powered by state-of-the-art cloud architecture and microservices.
            </p>
          </div>

          {/* Quick links columns */}
          <div>
            <h4 className="text-xs font-semibold text-gray-300 uppercase tracking-wider">Platform</h4>
            <ul className="mt-4 space-y-2 text-sm text-gray-400">
              <li>
                <Link to="/courses" className="hover:text-purple-400 transition-colors">Browse Courses</Link>
              </li>
              <li>
                <a href="#features" className="hover:text-purple-400 transition-colors">Features</a>
              </li>
              <li>
                <a href="#pricing" className="hover:text-purple-400 transition-colors">Pricing</a>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="text-xs font-semibold text-gray-300 uppercase tracking-wider">Company</h4>
            <ul className="mt-4 space-y-2 text-sm text-gray-400">
              <li>
                <Link to="/about" className="hover:text-purple-400 transition-colors">About Us</Link>
              </li>
              <li>
                <Link to="/contact" className="hover:text-purple-400 transition-colors">Contact</Link>
              </li>
              <li>
                <a href="#careers" className="hover:text-purple-400 transition-colors">Careers</a>
              </li>
            </ul>
          </div>
        </div>

        {/* Footer bottom bar */}
        <div className="mt-12 border-t border-gray-900 pt-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-gray-500 font-medium">
          <p>&copy; {currentYear} AetherLMS. All rights reserved.</p>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 bg-emerald-500 rounded-full animate-pulse"></span>
            <span>All systems operational</span>
          </div>
        </div>
      </div>
    </footer>
  )
}
