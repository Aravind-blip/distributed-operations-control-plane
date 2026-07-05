import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { ChevronDown, ChevronRight, ClipboardList, Plus } from 'lucide-react'
import { workflowsApi } from '../api/workflows'
import { useAsync } from '../hooks/useAsync'
import { useAuth, hasRole } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { RiskBadge, WorkflowStatusBadge } from '../components/ui/Badge'
import { Button, EmptyState, Modal, PageError, PageHeader, Select, Spinner } from '../components/ui'
import type { RiskLevel, Workflow, WorkflowType } from '../types/domain'
import { ApiError } from '../api/client'
import { formatDateTime } from '../utils/format'
import { WORKFLOW_TYPE_OPTIONS, workflowTypeLabel } from '../utils/workflowLabels'

const RISK_OPTIONS = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
]

export function WorkflowsPage() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const canRequest = hasRole(user?.role, ['OPERATOR', 'ADMIN'])
  const isAdmin = hasRole(user?.role, ['ADMIN'])

  const { data, isLoading, error, refetch } = useAsync(() => workflowsApi.list({ limit: 200 }), [])

  const [isModalOpen, setIsModalOpen] = useState(false)
  const [workflowType, setWorkflowType] = useState<WorkflowType>('RETRY_KAFKA_BATCH')
  const [riskLevel, setRiskLevel] = useState<RiskLevel>('LOW')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [pendingId, setPendingId] = useState<string | null>(null)
  const [expandedId, setExpandedId] = useState<string | null>(null)

  const workflows = data?.items ?? []

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    setIsSubmitting(true)
    try {
      await workflowsApi.create({ workflowType, riskLevel })
      showToast({ variant: 'success', title: 'Workflow requested', description: workflowTypeLabel(workflowType) })
      setIsModalOpen(false)
      refetch()
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to submit workflow request.'
      showToast({ variant: 'error', title: 'Request failed', description: message })
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleApprove(workflow: Workflow) {
    setPendingId(workflow.id)
    try {
      await workflowsApi.approve(workflow.id)
      showToast({ variant: 'success', title: 'Workflow approved', description: workflowTypeLabel(workflow.workflowType) })
      refetch()
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to approve workflow.'
      showToast({ variant: 'error', title: 'Action failed', description: message })
    } finally {
      setPendingId(null)
    }
  }

  async function handleReject(workflow: Workflow) {
    setPendingId(workflow.id)
    try {
      await workflowsApi.reject(workflow.id)
      showToast({ variant: 'success', title: 'Workflow rejected', description: workflowTypeLabel(workflow.workflowType) })
      refetch()
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to reject workflow.'
      showToast({ variant: 'error', title: 'Action failed', description: message })
    } finally {
      setPendingId(null)
    }
  }

  const sortedWorkflows = useMemo(
    () => [...workflows].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()),
    [workflows],
  )

  return (
    <div>
      <PageHeader
        title="Workflow Approvals"
        description="Operational workflow requests and their approval lifecycle."
        count={workflows.length}
        actions={
          canRequest ? (
            <Button icon={<Plus size={15} />} onClick={() => setIsModalOpen(true)}>
              New Workflow Request
            </Button>
          ) : undefined
        }
      />

      {isLoading ? (
        <Spinner label="Loading workflows…" />
      ) : error ? (
        <PageError message={error} onRetry={refetch} />
      ) : workflows.length === 0 ? (
        <EmptyState
          icon={<ClipboardList size={28} />}
          title="No workflow requests"
          description="Operational workflow requests will appear here once submitted."
        />
      ) : (
        <div className="space-y-3">
          {sortedWorkflows.map((workflow) => {
            const isExpanded = expandedId === workflow.id
            const isPending = pendingId === workflow.id
            return (
              <div key={workflow.id} className="rounded-xl border border-surface-600 bg-surface-800">
                <div className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
                  <button
                    type="button"
                    onClick={() => setExpandedId(isExpanded ? null : workflow.id)}
                    className="flex items-center gap-2 text-left"
                  >
                    {isExpanded ? (
                      <ChevronDown size={16} className="text-slate-400" />
                    ) : (
                      <ChevronRight size={16} className="text-slate-400" />
                    )}
                    <div>
                      <p className="text-sm font-medium text-slate-100">{workflowTypeLabel(workflow.workflowType)}</p>
                      <p className="text-xs text-slate-500">
                        Requested by {workflow.requester} · {formatDateTime(workflow.createdAt)}
                      </p>
                    </div>
                  </button>

                  <div className="flex items-center gap-2">
                    <RiskBadge risk={workflow.riskLevel} />
                    <WorkflowStatusBadge status={workflow.status} />
                    {isAdmin && workflow.status === 'REQUESTED' && (
                      <>
                        <Button
                          size="sm"
                          variant="secondary"
                          disabled={isPending}
                          isLoading={isPending}
                          onClick={() => handleApprove(workflow)}
                        >
                          Approve
                        </Button>
                        <Button
                          size="sm"
                          variant="danger"
                          disabled={isPending}
                          isLoading={isPending}
                          onClick={() => handleReject(workflow)}
                        >
                          Reject
                        </Button>
                      </>
                    )}
                  </div>
                </div>

                {isExpanded && (
                  <div className="border-t border-surface-600 px-4 py-3">
                    <p className="mb-2 text-xs font-medium uppercase tracking-wide text-slate-500">Audit Trail</p>
                    {workflow.auditTrail.length === 0 ? (
                      <p className="text-xs text-slate-500">No audit entries yet.</p>
                    ) : (
                      <ul className="space-y-1.5 text-xs text-slate-300">
                        {workflow.auditTrail.map((entry, index) => (
                          <li key={index} className="flex gap-2">
                            <span className="text-slate-600">{index + 1}.</span>
                            <span>{entry}</span>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="New Workflow Request"
        footer={
          <>
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button form="new-workflow-form" type="submit" isLoading={isSubmitting}>
              Submit Request
            </Button>
          </>
        }
      >
        <form id="new-workflow-form" onSubmit={handleCreate} className="space-y-4">
          <div>
            <label htmlFor="workflowType" className="mb-1 block text-xs font-medium text-slate-400">
              Workflow Type
            </label>
            <Select
              id="workflowType"
              className="w-full"
              options={WORKFLOW_TYPE_OPTIONS}
              value={workflowType}
              onChange={(event) => setWorkflowType(event.target.value as WorkflowType)}
            />
          </div>
          <div>
            <label htmlFor="riskLevel" className="mb-1 block text-xs font-medium text-slate-400">
              Risk Level
            </label>
            <Select
              id="riskLevel"
              className="w-full"
              options={RISK_OPTIONS}
              value={riskLevel}
              onChange={(event) => setRiskLevel(event.target.value as RiskLevel)}
            />
          </div>
        </form>
      </Modal>
    </div>
  )
}
