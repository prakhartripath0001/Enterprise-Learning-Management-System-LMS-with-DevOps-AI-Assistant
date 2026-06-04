import React from 'react'

export default function Select({
  label,
  error,
  options = [],
  id,
  className = '',
  children,
  ...props
}) {
  const selectId = id || `select-${Math.random().toString(36).substr(2, 9)}`

  return (
    <div className="flex flex-col w-full gap-1.5">
      {label && (
        <label htmlFor={selectId} className="text-xs font-semibold text-gray-400 uppercase tracking-wider">
          {label}
        </label>
      )}
      <select
        id={selectId}
        className={`w-full px-4 py-2 bg-gray-900/60 border ${
          error ? 'border-red-500 focus:ring-red-500' : 'border-gray-800 focus:ring-purple-500'
        } rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-955 transition-all duration-200 ${className}`}
        aria-invalid={!!error}
        aria-describedby={error ? `${selectId}-error` : undefined}
        {...props}
      >
        {children ||
          options.map((opt) => (
            <option key={opt.value} value={opt.value} className="bg-gray-900 text-white">
              {opt.label}
            </option>
          ))}
      </select>
      {error && (
        <span id={`${selectId}-error`} className="text-xs text-red-400 font-medium">
          {error}
        </span>
      )}
    </div>
  )
}
