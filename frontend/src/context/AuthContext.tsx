import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '../api/auth'
import { clearStoredToken, getStoredToken, setStoredToken, SESSION_EXPIRED_EVENT } from '../api/client'
import type { Role } from '../types/domain'

const EMAIL_KEY = 'docp.auth.email'
const ROLE_KEY = 'docp.auth.role'

export interface AuthUser {
  email: string
  role: Role
}

interface AuthContextValue {
  token: string | null
  user: AuthUser | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function readStoredUser(): AuthUser | null {
  const email = localStorage.getItem(EMAIL_KEY)
  const role = localStorage.getItem(ROLE_KEY) as Role | null
  if (!email || !role) return null
  return { email, role }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getStoredToken())
  const [user, setUser] = useState<AuthUser | null>(() => readStoredUser())

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login(email, password)
    setStoredToken(response.token)
    localStorage.setItem(EMAIL_KEY, response.email)
    localStorage.setItem(ROLE_KEY, response.role)
    setToken(response.token)
    setUser({ email: response.email, role: response.role })
  }, [])

  const logout = useCallback(() => {
    clearStoredToken()
    localStorage.removeItem(EMAIL_KEY)
    localStorage.removeItem(ROLE_KEY)
    setToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    window.addEventListener(SESSION_EXPIRED_EVENT, logout)
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, logout)
  }, [logout])

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      login,
      logout,
    }),
    [token, user, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

export function hasRole(role: Role | undefined, allowed: Role[]): boolean {
  if (!role) return false
  return allowed.includes(role)
}
