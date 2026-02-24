# Production setup (Render + Vercel)

## 1. Render (backend)

In your Render service → **Environment** tab, add:

| Key | Value |
|-----|--------|
| `SUPABASE_DB_PASSWORD` | Your Supabase database password |
| `ALLOWED_ORIGINS` | Production frontend URL, e.g. `https://bridgefrontend-delta.vercel.app` (no trailing slash). Multiple: comma-separated. |

Save. Render will redeploy. Copy your backend URL (e.g. `https://bridge-backend.onrender.com`).

---

## 2. Vercel (frontend)

In your Vercel project → **Settings** → **Environment Variables**, add for **Production** (and Preview if you want):

| Key | Value |
|-----|--------|
| `VITE_API_URL` | Your Render backend URL, e.g. `https://bridge-backend.onrender.com` (no trailing slash) |
| `VITE_CLERK_PUBLISHABLE_KEY` | Your Clerk publishable key (same as dev or production key from Clerk Dashboard) |
| `VITE_SUPABASE_URL` | Your Supabase URL, e.g. `https://xxxxx.supabase.co` |
| `VITE_SUPABASE_ANON_KEY` | Your Supabase anon/public key |

Redeploy the frontend so the new env vars are applied.

---

## 3. Clerk (optional for production app)

If you use a separate **production** Clerk application:

- In Clerk Dashboard, add your Vercel URL to **Allowed redirect URLs**.
- Use the production publishable key in Vercel and, if needed, set the backend to use the production Clerk JWKS URL (e.g. via env or `application.properties`).

---

## 4. Checklist

- [ ] Render: `SUPABASE_DB_PASSWORD` and `ALLOWED_ORIGINS` (your Vercel URL) set.
- [ ] Vercel: `VITE_API_URL` = Render URL, Clerk key, Supabase URL and anon key set.
- [ ] Frontend redeployed after changing env vars.
- [ ] No localhost URLs in production env vars.
