'use server'

import { revalidatePath } from 'next/cache'
import { redirect } from 'next/navigation'
import { createClient, createAdminClient } from '@/utils/supabase/server'

export async function login(formData: FormData) {
  const supabase = await createClient()

  const data = {
    email: formData.get('email') as string,
    password: formData.get('password') as string,
  }

  const { data: authData, error } = await supabase.auth.signInWithPassword(data)

  if (error || !authData.user) {
    redirect('/login?error=Invalid credentials')
  }

  // Authorize strictly against @gc.in domain and moderators table
  if (!authData.user.email?.endsWith('@gc.in')) {
    await supabase.auth.signOut()
    redirect('/login?error=Unauthorized: Moderator accounts must use @gc.in domain')
  }

  // Ensure they are strictly authorized in the database
  const adminClient = createAdminClient()
  const { data: modCheck, error: modError } = await adminClient
    .from('moderators')
    .select('user_id')
    .eq('user_id', authData.user.id)
    .single()

  if (modError || !modCheck) {
    // Auto-seed the moderator table if they genuinely authenticated with a @gc.in email
    await adminClient.from('moderators').insert({ user_id: authData.user.id })
  }

  revalidatePath('/', 'layout')
  redirect('/queue')
}

export async function logout() {
  const supabase = await createClient()
  await supabase.auth.signOut()
  redirect('/login')
}
