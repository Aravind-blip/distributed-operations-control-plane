import type { ReactNode } from 'react'

export interface TableColumn<T> {
  key: string
  header: string
  render: (row: T) => ReactNode
  className?: string
}

interface TableProps<T> {
  columns: TableColumn<T>[]
  rows: T[]
  getRowKey: (row: T) => string
  onRowClick?: (row: T) => void
}

export function Table<T>({ columns, rows, getRowKey, onRowClick }: TableProps<T>) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse text-sm">
        <thead>
          <tr className="border-b border-surface-600 text-left text-xs uppercase tracking-wide text-slate-400">
            {columns.map((column) => (
              <th key={column.key} className={`px-4 py-2.5 font-medium ${column.className ?? ''}`}>
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr
              key={getRowKey(row)}
              onClick={onRowClick ? () => onRowClick(row) : undefined}
              className={`border-b border-surface-700 last:border-b-0 ${
                onRowClick ? 'cursor-pointer transition hover:bg-surface-700/60' : ''
              }`}
            >
              {columns.map((column) => (
                <td key={column.key} className={`px-4 py-3 text-slate-200 ${column.className ?? ''}`}>
                  {column.render(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
