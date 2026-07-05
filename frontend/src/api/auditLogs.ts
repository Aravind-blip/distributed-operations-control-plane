import { apiClient } from './client'
import { normalizeList, type SpringPage } from './normalize'
import type { AuditLog, Paginated } from '../types/domain'

export interface AuditLogListParams {
  limit?: number
  offset?: number
}

export type AuditLogListResponse = AuditLog[] | Paginated<AuditLog> | SpringPage<AuditLog>

export const auditLogsApi = {
  list: async (params: AuditLogListParams = {}) => {
    const data = await apiClient.get<AuditLogListResponse>('/audit-logs', params)
    return normalizeList(data)
  },
}
