import React from 'react'

export default function Card({
  children,
  variant = 'default',
  interactive = false,
  className = '',
  ...props
}) {
  const baseStyles = 'rounded-xl overflow-hidden p-6 transition-all duration-300'

  const variants = {
    default: 'bg-gray-900/50 border border-gray-800/80',
    elevated: 'bg-gray-900/80 border border-gray-800 shadow-xl',
    bordered: 'bg-transparent border-2 border-gray-800/60',
  }

  const hoverStyles = interactive
    ? 'hover:border-purple-500/50 hover:shadow-lg hover:shadow-purple-500/5 hover:-translate-y-0.5 cursor-pointer'
    : ''

  return (
    <div
      className={`${baseStyles} ${variants[variant]} ${hoverStyles} ${className}`}
      {...props}
    >
      {children}
    </div>
  )
}
