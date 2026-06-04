import React from 'react'

export default function Badge({
  children,
  variant = 'default',
  className = '',
  ...props
}) {
  const baseStyles = 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold tracking-wide border'

  const variants = {
    default: 'bg-gray-800 text-gray-300 border-gray-700',
    success: 'bg-emerald-950/60 text-emerald-400 border-emerald-800/60',
    danger: 'bg-red-950/60 text-red-400 border-red-800/60',
    warning: 'bg-amber-955/60 text-amber-400 border-amber-800/60',
    info: 'bg-blue-950/60 text-blue-400 border-blue-800/60',
  }

  return (
    <span className={`${baseStyles} ${variants[variant]} ${className}`} {...props}>
      {children}
    </span>
  )
}
