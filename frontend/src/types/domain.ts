export type Role = 'VIEWER' | 'OPERATOR' | 'ADMIN'

export type Environment = 'CLOUD' | 'EDGE' | 'DATA_CENTER'

export type ServiceStatus = 'HEALTHY' | 'DEGRADED' | 'OFFLINE'

export interface DistributedService {
  id: string
  name: string
  environment: Environment
  region: string
  status: ServiceStatus
  latencyMs: number
  errorRate: number
  lastHeartbeat: string
  version: string
  ownerTeam: string
  serviceType: string
  createdAt: string
  updatedAt: string
}

export type AlertSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'

export type AlertStatus = 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED'

export interface Alert {
  id: string
  severity: AlertSeverity
  sourceServiceId: string
  title: string
  description: string
  status: AlertStatus
  createdAt: string
  acknowledgedBy: string | null
  resolvedBy: string | null
  resolvedAt: string | null
}

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'

export type WorkflowStatus = 'REQUESTED' | 'APPROVED' | 'REJECTED' | 'COMPLETED'

export type WorkflowType =
  | 'RETRY_KAFKA_BATCH'
  | 'APPROVE_CONFIG_ROLLOUT'
  | 'RESTART_EDGE_GATEWAY'
  | 'VALIDATE_METERING_PIPELINE'
  | 'INCIDENT_REVIEW'
  | 'SCALE_K8S_DEPLOYMENT'
  | 'VALIDATE_SOAP_ADAPTER_RECOVERY'
  | 'RESTART_EMS_BRIDGE'

export interface Workflow {
  id: string
  workflowType: WorkflowType
  requester: string
  approver: string | null
  riskLevel: RiskLevel
  status: WorkflowStatus
  createdAt: string
  updatedAt: string
  auditTrail: string[]
}

export interface AuditLog {
  id: string
  actor: string
  action: string
  resourceType: string
  resourceId: string
  metadata: string
  createdAt: string
}

export interface DashboardSummary {
  totalServices: number
  healthyServices: number
  degradedServices: number
  offlineServices: number
  activeCriticalAlerts: number
  avgLatencyMs: number
  kafkaMessagesProcessed: number
  openWorkflows: number
}

export interface LoginResponse {
  token: string
  email: string
  role: Role
}

export interface Paginated<T> {
  items: T[]
  total: number
}
