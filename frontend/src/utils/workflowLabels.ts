import type { WorkflowType } from '../types/domain'

export const WORKFLOW_TYPE_LABELS: Record<WorkflowType, string> = {
  RETRY_KAFKA_BATCH: 'Retry Failed Kafka Event Batch',
  APPROVE_CONFIG_ROLLOUT: 'Approve Config Rollout',
  RESTART_EDGE_GATEWAY: 'Restart Edge Gateway',
  VALIDATE_METERING_PIPELINE: 'Validate Metering Pipeline',
  INCIDENT_REVIEW: 'Incident Review',
  SCALE_K8S_DEPLOYMENT: 'Scale Kubernetes Deployment',
  VALIDATE_SOAP_ADAPTER_RECOVERY: 'Validate SOAP Adapter Recovery',
  RESTART_EMS_BRIDGE: 'Restart EMS Bridge',
}

export const WORKFLOW_TYPE_OPTIONS: { value: WorkflowType; label: string }[] = (
  Object.keys(WORKFLOW_TYPE_LABELS) as WorkflowType[]
).map((value) => ({ value, label: WORKFLOW_TYPE_LABELS[value] }))

export function workflowTypeLabel(type: WorkflowType): string {
  return WORKFLOW_TYPE_LABELS[type] ?? type
}
