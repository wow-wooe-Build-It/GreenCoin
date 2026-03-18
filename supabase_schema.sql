-- GreenCoins - Fully Database-Driven Schema
-- Enable Row Level Security (RLS) on all tables

-- 1. USERS TABLE (extended for dynamic profile data)
create table if not exists public.users (
  id uuid references auth.users not null primary key,
  email text,
  full_name text,
  avatar_url text,
  total_gc int default 0,
  coins int default 0,
  level int default 1,
  missions_completed int default 0,
  streak_count int default 0,
  last_mission_date date,
  global_rank int,
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

-- Add new columns if migrating from existing schema
alter table public.users add column if not exists total_gc int default 0;
alter table public.users add column if not exists coins int default 0;
alter table public.users add column if not exists missions_completed int default 0;
alter table public.users add column if not exists streak_count int default 0;
alter table public.users add column if not exists last_mission_date date;
alter table public.users add column if not exists level int default 1;
alter table public.users add column if not exists global_rank int;
alter table public.users add column if not exists updated_at timestamptz default now();
alter table public.users add column if not exists phone text;
alter table public.users add column if not exists city text;

-- Drop legacy eco score columns if they exist
alter table public.users drop column if exists eco_score;
alter table public.users drop column if exists trees_planted;
alter table public.users drop column if exists plastic_recycled_kg;
alter table public.users drop column if exists co2_saved_kg;

alter table public.users enable row level security;

drop policy if exists "Users can read own profile" on public.users; create policy "Users can read own profile"
  on public.users for select using ( auth.uid() = id );

drop policy if exists "Users can update own profile" on public.users;
create policy "Users can update own profile"
  on public.users for update using ( auth.uid() = id );

create or replace function public.handle_new_user()
returns trigger as $$
begin
  insert into public.users (id, email, full_name, avatar_url)
  values (
    new.id,
    new.email,
    new.raw_user_meta_data->>'full_name',
    new.raw_user_meta_data->>'avatar_url'
  )
  on conflict (id) do nothing;
  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();


-- 2. CHALLENGES TABLE
create table if not exists public.challenges (
  id uuid default gen_random_uuid() primary key,
  title text not null,
  description text,
  cover_image_url text,
  reward_gc int default 0,
  start_date timestamptz,
  end_date timestamptz,
  is_active boolean default true,
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

alter table public.challenges add column if not exists updated_at timestamptz default now();
alter table public.challenges enable row level security;

drop policy if exists "Public read access for challenges" on public.challenges;
create policy "Public read access for challenges"
  on public.challenges for select using ( true );


-- 3. MISSIONS TABLE (with steps for briefing)
create table if not exists public.missions (
  id uuid default gen_random_uuid() primary key,
  title text not null,
  description text,
  icon_type text,
  image_url text,
  gc_reward int default 0,
  category text,
  is_active boolean default true,
  steps jsonb default '["Prepare for the mission action", "Perform the eco-friendly task", "Take a photo as proof"]',
  challenge_id uuid references public.challenges(id),
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

alter table public.missions add column if not exists image_url text;
alter table public.missions add column if not exists category text;
alter table public.missions add column if not exists is_active boolean default true;
alter table public.missions add column if not exists steps jsonb default '["Prepare for the mission action", "Perform the eco-friendly task", "Take a photo as proof"]';
alter table public.missions add column if not exists updated_at timestamptz default now();

alter table public.missions enable row level security;

drop policy if exists "Public read access for missions" on public.missions;
create policy "Public read access for missions"
  on public.missions for select using ( true );


-- 4. USER_CHALLENGES TABLE (track joined challenges)
create table if not exists public.user_challenges (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references public.users(id) not null,
  challenge_id uuid references public.challenges(id) not null,
  challenge_score int default 0,
  joined_at timestamptz default now(),
  unique(user_id, challenge_id)
);

alter table public.user_challenges enable row level security;

drop policy if exists "Users can read own challenge joins" on public.user_challenges;
create policy "Users can read own challenge joins"
  on public.user_challenges for select using ( auth.uid() = user_id );

drop policy if exists "Users can insert own challenge joins" on public.user_challenges;
create policy "Users can insert own challenge joins"
  on public.user_challenges for insert with check ( auth.uid() = user_id );


-- 5. SUBMISSIONS TABLE
create table if not exists public.submissions (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references public.users(id) not null,
  mission_id uuid references public.missions(id) not null,
  challenge_id uuid references public.challenges(id),
  description text,
  image_url text,
  before_image_url text,
  after_image_url text,
  latitude float8,
  longitude float8,
  location_name text,
  notes text,
  reward_coins integer,
  status text default 'pending',
  rejected_reason text,
  completed_at timestamptz default now(),
  verified_at timestamptz,
  created_at timestamptz default now()
);

alter table public.submissions add column if not exists location_name text;
alter table public.submissions add column if not exists notes text;
alter table public.submissions add column if not exists reward_coins integer;
alter table public.submissions add column if not exists completed_at timestamptz default now();
alter table public.submissions add column if not exists verified_at timestamptz;

alter table public.submissions enable row level security;

drop policy if exists "Users can view own submissions" on public.submissions;
create policy "Users can view own submissions"
  on public.submissions for select using ( auth.uid() = user_id );

drop policy if exists "Users can create submissions" on public.submissions;
create policy "Users can create submissions"
  on public.submissions for insert with check ( auth.uid() = user_id );


-- 6. REWARDS TABLE (SHOP)
create table if not exists public.rewards (
  id uuid default gen_random_uuid() primary key,
  title text not null,
  description text,
  category text not null,
  gc_cost int default 0,
  image_url text,
  discount_label text,
  stock int default -1,
  is_active boolean default true,
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

alter table public.rewards add column if not exists stock int default -1;
alter table public.rewards add column if not exists is_active boolean default true;
alter table public.rewards add column if not exists updated_at timestamptz default now();

alter table public.rewards enable row level security;

drop policy if exists "Public read access for rewards" on public.rewards;
create policy "Public read access for rewards"
  on public.rewards for select using ( true );


-- 7. TRANSACTIONS TABLE
create table if not exists public.transactions (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references public.users(id) not null,
  amount int not null,
  description text,
  type text,
  reference_id uuid,
  created_at timestamptz default now()
);

alter table public.transactions add column if not exists reference_id uuid;

alter table public.transactions enable row level security;

drop policy if exists "Users can read own transactions" on public.transactions;
create policy "Users can read own transactions"
  on public.transactions for select using ( auth.uid() = user_id );

drop policy if exists "Users can insert own transactions" on public.transactions;
create policy "Users can insert own transactions"
  on public.transactions for insert with check ( auth.uid() = user_id );


-- 8. FAQ TABLE (for Help screen dynamic content)
create table if not exists public.faq (
  id uuid default gen_random_uuid() primary key,
  question text not null,
  answer text not null,
  sort_order int default 0,
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

alter table public.faq enable row level security;

drop policy if exists "Public read access for FAQ" on public.faq;
create policy "Public read access for FAQ"
  on public.faq for select using ( true );


-- 9. REWARD_CATEGORIES TABLE (for Shop categories)
create table if not exists public.reward_categories (
  id uuid default gen_random_uuid() primary key,
  name text not null unique,
  sort_order int default 0,
  created_at timestamptz default now()
);

alter table public.reward_categories enable row level security;

drop policy if exists "Public read access for reward categories" on public.reward_categories;
create policy "Public read access for reward categories"
  on public.reward_categories for select using ( true );


-- 10. LEVELS TABLE
create table if not exists public.levels (
  id integer primary key generated by default as identity,
  level_number integer unique not null,
  title text not null,
  missions_required integer not null,
  coin_reward integer not null,
  created_at timestamptz default now()
);

alter table public.levels enable row level security;
drop policy if exists "Public read access for levels" on public.levels;
create policy "Public read access for levels" on public.levels for select using (true);


-- DATABASE FUNCTIONS & TRIGGERS

-- Calculate streak dynamically from verified submissions (source of truth)
-- Logic: Get unique dates from verified submissions, count consecutive days from most recent
-- Edge cases: no activity=0, gap>1 day=0, same day multiple missions=count once
create or replace function public.get_calculated_streak(p_user_id uuid)
returns table(streak int) as $$
declare
  v_dates date[];
  v_most_recent date;
  v_expected date;
  v_count int := 0;
  v_i int;
begin
  select array_agg(d order by d desc)
  into v_dates
  from (
    select distinct date(completed_at) as d
    from public.submissions
    where user_id = p_user_id and status = 'verified' and completed_at is not null
  ) sub;

  if v_dates is null or array_length(v_dates, 1) is null then
    return query select 0;
    return;
  end if;

  v_most_recent := v_dates[1];
  if v_most_recent < current_date - interval '1 day' then
    return query select 0;
    return;
  end if;

  v_expected := v_most_recent;
  for v_i in 1..array_length(v_dates, 1) loop
    if v_dates[v_i] = v_expected then
      v_count := v_count + 1;
      v_expected := v_expected - interval '1 day';
    else
      exit;
    end if;
  end loop;

  return query select v_count;
end;
$$ language plpgsql security definer;

-- Handle Mission Verified (Leveling, Streaks, Coins)
create or replace function public.handle_mission_verified()
returns trigger as $$
declare
  v_today date := current_date;
  v_yesterday date := current_date - interval '1 day';
  v_user record;
  v_next_level record;
  v_mission_reward int := 0;
begin
  -- Only execute if status changed to 'verified'
  if new.status = 'verified' and (tg_op = 'INSERT' or old.status != 'verified') then
    
    -- Get user data
    select * into v_user from public.users where id = new.user_id for update;
    if not found then return new; end if;

    -- Get mission reward
    select gc_reward into v_mission_reward from public.missions where id = new.mission_id;
    if v_mission_reward is null then v_mission_reward := 0; end if;

    -- Update Mission Timestamps & Reward
    new.verified_at := now();
    new.reward_coins := v_mission_reward;
    if new.completed_at is null then new.completed_at := now(); end if;

    -- Calculate streak from verified submissions (source of truth, not stored value)
    select s.streak into v_user.streak_count from public.get_calculated_streak(new.user_id) s limit 1;
    v_user.streak_count := coalesce(v_user.streak_count, 0);

    -- Update base user stats & grant mission coins
    v_user.missions_completed := coalesce(v_user.missions_completed, 0) + 1;
    v_user.last_mission_date := v_today;
    v_user.coins := coalesce(v_user.coins, 0) + coalesce(v_mission_reward, 0);
    v_user.total_gc := coalesce(v_user.total_gc, 0) + coalesce(v_mission_reward, 0);
    
    -- Insert Transaction Record
    insert into public.transactions (user_id, amount, type, reference_id)
    values (new.user_id, v_mission_reward, 'mission_reward', new.id);

    -- Evaluate Level Up
    loop
      select * into v_next_level from public.levels where level_number = coalesce(v_user.level, 0) + 1;
      exit when not found;

      if coalesce(v_user.missions_completed, 0) >= coalesce(v_next_level.missions_required, 0) then
        v_user.level := v_next_level.level_number;
        v_user.coins := coalesce(v_user.coins, 0) + coalesce(v_next_level.coin_reward, 0);
        v_user.total_gc := coalesce(v_user.total_gc, 0) + coalesce(v_next_level.coin_reward, 0);
      else
        exit;
      end if;
    end loop;

    -- Save back to users table
    update public.users set 
      streak_count = v_user.streak_count,
      missions_completed = v_user.missions_completed,
      last_mission_date = v_user.last_mission_date,
      coins = v_user.coins,
      total_gc = v_user.total_gc,
      level = v_user.level
    where id = new.user_id;

  end if;
  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists on_submission_verified on public.submissions;
drop trigger if exists on_submission_approved on public.submissions;

create trigger on_submission_verified
  before update or insert on public.submissions
  for each row execute procedure public.handle_mission_verified();

-- Reset streak when user has missed a day (last_mission_date before yesterday)
-- Call on app open to ensure streak breaks correctly when inactive
create or replace function public.check_and_reset_streak(p_user_id uuid)
returns void as $$
begin
  update public.users
  set streak_count = 0
  where id = p_user_id
    and (last_mission_date is null or last_mission_date < current_date - interval '1 day');
end;
$$ language plpgsql security definer;

-- Get User Weekly Streak RPC
create or replace function public.get_user_streak(p_user_id uuid)
returns boolean[] as $$
declare
  v_days boolean[] := array[false, false, false, false, false, false, false];
  v_record record;
  v_day_diff int;
begin
  for v_record in 
    select date(completed_at) as mission_date
    from public.submissions
    where user_id = p_user_id and status = 'verified'
      and date(completed_at) >= current_date - interval '6 days'
  loop
    v_day_diff := current_date - v_record.mission_date;
    if v_day_diff between 0 and 6 then
      -- Array is 1-indexed. Index 7 is today, Index 1 is 6 days ago.
      v_days[7 - v_day_diff] := true;
    end if;
  end loop;
  
  return v_days;
end;
$$ language plpgsql security definer;


-- SEED DATA

-- Levels
insert into public.levels (level_number, title, missions_required, coin_reward) values
(1, 'Seed', 0, 0),
(2, 'Sprout', 5, 150),
(3, 'Eco Explorer', 15, 200),
(4, 'Green Guardian', 30, 300),
(5, 'Earth Champion', 50, 500)
on conflict (level_number) do nothing;

-- Reward Categories
insert into public.reward_categories (name, sort_order) values
('Travel', 1),
('Eco Store', 2),
('Lifestyle', 3),
('Direct Donate', 4),
('Food & Beverage', 5)
on conflict (name) do nothing;

-- Missions (run once; existing DB may already have these)
insert into public.missions (title, description, icon_type, gc_reward, steps)
select * from (values
  ('Green Canopy', 'Plant a native tree', 'TreePine', 250, '["Prepare for the mission action", "Perform the eco-friendly task", "Take a photo as proof"]'::jsonb),
  ('Cycle Loop', 'Verify recycling batch', 'Recycle', 150, '["Prepare for the mission action", "Perform the eco-friendly task", "Take a photo as proof"]'::jsonb),
  ('Plastic-Free', 'Cleanup plastic waste', 'Leaf', 100, '["Prepare for the mission action", "Perform the eco-friendly task", "Take a photo as proof"]'::jsonb),
  ('Community Pulse', 'NGO volunteer work', 'Users', 300, '["Prepare for the mission action", "Perform the eco-friendly task", "Take a photo as proof"]'::jsonb),
  ('Eco-Clearance', 'Garbage cleanup', 'Trash2', 200, '["Prepare for the mission action", "Perform the eco-friendly task", "Take a photo as proof"]'::jsonb),
  ('Wildcard', 'Propose eco-action', 'Zap', 50, '["Prepare for the mission action", "Perform the eco-friendly task", "Take a photo as proof"]'::jsonb)
) v(title, description, icon_type, gc_reward, steps)
where not exists (select 1 from public.missions limit 1);

-- Challenges
insert into public.challenges (title, description, reward_gc, end_date, cover_image_url)
select * from (values
  ('City Cleanup Drive', 'Join the city-wide cleanup marathon.', 800, now() + interval '2 days', 'https://images.unsplash.com/photo-1757801720436-032c2e5b58c6?q=80&w=400'),
  ('Solar Transition', 'Switch to solar energy solutions.', 1500, now() + interval '12 days', 'https://images.unsplash.com/photo-1759266039803-1f81c04bd4c0?q=80&w=400'),
  ('Green Roof Initiative', 'Promoting green rooftops.', 5000, now() + interval '30 days', 'https://images.unsplash.com/photo-1607194402064-d0742de6d17b?q=80&w=600'),
  ('Coral Reef Revival', 'Protect our oceans.', 0, now() + interval '60 days', 'https://images.unsplash.com/photo-1741704445331-83ed820f0214?q=80&w=600')
) v(title, description, reward_gc, end_date, cover_image_url)
where not exists (select 1 from public.challenges limit 1);

-- Rewards
insert into public.rewards (title, category, gc_cost, image_url, discount_label)
select * from (values
  ('Metro Pass (1 Month)', 'Travel', 1200, 'https://images.unsplash.com/photo-1712591009476-5fe03c2ea938?q=80&w=400', '20% OFF'),
  ('Eco Bottle Pro', 'Eco Store', 800, 'https://images.unsplash.com/photo-1760863264228-fa0792a2d894?q=80&w=400', 'FREE'),
  ('Forest Donation', 'Direct Donate', 500, 'https://images.unsplash.com/photo-1647220576336-f2e94680f3b8?q=80&w=400', null),
  ('Zero Waste Kit', 'Lifestyle', 1500, 'https://images.unsplash.com/photo-1759868412016-8b7da190992a?q=80&w=400', '15% OFF'),
  ('₹50 Metro Recharge', 'Travel', 250, null, null),
  ('Bus Pass Discount', 'Travel', 400, null, null),
  ('Reusable Bottle', 'Eco Store', 300, null, null),
  ('Cloth Tote Bag', 'Eco Store', 200, null, null),
  ('Café Voucher', 'Lifestyle', 350, null, null)
) v(title, category, gc_cost, image_url, discount_label)
where not exists (select 1 from public.rewards limit 1);

-- FAQ (Help screen)
insert into public.faq (question, answer, sort_order)
select * from (values
  ('How are missions verified?', 'We use a combination of AI image recognition, metadata validation (GPS/Timestamp), and community peer-review to ensure every action is genuine.', 1),
  ('What can I buy with GreenCoins?', 'GreenCoins can be redeemed for sustainable products, public transport passes, or converted into direct donations for certified eco-projects.', 2),
  ('How do I level up?', 'Earn XP by completing missions and challenges. Higher levels unlock exclusive high-reward missions and limited edition rewards.', 3)
) v(question, answer, sort_order)
where not exists (select 1 from public.faq limit 1);

-- Storage: Create buckets 'mission-proofs' and 'avatar' in Supabase Dashboard

-- STORAGE POLICIES FOR 'avatar' BUCKET
-- Note: You must first manually create the 'avatar' bucket as PUBLIC in the Supabase Dashboard, 
-- then run these policies to allow users to upload and update their own avatars.

insert into storage.buckets (id, name, public) values ('avatar', 'avatar', true) on conflict (id) do nothing;

create policy "Avatar images are publicly accessible."
  on storage.objects for select
  using ( bucket_id = 'avatar' );

create policy "Users can upload their own avatar."
  on storage.objects for insert
  with check ( bucket_id = 'avatar' and auth.uid()::text = (storage.foldername(name))[1] );

create policy "Users can update their own avatar."
  on storage.objects for update
  using ( bucket_id = 'avatar' and auth.uid()::text = (storage.foldername(name))[1] );

