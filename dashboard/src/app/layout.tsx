import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import { createClient } from '@/utils/supabase/server'
import { Sidebar } from '@/components/Sidebar'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'GreenCoins Moderator Dashboard',
  description: 'AI Auditing & Moderator Platform',
}

export default async function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const supabase = await createClient()
  const { data: { user } } = await supabase.auth.getUser()

  return (
    <html lang="en" className="dark">
      <body className={`${inter.className} bg-bg-dark text-text-primary min-h-screen flex`}>
        {user && <Sidebar />}
        <main className="flex-1 overflow-x-hidden relative">
          {children}
        </main>
      </body>
    </html>
  )
}
