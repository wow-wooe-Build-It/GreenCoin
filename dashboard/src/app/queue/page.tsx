import { createAdminClient } from '@/utils/supabase/server'
import Link from 'next/link'
import { formatDistanceToNow } from 'date-fns'

export const dynamic = 'force-dynamic'

export default async function QueuePage(props: { searchParams: Promise<{ tab?: string }> }) {
  const searchParams = await props.searchParams
  const currentTab = searchParams?.tab || 'pending'
  const adminClient = createAdminClient()

  const statusFilter = 
    currentTab === 'verified' ? 'verified' : 
    currentTab === 'rejected' ? 'rejected' : 
    'pending'

  const { data: submissions, error } = await adminClient
    .from('submissions')
    .select('*, users(full_name), missions(title)')
    .eq('status', statusFilter)
    .order('created_at', { ascending: false })

  if (error) {
    console.error("SUPABASE QUEUE ERROR:", error)
  }

  const tabs = [
    { id: 'pending', label: 'Needs Review' },
    { id: 'verified', label: 'Verified' },
    { id: 'rejected', label: 'Rejected' },
  ]

  return (
    <div className="p-8 max-w-7xl mx-auto h-screen overflow-y-auto">
      <header className="mb-8">
        <h1 className="text-3xl font-bold text-primary mb-2">Review Queue</h1>
        <p className="text-text-secondary">Process and audit AI verifications</p>
      </header>
      
      <div className="flex gap-4 mb-6 border-b border-surface-darkVariant">
        {tabs.map(tab => (
          <Link 
            key={tab.id} 
            href={`/queue?tab=${tab.id}`}
            className={`px-4 py-3 font-medium transition-all ${currentTab === tab.id ? 'text-primary border-b-2 border-primary' : 'text-text-secondary hover:text-text-primary'}`}
          >
            {tab.label}
          </Link>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-4">
        {error && (
          <div className="bg-status-error/20 border border-status-error/50 p-4 rounded-xl text-status-error">
            <strong>Database Error:</strong> {error.message}
            <br/><span className="text-xs">{error.details}</span>
            <br/><span className="text-xs">{error.hint}</span>
          </div>
        )}
        {submissions?.map(sub => (
          <div key={sub.id} className="bg-surface-dark border border-surface-darkVariant p-5 rounded-2xl flex items-center justify-between shadow-sm">
            <div className="flex flex-col gap-1">
              <strong className="text-lg text-text-primary">{sub.missions?.title || 'Unknown Mission'}</strong>
              <span className="text-sm text-text-secondary">
                Submitted by {sub.users?.full_name || 'Anonymous User'} • {formatDistanceToNow(new Date(sub.created_at))} ago
              </span>
              <span className="text-xs text-text-tertiary mt-1">Location: {sub.location_name || 'N/A'}</span>
            </div>
            
            <div className="flex gap-4 items-center">
              {sub.ai_final_score !== null && (
                 <div className="text-right">
                   <div className="text-sm text-text-secondary">AI Score</div>
                   <div className="font-mono text-primary font-bold">{(sub.ai_final_score * 100).toFixed(0)}%</div>
                 </div>
              )}
              <Link 
                href={`/submissions/${sub.id}`} 
                className="px-6 py-2 rounded-xl bg-surface-darkVariant text-text-primary hover:bg-primary hover:text-bg-dark transition-all shadow-[0_0_10px_rgba(162,255,0,0.1)] font-medium"
              >
                Inspect
              </Link>
            </div>
          </div>
        ))}
        {(!submissions || submissions.length === 0) && (
          <div className="py-20 text-center text-text-secondary bg-surface-dark/50 rounded-2xl border border-dashed border-surface-darkVariant">
            No submissions found in this queue.
          </div>
        )}
      </div>
    </div>
  )
}
