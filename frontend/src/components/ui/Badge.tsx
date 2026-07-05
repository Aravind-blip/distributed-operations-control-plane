import type { ReactNode } from 'react'
import type { AlertSeverity, AlertStatus, Environment, RiskLevel, ServiceStatus, WorkflowStatus } from '../../types/domain'

type Tone = 'green' | 'amber' | 'red' | 'blue' | 'slate' | 'purple'

const TONE_CLASSES: Record<Tone, string> = {
  green: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30',
  amber: 'bg-amber-500/15 text-amber-300 border-amber-500/30',
  red: 'bg-red-500/15 text-red-300 border-red-500/30',
  blue: 'bg-blue-500/15 text-blue-300 border-blue-500/30',
  slate: 'bg-slate-500/15 text-slate-300 border-slate-500/30',
  purple: 'bg-purple-500/15 text-purple-300 border-purple-500/30',
}

export function Badge({ tone = 'slate', children }: { tone?: Tone; children: ReactNode }) {
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs font-medium ${TONE_CLASSES[tone]}`}
    >
      {children}
    </span>
  )
}

const SERVICE_STATUS_TONE: Record<ServiceStatus, Tone> = {
  HEALTHY: 'green',
  DEGRADED: 'amber',
  OFFLINE: 'red',
}

export function ServiceStatusBadge({ status }: { status: ServiceStatus }) {
  return <Badge tone={SERVICE_STATUS_TONE[status]}>{status}</Badge>
}

const ENVIRONMENT_TONE: Record<Environment, Tone> = {
  CLOUD: 'blue',
  EDGE: 'purple',
  DATA_CENTER: 'slate',
}

export function EnvironmentBadge({ environment }: { environment: Environment }) {
  return <Badge tone={ENVIRONMENT_TONE[environment]}>{environment.replace('_', ' ')}</Badge>
}

const SEVERITY_TONE: Record<AlertSeverity, Tone> = {
  CRITICAL: 'red',
  HIGH: 'amber',
  MEDIUM: 'blue',
  LOW: 'slate',
}

export function SeverityBadge({ severity }: { severity: AlertSeverity }) {
  return <Badge tone={SEVERITY_TONE[severity]}>{severity}</Badge>
}

const ALERT_STATUS_TONE: Record<AlertStatus, Tone> = {
  OPEN: 'red',
  ACKNOWLEDGED: 'amber',
  RESOLVED: 'green',
}

export function AlertStatusBadge({ status }: { status: AlertStatus }) {
  return <Badge tone={ALERT_STATUS_TONE[status]}>{status}</Badge>
}

const WORKFLOW_STATUS_TONE: Record<WorkflowStatus, Tone> = {
  REQUESTED: 'blue',
  APPROVED: 'green',
  REJECTED: 'red',
  COMPLETED: 'slate',
}

export function WorkflowStatusBadge({ status }: { status: WorkflowStatus }) {
  return <Badge tone={WORKFLOW_STATUS_TONE[status]}>{status}</Badge>
}

const RISK_TONE: Record<RiskLevel, Tone> = {
  LOW: 'green',
  MEDIUM: 'amber',
  HIGH: 'red',
}

export function RiskBadge({ risk }: { risk: RiskLevel }) {
  return <Badge tone={RISK_TONE[risk]}>{risk}</Badge>
}
