import { login } from './actions'

export default async function LoginPage(props: { searchParams: Promise<{ error?: string }> }) {
  const searchParams = await props.searchParams
  const error = searchParams?.error

  return (
    <div className="flex h-screen items-center justify-center p-4">
      <div className="w-full max-w-sm rounded-[24px] bg-surface-dark p-8 shadow-[0_0_20px_rgba(162,255,0,0.1)] border border-surface-darkVariant">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-primary mb-2">GreenCoins</h1>
          <p className="text-text-secondary text-sm">Moderator Base Dashboard</p>
        </div>
        
        {error && (
          <div className="mb-6 p-3 rounded-lg bg-status-error/10 border border-status-error/20 text-status-error text-sm text-center">
            {error}
          </div>
        )}
        
        <form action={login} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <label className="text-sm font-medium text-text-secondary" htmlFor="email">Email</label>
            <input 
              id="email" 
              name="email" 
              type="email" 
              required 
              className="rounded-xl border border-surface-lightVariant/20 bg-bg-dark px-4 py-3 outline-none focus:border-primary text-text-primary"
            />
          </div>
          <div className="flex flex-col gap-2">
            <label className="text-sm font-medium text-text-secondary" htmlFor="password">Password</label>
            <input 
              id="password" 
              name="password" 
              type="password" 
              required 
              className="rounded-xl border border-surface-lightVariant/20 bg-bg-dark px-4 py-3 outline-none focus:border-primary text-text-primary"
            />
          </div>
          <button 
            type="submit" 
            className="mt-4 rounded-xl bg-primary px-4 py-3 font-semibold text-bg-dark hover:brightness-110 transition-all shadow-[0_0_15px_rgba(162,255,0,0.2)]"
          >
            Authenticate
          </button>
        </form>
      </div>
    </div>
  )
}
