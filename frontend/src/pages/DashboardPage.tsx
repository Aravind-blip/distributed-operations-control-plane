import { useMemo } from 'react'
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  Gauge,
  MessageSquare,
  Server,
  ShieldAlert,
  Workflow as WorkflowIcon,
  XCircle,
} from 'lucide-react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { metricsApi } from '../api/metrics'
import { servicesApi } from '../api/services'
import { alertsApi } from '../api/alerts'
import { auditLogsApi } from '../api/auditLogs'
import { useAsync } from '../hooks/useAsync'
import { Card, EmptyState, PageError, PageHeader, Spinner } from '../components/ui'
import { formatLatency, formatNumber, formatRelativeToNow } from '../utils/format'
import {
  simulateApiLatencySeries,
  simulateErrorRateSeries,
  simulateKafkaThroughputSeries,
} from '../utils/simulatedTimeSeries'
import type { Environment, ServiceStatus } from '../types/domain'

const ENVIRONMENTS: Environment[] = ['CLOUD', 'EDGE', 'DATA_CENTER']
const STATUS_COLORS: Record<ServiceStatus, string> = {
  HEALTHY: '#22c55e',
  DEGRADED: '#f59e0b',
  OFFLINE: '#ef4444',
}
const SEVERITY_COLORS: Record<string, string> = {
  CRITICAL: '#ef4444',
  HIGH: '#f59e0b',
  MEDIUM: '#3b82f6',
  LOW: '#64748b',
}

function StatCard({
  label,
  value,
  icon: Icon,
  accent,
}: {
  label: string
  value: string
  icon: typeof Server
  accent: string
}) {
  return (
    <Card className="flex items-center gap-3">
      <div className={`rounded-lg p-2.5 ${accent}`}>
        <Icon size={18} />
      </div>
      <div>
        <p className="text-xs text-slate-400">{label}</p>
        <p className="text-lg font-semibold text-slate-100">{value}</p>
      </div>
    </Card>
  )
}

