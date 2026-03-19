import { logout } from '@/app/login/actions'
import Link from 'next/link'
import { LayoutDashboard, LogOut } from 'lucide-react'

export function Sidebar() {
  return (
    <aside className="w-64 bg-surface-dark border-r border-surface-darkVariant p-6 flex flex-col justify-between">
      <div>
        <h2 className="text-xl font-bold text-primary mb-8 tracking-wide">GreenCoins Mod</h2>
        <nav className="flex flex-col gap-2">
          <Link href="/queue" className="flex items-center gap-3 px-4 py-3 rounded-xl bg-surface-darkVariant text-text-primary hover:bg-surface-lightVariant/10 transition">
            <LayoutDashboard size={20} />
            Queue
          </Link>
        </nav>
      </div>
      <form action={logout}>
        <button type="submit" className="flex items-center gap-3 px-4 py-3 w-full text-left rounded-xl text-status-error hover:bg-status-error/10 transition">
          <LogOut size={20} />
          Logout
        </button>
      </form>
    </aside>
  )
}
