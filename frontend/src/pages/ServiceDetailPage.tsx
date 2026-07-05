import { useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, ShieldAlert } from 'lucide-react'
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { servicesApi } from '../api/services'
import { alertsApi } from '../api/alerts'
import { useAsync } from '../hooks/useAsync'
import { Card, EmptyState, PageError, PageHeader, Spinner } from '../components/ui'
import { EnvironmentBadge, SeverityBadge, ServiceStatusBadge, AlertStatusBadge } from '../components/ui/Badge'
import { formatDateTime, formatLatency, formatPercent, formatRelativeToNow } from '../utils/format'
import { simulateApiLatencySeries, simulateErrorRateSeries } from '../utils/simulatedTimeSeries'

export function ServiceDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const serviceState = useAsync(() => servicesApi.get(id ?? ''), [id])
  const alertsState = useAsync(() => alertsApi.list({ limit: 200 }), [id])

  const relatedAlerts = useMemo(
    () => (alertsState.data?.items ?? []).filter((alert) => alert.sourceServiceId === id),
    [alertsState.data, id],
  )

  const latencySeries = useMemo(
    () => simulateApiLatencySeries(serviceState.data?.latencyMs ?? 80),
    [serviceState.data],
  )
  const errorSeries = useMemo(
    () => simulateErrorRateSeries(serviceState.data?.status === 'HEALTHY' ? 0 : 1, 1),
    [serviceState.data],
  )

  if (serviceState.isLoading) return <Spinner label="Loading service…" />
  if (serviceState.error) return <PageError message={serviceState.error} onRetry={serviceState.refetch} />
  if (!serviceState.data) return <PageError message="Service not found." />

  const service = serviceState.data

  return (
    <div>
      <button
        type="button"
        onClick={() => navigate('/services')}
        className="mb-4 flex items-center gap-1.5 text-sm text-slate-400 transition hover:text-slate-200"
      >
        <ArrowLeft size={15} />
        Back to services
      </button>

      <PageHeader
        title={service.name}
        description={`${service.serviceType} · ${service.region}`}
        actions={
          <div className="flex gap-2">
            <EnvironmentBadge environment={service.environment} />
            <ServiceStatusBadge status={service.status} />
          </div>
        }
      />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card title="Service Metadata" className="lg:col-span-1">
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between">
              <dt className="text-slate-400">Owner team</dt>
              <dd className="text-slate-200">{service.ownerTeam}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-400">Version</dt>
              <dd className="font-mono text-slate-200">{service.version}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-400">Latency</dt>
              <dd className="text-slate-200">{formatLatency(service.latencyMs)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-400">Error rate</dt>
              <dd className="text-slate-200">{formatPercent(service.errorRate)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-400">Last heartbeat</dt>
              <dd className="text-slate-200">{formatRelativeToNow(service.lastHeartbeat)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-400">Created</dt>
              <dd className="text-slate-200">{formatDateTime(service.createdAt)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-400">Updated</dt>
              <dd className="text-slate-200">{formatDateTime(service.updatedAt)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-400">Service ID</dt>
              <dd className="font-mono text-xs text-slate-400">{service.id}</dd>
            </div>
          </dl>
        </Card>

        <Card title="Latency & Error Rate" description="Simulated recent trend" className="lg:col-span-2">
          <ResponsiveContainer width="100%" height={180}>
            <LineChart data={latencySeries}>
              <CartesianGrid strokeDasharray="3 3" stroke="#242e42" />
              <XAxis dataKey="label" stroke="#94a3b8" fontSize={11} interval={3} />
              <YAxis stroke="#94a3b8" fontSize={12} />
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Line type="monotone" dataKey="value" name="Latency (ms)" stroke="#3b82f6" dot={false} strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
          <ResponsiveContainer width="100%" height={140}>
            <LineChart data={errorSeries}>
              <CartesianGrid strokeDasharray="3 3" stroke="#242e42" />
              <XAxis dataKey="label" stroke="#94a3b8" fontSize={11} interval={3} />
              <YAxis stroke="#94a3b8" fontSize={12} unit="%" />
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Line type="monotone" dataKey="value" name="Error rate %" stroke="#f59e0b" dot={false} strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Related Alerts" className="lg:col-span-3">
          {alertsState.isLoading ? (
            <Spinner size={20} label="Loading alerts…" />
          ) : alertsState.error ? (
            <PageError message={alertsState.error} onRetry={alertsState.refetch} />
          ) : relatedAlerts.length === 0 ? (
            <EmptyState icon={<ShieldAlert size={24} />} title="No alerts for this service" />
          ) : (
            <ul className="divide-y divide-surface-700">
              {relatedAlerts.map((alert) => (
                <li key={alert.id} className="flex items-center justify-between gap-4 py-3">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium text-slate-200">{alert.title}</p>
                    <p className="truncate text-xs text-slate-500">{alert.description}</p>
                  </div>
                  <div className="flex shrink-0 items-center gap-2">
                    <SeverityBadge severity={alert.severity} />
                    <AlertStatusBadge status={alert.status} />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </div>
  )
}
