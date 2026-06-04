import React, { useEffect, useRef } from 'react'

export default function Modal({
  isOpen,
  onClose,
  title,
  children,
  className = '',
  ...props
}) {
  const modalRef = useRef(null)

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        onClose()
      }
    }

    if (isOpen) {
      document.body.style.overflow = 'hidden'
      window.addEventListener('keydown', handleKeyDown)
      // Focus modal container on open for keyboard accessibility
      if (modalRef.current) {
        modalRef.current.focus()
      }
    }

    return () => {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [isOpen, onClose])

  if (!isOpen) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
      onClick={onClose}
      {...props}
    >
      <div
        ref={modalRef}
        tabIndex={-1}
        className={`w-full max-w-lg bg-gray-900/95 border border-gray-800 rounded-2xl shadow-2xl overflow-hidden focus:outline-none ${className}`}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-800">
          <h3 id="modal-title" className="text-lg font-bold text-white tracking-wide">
            {title}
          </h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white transition-colors duration-150 text-2xl font-light p-1 leading-none"
            aria-label="Close modal"
          >
            &times;
          </button>
        </div>

        {/* Modal Body */}
        <div className="px-6 py-6 text-gray-300">
          {children}
        </div>
      </div>
    </div>
  )
}
