interface SpinnerProps {
  size?: number
  label?: string
}

export function Spinner({ size = 24, label = 'Loading…' }: SpinnerProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-12 text-slate-400">
      <span
        className="animate-spin rounded-full border-2 border-slate-600 border-t-blue-500"
        style={{ width: size, height: size }}
        role="status"
        aria-label={label}
      />
      <span className="text-sm">{label}</span>
    </div>
  )
}
