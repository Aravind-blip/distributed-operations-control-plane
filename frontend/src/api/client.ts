const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'

const TOKEN_KEY = 'docp.auth.token'

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setStoredToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearStoredToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

// A 401 means the token is missing/invalid/expired -- distinct from a 403,
// which means an authenticated user lacks permission for this action. Only
// a 401 should force a re-login; AuthContext listens for this event to log
// the user out and redirect to /login instead of leaving stale pages stuck
// showing repeated auth errors.
export const SESSION_EXPIRED_EVENT = 'docp:session-expired'

function notifySessionExpired(): void {
  window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT))
}

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PATCH' | 'DELETE'
  body?: unknown
  query?: object
}

function buildUrl(path: string, query?: object): string {
  const url = new URL(`${BASE_URL}${path}`, window.location.origin)
  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== '') {
        url.searchParams.set(key, String(value))
      }
    }
  }
  return url.toString()
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const token = getStoredToken()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(buildUrl(path, options.query), {
    method: options.method ?? 'GET',
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  })

  if (!response.ok) {
    let message = response.statusText
    try {
      const data: unknown = await response.json()
      if (data && typeof data === 'object' && 'message' in data) {
        const maybeMessage = (data as { message?: unknown }).message
        if (typeof maybeMessage === 'string') message = maybeMessage
      }
    } catch {
      // response body was not JSON; fall back to statusText
    }
    if (response.status === 401) {
      clearStoredToken()
      notifySessionExpired()
    }
    throw new ApiError(message || `Request failed with status ${response.status}`, response.status)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

export const apiClient = {
  get: <T>(path: string, query?: object) => request<T>(path, { method: 'GET', query }),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body }),
  patch: <T>(path: string, body?: unknown) => request<T>(path, { method: 'PATCH', body }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
}
