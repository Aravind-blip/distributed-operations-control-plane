import { apiClient } from './client'
import type { LoginResponse, Role } from '../types/domain'

export interface MeResponse {
  email: string
  role: Role
}

export const authApi = {
  login: (email: string, password: string) => apiClient.post<LoginResponse>('/auth/login', { email, password }),
  me: () => apiClient.get<MeResponse>('/auth/me'),
}
