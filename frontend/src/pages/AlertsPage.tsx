import { useMemo, useState } from 'react'
import { ShieldAlert } from 'lucide-react'
import { alertsApi } from '../api/alerts'
import { useAsync } from '../hooks/useAsync'
import { useAuth, hasRole } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { AlertStatusBadge, SeverityBadge } from '../components/ui/Badge'
import { Button, EmptyState, PageError, PageHeader, Select, Spinner, Table } from '../components/ui'
import type { TableColumn } from '../components/ui/Table'
import type { Alert, AlertSeverity, AlertStatus } from '../types/domain'
import { ApiError } from '../api/client'
import { formatRelativeToNow } from '../utils/format'

const SEVERITY_OPTIONS = [
  { value: 'CRITICAL', label: 'Critical' },
  { value: 'HIGH', label: 'High' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'LOW', label: 'Low' },
]

const STATUS_OPTIONS = [
  { value: 'OPEN', label: 'Open' },
  { value: 'ACKNOWLEDGED', label: 'Acknowledged' },
  { value: 'RESOLVED', label: 'Resolved' },
]

export function AlertsPage() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const canAct = hasRole(user?.role, ['OPERATOR', 'ADMIN'])

  const [severity, setSeverity] = useState<AlertSeverity | ''>('')
  const [status, setStatus] = useState<AlertStatus | ''>('')
  const [pendingId, setPendingId] = useState<string | null>(null)

  const { data, isLoading, error, refetch } = useAsync(
    () =>
      alertsApi.list({
        severity: severity || undefined,
        status: status || undefined,
        limit: 200,
      }),
    [severity, status],
  )

  const alerts = data?.items ?? []
  const hasFilters = Boolean(severity || status)

  async function handleAcknowledge(alert: Alert) {
    setPendingId(alert.id)
    try {
      await alertsApi.acknowledge(alert.id)
      showToast({ variant: 'success', title: 'Alert acknowledged', description: alert.title })
      refetch()
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to acknowledge alert.'
      showToast({ variant: 'error', title: 'Action failed', description: message })
    } finally {
      setPendingId(null)
    }
  }

  async function handleResolve(alert: Alert) {
    setPendingId(alert.id)
    try {
      await alertsApi.resolve(alert.id)
      showToast({ variant: 'success', title: 'Alert resolved', description: alert.title })
      refetch()
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to resolve alert.'
      showToast({ variant: 'error', title: 'Action failed', description: message })
    } finally {
      setPendingId(null)
    }
  }

  const columns = useMemo<TableColumn<Alert>[]>(
    () => [
      { key: 'severity', header: 'Severity', render: (row) => <SeverityBadge severity={row.severity} /> },
      { key: 'title', header: 'Title', render: (row) => <span className="font-medium text-slate-100">{row.title}</span> },
      {
        key: 'description',
        header: 'Description',
        render: (row) => <span className="text-slate-400">{row.description}</span>,
        className: 'max-w-xs truncate',
      },
      { key: 'status', header: 'Status', render: (row) => <AlertStatusBadge status={row.status} /> },
      { key: 'created', header: 'Created', render: (row) => formatRelativeToNow(row.createdAt) },
      {
        key: 'actions',
        header: 'Actions',
        render: (row) => {
          const disabledReason = !canAct ? 'Viewers have read-only access' : undefined
          const isPending = pendingId === row.id
          return (
            <div className="flex gap-2" title={disabledReason}>
              <Button
                size="sm"
                variant="secondary"
                disabled={!canAct || row.status !== 'OPEN' || isPending}
                isLoading={isPending}
                onClick={() => handleAcknowledge(row)}
              >
                Acknowledge
              </Button>
              <Button
                size="sm"
                variant="primary"
                disabled={!canAct || row.status === 'RESOLVED' || isPending}
                isLoading={isPending}
                onClick={() => handleResolve(row)}
              >
                Resolve
              </Button>
            </div>
          )
        },
      },
    ],
    [canAct, pendingId],
  )

  return (
    <div>
      <PageHeader
        title="Alerts Center"
        description="Active and historical alerts raised across the service fleet."
        count={alerts.length}
        actions={
          <div className="flex items-center gap-2">
            <Select
              options={SEVERITY_OPTIONS}
              placeholder="All severities"
              value={severity}
              onChange={(event) => setSeverity(event.target.value as AlertSeverity | '')}
            />
            <Select
              options={STATUS_OPTIONS}
              placeholder="All statuses"
              value={status}
              onChange={(event) => setStatus(event.target.value as AlertStatus | '')}
            />
          </div>
        }
      />

      {isLoading ? (
        <Spinner label="Loading alerts…" />
      ) : error ? (
        <PageError message={error} onRetry={refetch} />
      ) : alerts.length === 0 ? (
        <EmptyState
          icon={<ShieldAlert size={28} />}
          title={hasFilters ? 'No alerts match your filters' : 'No alerts'}
          description={hasFilters ? 'Try a different severity or status filter.' : 'The fleet currently has no alerts.'}
        />
      ) : (
        <div className="rounded-xl border border-surface-600 bg-surface-800">
          <Table columns={columns} rows={alerts} getRowKey={(row) => row.id} />
        </div>
      )}
    </div>
  )
}
