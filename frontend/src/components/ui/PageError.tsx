import { RefreshCw, TriangleAlert } from 'lucide-react'
import { Button } from './Button'

interface PageErrorProps {
  message?: string
  onRetry?: () => void
}

export function PageError({ message = 'Something went wrong while loading this data.', onRetry }: PageErrorProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-xl border border-red-500/20 bg-red-500/5 py-16 text-center">
      <TriangleAlert className="text-red-400" size={28} />
      <p className="max-w-sm text-sm text-slate-300">{message}</p>
      {onRetry && (
        <Button variant="secondary" size="sm" icon={<RefreshCw size={14} />} onClick={onRetry}>
          Retry
        </Button>
      )}
    </div>
  )
}
