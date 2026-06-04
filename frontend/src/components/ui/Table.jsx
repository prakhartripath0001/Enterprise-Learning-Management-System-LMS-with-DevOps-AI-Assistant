import React from 'react'
import Spinner from './Spinner'

export default function Table({
  headers = [],
  data = [],
  loading = false,
  emptyMessage = 'No data available',
  renderRow,
  className = '',
  ...props
}) {
  return (
    <div className={`w-full overflow-x-auto rounded-xl border border-gray-800/80 bg-gray-900/20 ${className}`} {...props}>
      <table className="w-full text-left border-collapse text-sm">
        <thead>
          <tr className="border-b border-gray-800 bg-gray-900/60 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            {headers.map((header, idx) => (
              <th key={idx} className="px-6 py-4">
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-800/60">
          {loading ? (
            <tr>
              <td colSpan={headers.length} className="px-6 py-12 text-center">
                <div className="flex flex-col items-center justify-center gap-3">
                  <Spinner size="md" />
                  <span className="text-gray-400">Loading data...</span>
                </div>
              </td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={headers.length} className="px-6 py-12 text-center text-gray-500 font-medium">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((item, rowIdx) => renderRow(item, rowIdx))
          )}
        </tbody>
      </table>
    </div>
  )
}
