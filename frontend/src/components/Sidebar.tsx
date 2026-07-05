import { NavLink } from 'react-router-dom'
import {
  Activity,
  ClipboardList,
  Gauge,
  LayoutDashboard,
  LogOut,
  ScrollText,
  ServerCog,
  ShieldAlert,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/services', label: 'Distributed Services', icon: ServerCog },
  { to: '/alerts', label: 'Alerts Center', icon: ShieldAlert },
  { to: '/workflows', label: 'Workflow Approvals', icon: ClipboardList },
  { to: '/observability', label: 'Observability', icon: Gauge },
  { to: '/audit-logs', label: 'Audit Logs', icon: ScrollText },
]

export function Sidebar() {
  const { user, logout } = useAuth()

  return (
    <aside className="flex h-full w-64 shrink-0 flex-col border-r border-surface-600 bg-surface-900">
      <div className="flex items-center gap-2 border-b border-surface-600 px-5 py-4">
        <Activity className="text-blue-500" size={22} />
        <div>
          <p className="text-sm font-semibold leading-tight text-slate-100">Control Plane</p>
          <p className="text-[11px] leading-tight text-slate-500">Distributed Ops</p>
        </div>
      </div>

      <nav className="flex-1 space-y-1 px-3 py-4">
        {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex items-center gap-2.5 rounded-md px-3 py-2 text-sm font-medium transition ${
                isActive
                  ? 'bg-blue-600/15 text-blue-300'
                  : 'text-slate-400 hover:bg-surface-700 hover:text-slate-200'
              }`
            }
          >
            <Icon size={17} />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="border-t border-surface-600 px-4 py-4">
        <div className="mb-3">
          <p className="truncate text-sm font-medium text-slate-200">{user?.email}</p>
          <p className="text-xs uppercase tracking-wide text-slate-500">{user?.role}</p>
        </div>
        <button
          type="button"
          onClick={logout}
          className="flex w-full items-center gap-2 rounded-md border border-surface-500 px-3 py-2 text-sm text-slate-300 transition hover:bg-surface-700"
        >
          <LogOut size={15} />
          Log out
        </button>
      </div>
    </aside>
  )
}
