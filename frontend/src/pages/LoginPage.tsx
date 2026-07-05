import { useState } from 'react'
import type { FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { Activity, LogIn } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { Button } from '../components/ui'
import { ApiError } from '../api/client'

export function LoginPage() {
  const { login } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const redirectTo = (location.state as { from?: { pathname: string } } | null)?.from?.pathname ?? '/dashboard'

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setIsSubmitting(true)
    try {
      await login(email, password)
      showToast({ variant: 'success', title: 'Signed in', description: `Welcome back, ${email}` })
      navigate(redirectTo, { replace: true })
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Unable to sign in. Check your credentials.'
      showToast({ variant: 'error', title: 'Login failed', description: message })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-surface-950 px-4">
      <div className="w-full max-w-sm rounded-xl border border-surface-600 bg-surface-800 p-8 shadow-panel">
        <div className="mb-6 flex flex-col items-center gap-2 text-center">
          <div className="rounded-lg bg-blue-600/15 p-2.5 text-blue-400">
            <Activity size={26} />
          </div>
          <h1 className="text-lg font-semibold text-slate-100">Distributed Operations Control Plane</h1>
          <p className="text-sm text-slate-400">Sign in to monitor services across your fleet.</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="email" className="mb-1 block text-xs font-medium text-slate-400">
              Email
            </label>
            <input
              id="email"
              type="email"
              required
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className="w-full rounded-md border border-surface-500 bg-surface-700 px-3 py-2 text-sm text-slate-100 outline-none focus:border-blue-500"
              placeholder="you@company.com"
            />
          </div>
          <div>
            <label htmlFor="password" className="mb-1 block text-xs font-medium text-slate-400">
              Password
            </label>
            <input
              id="password"
              type="password"
              required
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="w-full rounded-md border border-surface-500 bg-surface-700 px-3 py-2 text-sm text-slate-100 outline-none focus:border-blue-500"
              placeholder="••••••••"
            />
          </div>

          <Button type="submit" className="w-full" isLoading={isSubmitting} icon={<LogIn size={15} />}>
            Sign in
          </Button>
        </form>
        <p className="mt-6 text-center text-xs text-slate-500">
          Simulated portfolio project — no real infrastructure or customer data.
        </p>
      </div>
    </div>
  )
}
