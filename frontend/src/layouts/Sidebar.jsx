import React, { useState } from 'react'
import { NavLink } from 'react-router-dom'

export default function Sidebar({
  role = 'student', // placeholder for role based access control (rbac)
  className = '',
}) {
  const [isCollapsed, setIsCollapsed] = useState(false)

  const menuItems = {
    student: [
      { path: '/', label: 'Dashboard', icon: '📊' },
      { path: '/courses', label: 'My Courses', icon: '📚' },
      { path: '/about', label: 'Certifications', icon: '🎓' },
    ],
    instructor: [
      { path: '/', label: 'Instructor Hub', icon: '👨‍🏫' },
      { path: '/courses', label: 'Course Builder', icon: '📝' },
      { path: '/contact', label: 'Analytics', icon: '📈' },
    ],
    admin: [
      { path: '/', label: 'Admin Panel', icon: '⚙️' },
      { path: '/courses', label: 'Manage Courses', icon: '🛡️' },
      { path: '/about', label: 'User Directory', icon: '👥' },
    ],
  }

  const activeRoleItems = menuItems[role] || menuItems.student

  return (
    <aside
      className={`hidden lg:flex flex-col h-[calc(100vh-4rem)] sticky top-16 border-r border-gray-800/80 bg-gray-950/40 transition-all duration-300 ${
        isCollapsed ? 'w-20' : 'w-64'
      } ${className}`}
      aria-label="Sidebar navigation"
    >
      {/* Collapse toggle button */}
      <div className="flex justify-end p-4">
        <button
          onClick={() => setIsCollapsed(!isCollapsed)}
          className="p-1.5 rounded-lg border border-gray-800 hover:bg-gray-900 text-gray-400 hover:text-white transition-colors duration-150"
          aria-label={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {isCollapsed ? '➡️' : '⬅️'}
        </button>
      </div>

      {/* Nav links */}
      <nav className="flex-1 px-3 space-y-1">
        {activeRoleItems.map((item) => (
          <NavLink
            key={item.label}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center gap-4 px-3 py-3 text-sm font-semibold rounded-xl transition-all duration-200 ${
                isActive
                  ? 'bg-purple-600/10 text-purple-400 border border-purple-500/20'
                  : 'text-gray-400 hover:text-white hover:bg-gray-900 border border-transparent'
              }`
            }
          >
            <span className="text-lg select-none">{item.icon}</span>
            {!isCollapsed && <span className="truncate">{item.label}</span>}
          </NavLink>
        ))}
      </nav>

      {/* Sidebar Footer */}
      {!isCollapsed && (
        <div className="p-4 border-t border-gray-900 text-center">
          <p className="text-[10px] uppercase font-bold tracking-widest text-gray-500">Workspace Role</p>
          <p className="text-xs font-semibold text-purple-400 mt-1 capitalize">{role}</p>
        </div>
      )}
    </aside>
  )
}
