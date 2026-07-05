import type { ReactNode } from 'react'

interface PageHeaderProps {
  title: string
  description?: string
  count?: number
  actions?: ReactNode
}

export function PageHeader({ title, description, count, actions }: PageHeaderProps) {
  return (
    <div className="mb-6 flex flex-wrap items-start justify-between gap-4">
      <div>
        <div className="flex items-center gap-2.5">
          <h1 className="text-xl font-semibold text-slate-100">{title}</h1>
          {typeof count === 'number' && (
            <span className="rounded-full bg-surface-700 px-2 py-0.5 text-xs font-medium text-slate-300">
              {count}
            </span>
          )}
        </div>
        {description && <p className="mt-1 text-sm text-slate-400">{description}</p>}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  )
}
