import { apiClient } from './client'
import type { DashboardSummary } from '../types/domain'

export const metricsApi = {
  dashboardSummary: () => apiClient.get<DashboardSummary>('/metrics/dashboard-summary'),
}
