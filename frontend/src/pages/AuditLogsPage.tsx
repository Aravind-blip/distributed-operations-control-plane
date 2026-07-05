import { useMemo, useState } from 'react'
import { ScrollText } from 'lucide-react'
import { auditLogsApi } from '../api/auditLogs'
import { useAsync } from '../hooks/useAsync'
import { EmptyState, PageError, PageHeader, Select, Spinner, Table } from '../components/ui'
import type { TableColumn } from '../components/ui/Table'
import type { AuditLog } from '../types/domain'
import { formatDateTime } from '../utils/format'

export function AuditLogsPage() {
  const { data, isLoading, error, refetch } = useAsync(() => auditLogsApi.list({ limit: 200 }), [])
  const [actionFilter, setActionFilter] = useState('')

  const logs = data?.items ?? []

  const actionOptions = useMemo(() => {
    const unique = Array.from(new Set(logs.map((log) => log.action)))
    return unique.sort().map((action) => ({ value: action, label: action }))
  }, [logs])

  const filteredLogs = useMemo(
    () => (actionFilter ? logs.filter((log) => log.action === actionFilter) : logs),
    [logs, actionFilter],
  )

  const columns = useMemo<TableColumn<AuditLog>[]>(
    () => [
      { key: 'createdAt', header: 'Timestamp', render: (row) => formatDateTime(row.createdAt) },
      { key: 'actor', header: 'Actor', render: (row) => <span className="font-medium text-slate-100">{row.actor}</span> },
      { key: 'action', header: 'Action', render: (row) => row.action },
      { key: 'resourceType', header: 'Resource Type', render: (row) => row.resourceType },
      { key: 'resourceId', header: 'Resource ID', render: (row) => <span className="font-mono text-xs">{row.resourceId}</span> },
      { key: 'metadata', header: 'Metadata', render: (row) => <span className="text-slate-400">{row.metadata}</span>, className: 'max-w-xs truncate' },
    ],
    [],
  )

  return (
    <div>
      <PageHeader
        title="Audit Logs"
        description="Full history of user and system actions across the control plane."
        count={filteredLogs.length}
        actions={
          actionOptions.length > 0 ? (
            <Select
              options={actionOptions}
              placeholder="All actions"
              value={actionFilter}
              onChange={(event) => setActionFilter(event.target.value)}
            />
          ) : undefined
        }
      />

      {isLoading ? (
        <Spinner label="Loading audit logs…" />
      ) : error ? (
        <PageError message={error} onRetry={refetch} />
      ) : filteredLogs.length === 0 ? (
        <EmptyState
          icon={<ScrollText size={28} />}
          title={actionFilter ? 'No logs match this action type' : 'No audit logs yet'}
          description={actionFilter ? 'Try a different action filter.' : 'Actions taken across the platform will appear here.'}
        />
      ) : (
        <div className="rounded-xl border border-surface-600 bg-surface-800">
          <Table columns={columns} rows={filteredLogs} getRowKey={(row) => row.id} />
        </div>
      )}
    </div>
  )
}
