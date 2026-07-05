import type { ReactNode } from 'react'

interface CardProps {
  children: ReactNode
  className?: string
  title?: string
  description?: string
  actions?: ReactNode
}

export function Card({ children, className = '', title, description, actions }: CardProps) {
  return (
    <div className={`rounded-xl border border-surface-600 bg-surface-800 shadow-panel ${className}`}>
      {(title || actions) && (
        <div className="flex items-center justify-between border-b border-surface-600 px-4 py-3">
          <div>
            {title && <h3 className="text-sm font-semibold text-slate-100">{title}</h3>}
            {description && <p className="mt-0.5 text-xs text-slate-400">{description}</p>}
          </div>
          {actions}
        </div>
      )}
      <div className="p-4">{children}</div>
    </div>
  )
}
