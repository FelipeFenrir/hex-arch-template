import React from 'react'

interface JsonViewerProps {
  data: unknown
  className?: string
}

export function JsonViewer({ data, className = '' }: JsonViewerProps) {
  const renderValue = (value: unknown, depth: number = 0): React.ReactNode => {
    if (value === null) {
      return <span className="text-red-600">null</span>
    }
    if (value === undefined) {
      return <span className="text-red-600">undefined</span>
    }
    if (typeof value === 'boolean') {
      return <span className="text-blue-600">{String(value)}</span>
    }
    if (typeof value === 'number') {
      return <span className="text-green-600">{value}</span>
    }
    if (typeof value === 'string') {
      return <span className="text-orange-600">"{value}"</span>
    }
    if (Array.isArray(value)) {
      if (value.length === 0) {
        return <span>[]</span>
      }
      return (
        <div className="ml-4">
          <span>[</span>
          <div className="ml-4 border-l pl-2">
            {value.map((item, idx) => (
              <div key={idx} className="text-sm font-mono">
                {renderValue(item, depth + 1)}
                {idx < value.length - 1 ? ',' : ''}
              </div>
            ))}
          </div>
          <span>]</span>
        </div>
      )
    }
    if (typeof value === 'object') {
      const entries = Object.entries(value as Record<string, unknown>)
      if (entries.length === 0) {
        return <span>{{}}</span>
      }
      return (
        <div className="ml-4">
          <span>{'{'}</span>
          <div className="ml-4 border-l pl-2">
            {entries.map(([key, val], idx) => (
              <div key={key} className="text-sm font-mono">
                <span className="text-purple-600">"{key}"</span>: {renderValue(val, depth + 1)}
                {idx < entries.length - 1 ? ',' : ''}
              </div>
            ))}
          </div>
          <span>{'}'}</span>
        </div>
      )
    }
    return <span>{String(value)}</span>
  }

  return (
    <div className={`p-4 bg-gray-50 rounded font-mono text-sm overflow-auto max-h-96 ${className}`}>
      {renderValue(data)}
    </div>
  )
}

