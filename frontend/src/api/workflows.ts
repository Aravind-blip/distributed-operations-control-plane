import { apiClient } from './client'
import { normalizeList, type SpringPage } from './normalize'
import type { Paginated, RiskLevel, Workflow, WorkflowType } from '../types/domain'

export interface WorkflowListParams {
  limit?: number
  offset?: number
}

export interface CreateWorkflowPayload {
  workflowType: WorkflowType
  riskLevel: RiskLevel
}

export type WorkflowListResponse = Workflow[] | Paginated<Workflow> | SpringPage<Workflow>

export const workflowsApi = {
  list: async (params: WorkflowListParams = {}) => {
    const data = await apiClient.get<WorkflowListResponse>('/workflows', params)
    return normalizeList(data)
  },
  get: (id: string) => apiClient.get<Workflow>(`/workflows/${id}`),
  create: (payload: CreateWorkflowPayload) => apiClient.post<Workflow>('/workflows', payload),
  approve: (id: string) => apiClient.post<Workflow>(`/workflows/${id}/approve`),
  reject: (id: string) => apiClient.post<Workflow>(`/workflows/${id}/reject`),
}
