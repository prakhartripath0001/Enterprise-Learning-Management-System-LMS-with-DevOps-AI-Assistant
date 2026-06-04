import React from 'react'

export default function Alert({
  children,
  variant = 'info',
  onClose,
  className = '',
  ...props
}) {
  const variants = {
    info: 'bg-blue-950/40 text-blue-300 border-blue-800/80',
    success: 'bg-emerald-950/40 text-emerald-300 border-emerald-800/80',
    warning: 'bg-amber-955/40 text-amber-300 border-amber-800/80',
    error: 'bg-red-950/40 text-red-300 border-red-800/80',
  }

  const icons = {
    info: 'ℹ️',
    success: '✅',
    warning: '⚠️',
    error: '❌',
  }

  return (
    <div
      role="alert"
      className={`flex items-start gap-3 p-4 border rounded-xl text-sm ${variants[variant]} ${className}`}
      {...props}
    >
      <span className="text-base select-none">{icons[variant]}</span>
      <div className={`flex-1 ${className}`}>{children}</div>
      {onClose && (
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-white transition-colors duration-150"
          aria-label="Close alert"
        >
          &times;
        </button>
      )}
    </div>
  )
}
