'use server'

import { createAdminClient, createClient } from '@/utils/supabase/server'
import { revalidatePath } from 'next/cache'
import { redirect } from 'next/navigation'

async function getModeratorId() {
  const supabase = await createClient()
  const { data: { user } } = await supabase.auth.getUser()
  return user?.id
}

export async function verifySubmission(formData: FormData) {
  const submissionId = formData.get('submissionId') as string
  const notes = formData.get('notes') as string
  const modId = await getModeratorId()
  const admin = createAdminClient()
  
  await admin.from('submissions').update({
    status: 'verified',
    verified_at: new Date().toISOString(),
    completed_at: new Date().toISOString(),
    reviewed_by: modId,
    reviewed_at: new Date().toISOString(),
    review_notes: notes,
    verification_source: 'moderator'
  }).eq('id', submissionId)

  await admin.from('moderation_events').insert({
    submission_id: submissionId,
    moderator_id: modId,
    action: 'verified',
    reason: notes
  })
  
  revalidatePath(`/submissions/${submissionId}`)
  revalidatePath('/queue')
  redirect('/queue')
}

export async function rejectSubmission(formData: FormData) {
  const submissionId = formData.get('submissionId') as string
  const reason = formData.get('reason') as string
  const notes = formData.get('notes') as string
  
  const modId = await getModeratorId()
  const admin = createAdminClient()
  
  await admin.from('submissions').update({
    status: 'rejected',
    rejected_reason: reason,
    completed_at: new Date().toISOString(),
    reviewed_by: modId,
    reviewed_at: new Date().toISOString(),
    review_notes: notes,
    verification_source: 'moderator'
  }).eq('id', submissionId)

  await admin.from('moderation_events').insert({
    submission_id: submissionId,
    moderator_id: modId,
    action: 'rejected',
    reason: `Reason: ${reason} - Notes: ${notes}`
  })
  
  revalidatePath(`/submissions/${submissionId}`)
  revalidatePath('/queue')
  redirect('/queue')
}

export async function keepPendingSubmission(formData: FormData) {
  const submissionId = formData.get('submissionId') as string
  const notes = formData.get('notes') as string
  const modId = await getModeratorId()
  const admin = createAdminClient()
  
  await admin.from('submissions').update({
    status: 'pending',
    reviewed_by: modId,
    reviewed_at: new Date().toISOString(),
    review_notes: notes,
    verification_source: 'moderator'
  }).eq('id', submissionId)

  await admin.from('moderation_events').insert({
    submission_id: submissionId,
    moderator_id: modId,
    action: 'pending',
    reason: notes
  })
  
  revalidatePath(`/submissions/${submissionId}`)
  revalidatePath('/queue')
  redirect('/queue')
}
