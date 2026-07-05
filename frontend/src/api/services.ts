import { apiClient } from './client'
import { normalizeList, type SpringPage } from './normalize'
import type { DistributedService, Environment, Paginated, ServiceStatus } from '../types/domain'

export interface ServiceListParams {
  environment?: Environment
  status?: ServiceStatus
  limit?: number
  offset?: number
}

export type ServiceListResponse = DistributedService[] | Paginated<DistributedService> | SpringPage<DistributedService>

export const servicesApi = {
  list: async (params: ServiceListParams = {}) => {
    const data = await apiClient.get<ServiceListResponse>('/services', params)
    return normalizeList(data)
  },
  get: (id: string) => apiClient.get<DistributedService>(`/services/${id}`),
  create: (payload: Partial<DistributedService>) => apiClient.post<DistributedService>('/services', payload),
  update: (id: string, payload: Partial<DistributedService>) =>
    apiClient.patch<DistributedService>(`/services/${id}`, payload),
  remove: (id: string) => apiClient.delete<void>(`/services/${id}`),
}
