import { apiClient } from './client'
import { normalizeList, type SpringPage } from './normalize'
import type { Alert, AlertSeverity, AlertStatus, Paginated } from '../types/domain'

export interface AlertListParams {
  severity?: AlertSeverity
  status?: AlertStatus
  limit?: number
  offset?: number
}

export type AlertListResponse = Alert[] | Paginated<Alert> | SpringPage<Alert>

export const alertsApi = {
  list: async (params: AlertListParams = {}) => {
    const data = await apiClient.get<AlertListResponse>('/alerts', params)
    return normalizeList(data)
  },
  get: (id: string) => apiClient.get<Alert>(`/alerts/${id}`),
  acknowledge: (id: string) => apiClient.post<Alert>(`/alerts/${id}/acknowledge`),
  resolve: (id: string) => apiClient.post<Alert>(`/alerts/${id}/resolve`),
}
