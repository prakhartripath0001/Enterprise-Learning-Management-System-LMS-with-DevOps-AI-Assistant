import React from 'react'
import Navbar from './Navbar'
import Sidebar from './Sidebar'
import Footer from './Footer'

export default function AppLayout({ children }) {
  return (
    <div className="flex flex-col min-h-screen bg-[#0b0f19] text-slate-100 font-sans selection:bg-purple-500/30 selection:text-white">
      {/* Global Navbar */}
      <Navbar />

      {/* Main layout context */}
      <div className="flex flex-1 w-full mx-auto max-w-7xl">
        {/* Collapsible Sidebar */}
        <Sidebar role="student" />

        {/* Dynamic content context */}
        <main className="flex-1 flex flex-col min-w-0 p-4 sm:p-6 lg:p-8">
          <div className="flex-grow">
            {children}
          </div>
        </main>
      </div>

      {/* Global Footer */}
      <Footer />
    </div>
  )
}
