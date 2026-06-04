import React from 'react'

export default function Input({
  label,
  error,
  type = 'text',
  id,
  className = '',
  ...props
}) {
  const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`

  return (
    <div className="flex flex-col w-full gap-1.5">
      {label && (
        <label htmlFor={inputId} className="text-xs font-semibold text-gray-400 uppercase tracking-wider">
          {label}
        </label>
      )}
      <input
        type={type}
        id={inputId}
        className={`w-full px-4 py-2 bg-gray-900/60 border ${
          error ? 'border-red-500 focus:ring-red-500' : 'border-gray-800 focus:ring-purple-500'
        } rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-950 transition-all duration-200 ${className}`}
        aria-invalid={!!error}
        aria-describedby={error ? `${inputId}-error` : undefined}
        {...props}
      />
      {error && (
        <span id={`${inputId}-error`} className="text-xs text-red-400 font-medium">
          {error}
        </span>
      )}
    </div>
  )
}
