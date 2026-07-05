import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ServerCog } from 'lucide-react'
import { servicesApi } from '../api/services'
import { useAsync } from '../hooks/useAsync'
import { EnvironmentBadge, ServiceStatusBadge } from '../components/ui/Badge'
import { EmptyState, PageError, PageHeader, Select, Spinner, Table } from '../components/ui'
import type { TableColumn } from '../components/ui/Table'
import type { DistributedService, Environment, ServiceStatus } from '../types/domain'
import { formatLatency, formatPercent, formatRelativeToNow } from '../utils/format'

const ENVIRONMENT_OPTIONS = [
  { value: 'CLOUD', label: 'Cloud' },
  { value: 'EDGE', label: 'Edge' },
  { value: 'DATA_CENTER', label: 'Data Center' },
]

const STATUS_OPTIONS = [
  { value: 'HEALTHY', label: 'Healthy' },
  { value: 'DEGRADED', label: 'Degraded' },
  { value: 'OFFLINE', label: 'Offline' },
]

export function ServicesPage() {
  const navigate = useNavigate()
  const [environment, setEnvironment] = useState<Environment | ''>('')
  const [status, setStatus] = useState<ServiceStatus | ''>('')

  const { data, isLoading, error, refetch } = useAsync(
    () =>
      servicesApi.list({
        environment: environment || undefined,
        status: status || undefined,
        limit: 200,
      }),
    [environment, status],
  )

  const hasFilters = Boolean(environment || status)
  const services = data?.items ?? []

  const columns = useMemo<TableColumn<DistributedService>[]>(
    () => [
      { key: 'name', header: 'Name', render: (row) => <span className="font-medium text-slate-100">{row.name}</span> },
      { key: 'environment', header: 'Environment', render: (row) => <EnvironmentBadge environment={row.environment} /> },
      { key: 'region', header: 'Region', render: (row) => row.region },
      { key: 'status', header: 'Status', render: (row) => <ServiceStatusBadge status={row.status} /> },
      { key: 'latency', header: 'Latency', render: (row) => formatLatency(row.latencyMs) },
      { key: 'errorRate', header: 'Error Rate', render: (row) => formatPercent(row.errorRate) },
      { key: 'heartbeat', header: 'Last Heartbeat', render: (row) => formatRelativeToNow(row.lastHeartbeat) },
      { key: 'owner', header: 'Owner Team', render: (row) => row.ownerTeam },
    ],
    [],
  )

  return (
    <div>
      <PageHeader
        title="Distributed Services"
        description="Inventory of services running across cloud, edge, and data-center environments."
        count={services.length}
        actions={
          <div className="flex items-center gap-2">
            <Select
              options={ENVIRONMENT_OPTIONS}
              placeholder="All environments"
              value={environment}
              onChange={(event) => setEnvironment(event.target.value as Environment | '')}
            />
            <Select
              options={STATUS_OPTIONS}
              placeholder="All statuses"
              value={status}
              onChange={(event) => setStatus(event.target.value as ServiceStatus | '')}
            />
          </div>
        }
      />

      {isLoading ? (
        <Spinner label="Loading services…" />
      ) : error ? (
        <PageError message={error} onRetry={refetch} />
      ) : services.length === 0 ? (
        <EmptyState
          icon={<ServerCog size={28} />}
          title={hasFilters ? 'No services match your filters' : 'No services found'}
          description={
            hasFilters
              ? 'Try adjusting the environment or status filter.'
              : 'Registered services will appear here once available.'
          }
        />
      ) : (
        <div className="rounded-xl border border-surface-600 bg-surface-800">
          <Table
            columns={columns}
            rows={services}
            getRowKey={(row) => row.id}
            onRowClick={(row) => navigate(`/services/${row.id}`)}
          />
        </div>
      )}
    </div>
  )
}