export function DashboardPage() {
  const summaryState = useAsync(() => metricsApi.dashboardSummary(), [])
  const servicesState = useAsync(() => servicesApi.list({ limit: 200 }), [])
  const alertsState = useAsync(() => alertsApi.list({ limit: 200 }), [])
  const auditState = useAsync(() => auditLogsApi.list({ limit: 8 }), [])

  const isLoading = summaryState.isLoading || servicesState.isLoading || alertsState.isLoading
  const error = summaryState.error || servicesState.error || alertsState.error

  const environmentBreakdown = useMemo(() => {
    const services = servicesState.data?.items ?? []
    return ENVIRONMENTS.map((environment) => {
      const inEnv = services.filter((service) => service.environment === environment)
      return {
        environment: environment.replace('_', ' '),
        HEALTHY: inEnv.filter((s) => s.status === 'HEALTHY').length,
        DEGRADED: inEnv.filter((s) => s.status === 'DEGRADED').length,
        OFFLINE: inEnv.filter((s) => s.status === 'OFFLINE').length,
      }
    })
  }, [servicesState.data])

  const statusPieData = useMemo(() => {
    if (!summaryState.data) return []
    return [
      { name: 'Healthy', value: summaryState.data.healthyServices, status: 'HEALTHY' as ServiceStatus },
      { name: 'Degraded', value: summaryState.data.degradedServices, status: 'DEGRADED' as ServiceStatus },
      { name: 'Offline', value: summaryState.data.offlineServices, status: 'OFFLINE' as ServiceStatus },
    ]
  }, [summaryState.data])

  const alertsBySeverity = useMemo(() => {
    const alerts = alertsState.data?.items ?? []
    const severities = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'] as const
    return severities.map((severity) => ({
      severity,
      count: alerts.filter((alert) => alert.severity === severity).length,
    }))
  }, [alertsState.data])

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

  if (isLoading) return <Spinner label="Loading dashboard…" />
  if (error) {
    return (
      <PageError
        message={error}
        onRetry={() => {
          summaryState.refetch()
          servicesState.refetch()
          alertsState.refetch()
          auditState.refetch()
        }}
      />
    )
  }

  const summary = summaryState.data

  return (
    <div>
      <PageHeader
        title="Dashboard"
        description="Fleet-wide health across cloud, edge, and data-center environments."
      />

      <div className="mb-6 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
        <StatCard
          label="Total Services"
          value={formatNumber(summary?.totalServices ?? 0)}
          icon={Server}
          accent="bg-blue-500/15 text-blue-400"
        />
        <StatCard
          label="Healthy"
          value={formatNumber(summary?.healthyServices ?? 0)}
          icon={CheckCircle2}
          accent="bg-emerald-500/15 text-emerald-400"
        />
        <StatCard
          label="Degraded"
          value={formatNumber(summary?.degradedServices ?? 0)}
          icon={AlertTriangle}
          accent="bg-amber-500/15 text-amber-400"
        />
        <StatCard
          label="Offline"
          value={formatNumber(summary?.offlineServices ?? 0)}
          icon={XCircle}
          accent="bg-red-500/15 text-red-400"
        />
        <StatCard
          label="Active Critical Alerts"
          value={formatNumber(summary?.activeCriticalAlerts ?? 0)}
          icon={ShieldAlert}
          accent="bg-red-500/15 text-red-400"
        />
        <StatCard
          label="Avg API Latency"
          value={formatLatency(summary?.avgLatencyMs ?? 0)}
          icon={Gauge}
          accent="bg-blue-500/15 text-blue-400"
        />
        <StatCard
          label="Kafka Messages Processed"
          value={formatNumber(summary?.kafkaMessagesProcessed ?? 0)}
          icon={MessageSquare}
          accent="bg-purple-500/15 text-purple-400"
        />
        <StatCard
          label="Open Workflows"
          value={formatNumber(summary?.openWorkflows ?? 0)}
          icon={WorkflowIcon}
          accent="bg-blue-500/15 text-blue-400"
        />
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card title="Service Health by Environment">
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={environmentBreakdown}>
              <CartesianGrid strokeDasharray="3 3" stroke="#242e42" />
              <XAxis dataKey="environment" stroke="#94a3b8" fontSize={12} />
              <YAxis stroke="#94a3b8" fontSize={12} allowDecimals={false} />
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Legend />
              <Bar dataKey="HEALTHY" stackId="a" fill={STATUS_COLORS.HEALTHY} name="Healthy" />
              <Bar dataKey="DEGRADED" stackId="a" fill={STATUS_COLORS.DEGRADED} name="Degraded" />
              <Bar dataKey="OFFLINE" stackId="a" fill={STATUS_COLORS.OFFLINE} name="Offline" />
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Overall Service Status">
          <ResponsiveContainer width="100%" height={260}>
            <PieChart>
              <Pie data={statusPieData} dataKey="value" nameKey="name" innerRadius={55} outerRadius={90}>
                {statusPieData.map((entry) => (
                  <Cell key={entry.status} fill={STATUS_COLORS[entry.status]} />
                ))}
              </Pie>
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Alert Volume by Severity">
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={alertsBySeverity}>
              <CartesianGrid strokeDasharray="3 3" stroke="#242e42" />
              <XAxis dataKey="severity" stroke="#94a3b8" fontSize={12} />
              <YAxis stroke="#94a3b8" fontSize={12} allowDecimals={false} />
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Bar dataKey="count" name="Alerts" radius={[4, 4, 0, 0]}>
                {alertsBySeverity.map((entry) => (
                  <Cell key={entry.severity} fill={SEVERITY_COLORS[entry.severity]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Recent Operational Events" description="Latest audit log activity">
          {auditState.isLoading ? (
            <Spinner size={20} label="Loading events…" />
          ) : auditState.error ? (
            <PageError message={auditState.error} onRetry={auditState.refetch} />
          ) : (auditState.data?.items.length ?? 0) === 0 ? (
            <EmptyState title="No recent events" description="Audit activity will appear here as it happens." />
          ) : (
            <ul className="max-h-[240px] space-y-3 overflow-y-auto pr-1">
              {auditState.data?.items.map((log) => (
                <li key={log.id} className="flex items-start gap-3 text-sm">
                  <Activity size={14} className="mt-1 shrink-0 text-blue-400" />
                  <div className="min-w-0">
                    <p className="truncate text-slate-200">
                      <span className="font-medium">{log.actor}</span> {log.action.toLowerCase()}{' '}
                      <span className="text-slate-400">
                        {log.resourceType} #{log.resourceId}
                      </span>
                    </p>
                    <p className="text-xs text-slate-500">{formatRelativeToNow(log.createdAt)}</p>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card
          title="API Latency Over Time"
          description="Simulated rolling window, derived from current average latency"
          className="lg:col-span-2"
        >
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={latencySeries}>
              <CartesianGrid strokeDasharray="3 3" stroke="#242e42" />
              <XAxis dataKey="label" stroke="#94a3b8" fontSize={11} interval={3} />
              <YAxis stroke="#94a3b8" fontSize={12} />
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Line type="monotone" dataKey="value" name="Latency (ms)" stroke="#3b82f6" dot={false} strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        <Card
          title="Kafka Event Throughput"
          description="Simulated messages/min, derived from cumulative processed count"
        >
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={kafkaSeries}>
              <CartesianGrid strokeDasharray="3 3" stroke="#242e42" />
              <XAxis dataKey="label" stroke="#94a3b8" fontSize={11} interval={3} />
              <YAxis stroke="#94a3b8" fontSize={12} />
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Line type="monotone" dataKey="value" name="msg/min" stroke="#a855f7" dot={false} strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        <Card
          title="Error Rate Trend"
          description="Simulated fleet-wide error rate percentage"
        >
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={errorRateSeries}>
              <CartesianGrid strokeDasharray="3 3" stroke="#242e42" />
              <XAxis dataKey="label" stroke="#94a3b8" fontSize={11} interval={3} />
              <YAxis stroke="#94a3b8" fontSize={12} unit="%" />
              <Tooltip contentStyle={{ background: '#111827', border: '1px solid #33405a' }} />
              <Line type="monotone" dataKey="value" name="Error rate %" stroke="#f59e0b" dot={false} strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </Card>
      </div>
    </div>
  )
}
