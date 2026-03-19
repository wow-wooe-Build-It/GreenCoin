import { createAdminClient } from '@/utils/supabase/server'
import { verifySubmission, rejectSubmission, keepPendingSubmission } from './actions'
import { formatDistanceToNow } from 'date-fns'
import Link from 'next/link'
import { ArrowLeft, CheckCircle, XCircle, Clock } from 'lucide-react'

export const dynamic = 'force-dynamic'

export default async function SubmissionDetail(props: { params: Promise<{ id: string }> }) {
  const params = await props.params
  const adminClient = createAdminClient()
  
  const { data: sub } = await adminClient
    .from('submissions')
    .select('*, users(full_name), missions(title), moderation_events(*)')
    .eq('id', params.id)
    .single()

  if (!sub) {
    return <div className="p-8 text-center">Submission not found</div>
  }

  // Fallback map for older submissions that only had `image_url` instead of before/after
  const beforeUrl = sub.before_image_url || sub.image_url || 'https://placehold.co/600x400?text=No+Before+Image'
  const afterUrl = sub.after_image_url || 'https://placehold.co/600x400?text=No+After+Image'

  return (
    <div className="p-8 max-w-7xl mx-auto h-screen overflow-y-auto">
      <Link href="/queue" className="inline-flex items-center gap-2 text-text-secondary hover:text-primary transition mb-8">
        <ArrowLeft size={20} /> Back to Queue
      </Link>
      
      <div className="flex justify-between items-start mb-8">
        <div>
          <h1 className="text-3xl font-bold text-text-primary mb-2 flex items-center gap-3">
            {sub.missions?.title || 'Unknown Mission'}
            {sub.status === 'verified' && <span className="bg-status-success/20 text-status-success text-xs px-3 py-1 rounded-pill flex items-center gap-1"><CheckCircle size={14}/> Verified</span>}
            {sub.status === 'rejected' && <span className="bg-status-error/20 text-status-error text-xs px-3 py-1 rounded-pill flex items-center gap-1"><XCircle size={14}/> Rejected</span>}
            {sub.status === 'pending' && <span className="bg-status-warning/20 text-status-warning text-xs px-3 py-1 rounded-pill flex items-center gap-1"><Clock size={14}/> Pending</span>}
            {sub.status === 'failed' && <span className="bg-status-error/40 text-status-error text-xs px-3 py-1 rounded-pill flex items-center gap-1"><XCircle size={14}/> AI Failed</span>}
          </h1>
          <p className="text-text-secondary">
            Submitted by <span className="text-primary">{sub.users?.full_name || 'Anonymous User'}</span> on {new Date(sub.created_at).toLocaleString()}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left: Media Context */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-surface-dark border border-surface-darkVariant rounded-[24px] p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-text-primary mb-4">Photographic Evidence</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <span className="text-sm text-text-secondary block mb-2">Before</span>
                <img src={beforeUrl} alt="Before" className="w-full h-80 object-cover rounded-xl bg-surface-darkVariant" />
              </div>
              <div>
                 <span className="text-sm text-text-secondary block mb-2">After</span>
                 <img src={afterUrl} alt="After" className="w-full h-80 object-cover rounded-xl bg-surface-darkVariant" />
              </div>
            </div>
            
            <div className="mt-6 pt-6 border-t border-surface-darkVariant">
              <h3 className="text-sm font-medium text-text-secondary mb-2">Location Context</h3>
              <p className="text-text-primary">{sub.location_name || 'No location metadata provided.'}</p>
              {sub.latitude && sub.longitude && (
                <p className="text-xs font-mono text-text-tertiary mt-1">{sub.latitude}, {sub.longitude}</p>
              )}
            </div>
            <div className="mt-6 pt-6 border-t border-surface-darkVariant">
              <h3 className="text-sm font-medium text-text-secondary mb-2">User Description</h3>
              <p className="text-text-primary bg-bg-dark p-4 rounded-xl border border-surface-darkVariant">
                {sub.description || 'No description provided.'}
              </p>
            </div>
          </div>
        </div>

        {/* Right: Moderation UI */}
        <div className="space-y-6">
          <div className="bg-surface-dark border border-surface-darkVariant rounded-[24px] p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-text-primary mb-4">AI Verification Details</h2>
            <div className="space-y-4">
              <div className="flex justify-between items-center py-2 border-b border-surface-darkVariant">
                <span className="text-text-secondary">Final Score</span>
                <span className="text-primary font-mono font-bold">{sub.ai_final_score !== null ? (sub.ai_final_score * 100).toFixed(0) + '%' : 'N/A'}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-surface-darkVariant">
                <span className="text-text-secondary">Authenticity</span>
                <span className="text-text-primary font-mono">{sub.ai_authenticity_score !== null ? (sub.ai_authenticity_score * 100).toFixed(0) + '%' : 'N/A'}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-surface-darkVariant">
                <span className="text-text-secondary">Gemini Verdict</span>
                <span className="text-text-primary font-mono">{sub.ai_gemini_score !== null ? (sub.ai_gemini_score * 100).toFixed(0) + '%' : 'N/A'}</span>
              </div>
              
              {sub.ai_debug_reason && (
                <div className="pt-2">
                  <span className="text-status-error text-xs block mb-1 uppercase font-semibold">AI Flag Trigger</span>
                  <p className="text-sm text-text-secondary bg-status-error/10 p-3 rounded-lg border border-status-error/20">
                    {sub.ai_debug_reason}
                  </p>
                </div>
              )}
            </div>
          </div>

          <div className="bg-surface-dark border border-surface-primary border-primary/30 rounded-[24px] p-6 shadow-[0_0_20px_rgba(162,255,0,0.05)]">
            <h2 className="text-lg font-semibold text-text-primary mb-4">Moderation Action</h2>
            <form className="space-y-4">
              <input type="hidden" name="submissionId" value={sub.id} />
              
              <div>
                <label className="text-sm text-text-secondary mb-1 block">Review Notes (Internal)</label>
                <textarea 
                  name="notes" 
                  rows={2} 
                  className="w-full bg-bg-dark border border-surface-darkVariant rounded-xl p-3 text-text-primary text-sm focus:border-primary outline-none"
                  placeholder="Justify your verification decision..."
                />
              </div>

              <div>
                <label className="text-sm text-text-secondary mb-1 block">Rejection Reason</label>
                <input 
                  type="text" 
                  name="reason" 
                  className="w-full bg-bg-dark border border-surface-darkVariant rounded-xl p-3 text-text-primary text-sm focus:border-status-error outline-none"
                  placeholder="If rejecting, state the reason shown to user"
                />
              </div>

              <div className="grid grid-cols-2 gap-3 pt-2">
                <button formAction={verifySubmission} className="col-span-2 bg-primary text-bg-dark font-bold py-3 rounded-xl hover:brightness-110 shadow-[0_0_15px_rgba(162,255,0,0.2)] transition-all">
                  Approve Evidence
                </button>
                <button formAction={rejectSubmission} className="bg-[#FF4444]/10 text-[#FF4444] border border-[#FF4444]/20 font-semibold py-3 rounded-xl hover:bg-[#FF4444]/20 transition-all">
                  Reject
                </button>
                <button formAction={keepPendingSubmission} className="bg-surface-darkVariant text-text-primary font-semibold py-3 rounded-xl hover:bg-surface-lightVariant/10 transition-all">
                  Keep Pending
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}
