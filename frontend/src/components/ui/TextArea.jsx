import React from 'react'

export default function TextArea({
  label,
  error,
  id,
  rows = 4,
  className = '',
  ...props
}) {
  const textareaId = id || `textarea-${Math.random().toString(36).substr(2, 9)}`

  return (
    <div className="flex flex-col w-full gap-1.5">
      {label && (
        <label htmlFor={textareaId} className="text-xs font-semibold text-gray-400 uppercase tracking-wider">
          {label}
        </label>
      )}
      <textarea
        id={textareaId}
        rows={rows}
        className={`w-full px-4 py-2 bg-gray-900/60 border ${
          error ? 'border-red-500 focus:ring-red-500' : 'border-gray-800 focus:ring-purple-500'
        } rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-950 transition-all duration-200 resize-none ${className}`}
        aria-invalid={!!error}
        aria-describedby={error ? `${textareaId}-error` : undefined}
        {...props}
      />
      {error && (
        <span id={`${textareaId}-error`} className="text-xs text-red-400 font-medium">
          {error}
        </span>
      )}
    </div>
  )
}
