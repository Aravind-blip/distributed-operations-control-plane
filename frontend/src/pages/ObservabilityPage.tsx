import { useMemo } from 'react'
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { metricsApi } from '../api/metrics'
import { servicesApi } from '../api/services'
import { useAsync } from '../hooks/useAsync'
import { Card, EmptyState, PageError, PageHeader, Spinner, Table } from '../components/ui'
import type { TableColumn } from '../components/ui/Table'
import { ServiceStatusBadge } from '../components/ui/Badge'
import {
  simulateApiLatencySeries,
  simulateErrorRateSeries,
  simulateKafkaThroughputSeries,
} from '../utils/simulatedTimeSeries'
import type { DistributedService } from '../types/domain'
import { formatLatency, formatPercent, formatRelativeToNow } from '../utils/format'

export function ObservabilityPage() {
  const summaryState = useAsync(() => metricsApi.dashboardSummary(), [])
  const servicesState = useAsync(() => servicesApi.list({ limit: 200 }), [])

  const isLoading = summaryState.isLoading || servicesState.isLoading
  const error = summaryState.error || servicesState.error

  const latencySeries = useMemo(
    () => simulateApiLatencySeries(summaryState.data?.avgLatencyMs ?? 80),
    [summaryState.data],
  )
  const kafkaSeries = useMemo(
    () => simulateKafkaThroughputSeries(summaryState.data?.kafkaMessagesProcessed ?? 500_000),
    [summaryState.data],
  )
  const errorRateSeries = useMemo(
    () =>
      simulateErrorRateSeries(
        summaryState.data?.degradedServices ?? 0,
        summaryState.data?.totalServices ?? 1,
      ),
    [summaryState.data],
  )

  const services = servicesState.data?.items ?? []

  const columns = useMemo<TableColumn<DistributedService>[]>(
    () => [
      { key: 'name', header: 'Service', render: (row) => <span className="font-medium text-slate-100">{row.name}</span> },
      { key: 'status', header: 'Status', render: (row) => <ServiceStatusBadge status={row.status} /> },
      { key: 'latency', header: 'Latency', render: (row) => formatLatency(row.latencyMs) },
      { key: 'errorRate', header: 'Error Rate', render: (row) => formatPercent(row.errorRate) },
      { key: 'heartbeat', header: 'Heartbeat Age', render: (row) => formatRelativeToNow(row.lastHeartbeat) },
    ],
    [],
  )

  if (isLoading) return <Spinner label="Loading observability data…" />
  if (error) {
    return (
      <PageError
        message={error}
        onRetry={() => {
          summaryState.refetch()
          servicesState.refetch()
        }}
      />
    )
  }

  return (
    <div>
      <PageHeader
        title="Observability"
        description="Deep-dive into fleet-wide latency, throughput, and error trends."
      />

      <div className="mb-6 grid grid-cols-1 gap-4">
        <Card title="API Latency Over Time" description="Simulated, derived from current dashboard summary">
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={latencySeries}>
              <CartesianGrid strokeDasharray="3 3" stroke="#242e42" />
              <XAxis dataKey="label" stroke="#94a3b8" fontSize={12} />
              <YAxis stroke="#94a3b8" fontSize={12} />
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Line type="monotone" dataKey="value" name="Latency (ms)" stroke="#3b82f6" dot={false} strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Kafka Event Throughput" description="Simulated messages/min">
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={kafkaSeries}>
              <CartesianGrid strokeDasharray="3 3" stroke="#242e42" />
              <XAxis dataKey="label" stroke="#94a3b8" fontSize={12} />
              <YAxis stroke="#94a3b8" fontSize={12} />
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Line type="monotone" dataKey="value" name="msg/min" stroke="#a855f7" dot={false} strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Error Rate Trend" description="Simulated fleet-wide error rate percentage">
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={errorRateSeries}>
              <CartesianGrid strokeDasharray="3 3" stroke="#242e42" />
              <XAxis dataKey="label" stroke="#94a3b8" fontSize={12} />
              <YAxis stroke="#94a3b8" fontSize={12} unit="%" />
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Line type="monotone" dataKey="value" name="Error rate %" stroke="#f59e0b" dot={false} strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </Card>
      </div>

      <Card title="Per-Service Metrics">
        {services.length === 0 ? (
          <EmptyState title="No services to display" />
        ) : (
          <Table columns={columns} rows={services} getRowKey={(row) => row.id} />
        )}
      </Card>
    </div>
  )
}
