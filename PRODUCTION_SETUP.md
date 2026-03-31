# Production setup (Render + Vercel) — MySQL

The API stores data in **MySQL** (not Supabase). The frontend talks only to the Spring Boot API and Clerk.

## 0. Local development (MySQL on your machine)

Install MySQL 8+, create the `bridge` database, run **`mysql_schema.sql`**, and configure `.env`. Step-by-step: **[MYSQL_SETUP.md](./MYSQL_SETUP.md)**.

Then start the API:

```bash
set -a && source .env && set +a && ./mvnw spring-boot:run
```

Health: `http://localhost:8080/actuator/health` and `http://localhost:8080/`.

## 1. MySQL on Render

1. Provision MySQL on Render: see [Deploy MySQL](https://docs.render.com/docs/deploy-mysql) (private service on your workspace network). If you prefer another host, use any **MySQL 8+** reachable from your backend (same VPC / allowed IPs).
2. Note the **internal** host (for a service on Render in the same account/region), **port** (usually `3306`), **database name**, **user**, and **password**.
3. Create the tables once by running `mysql_schema.sql` against that database (Render “Shell” for the MySQL service, or any MySQL client over SSL). This is required because the app uses `spring.jpa.hibernate.ddl-auto=none`.

## 2. Render (backend Web Service)

In your backend service → **Environment**, set:

| Key | Value |
|-----|--------|
| `MYSQL_HOST` | MySQL hostname (internal hostname if the DB is on Render) |
| `MYSQL_PORT` | `3306` (or the port your provider gives) |
| `MYSQL_DATABASE` | Database name |
| `MYSQL_USER` | Database user |
| `MYSQL_PASSWORD` | Database password |
| `MYSQL_USE_SSL` | `true` for most managed MySQL providers |
| `ALLOWED_ORIGINS` | Production frontend URL, e.g. `https://your-app.vercel.app` (no trailing slash). Multiple: comma-separated. |

Optional: you can set a full JDBC URL instead of the pieces above using Spring Boot’s relaxed binding, e.g. `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` (then you do not need the `MYSQL_*` URL parts).

Redeploy the web service after saving.

**Remove** any old `SUPABASE_DB_PASSWORD` variable from Render.

If the database was created before custom-work tables existed, run **`mysql_migration_custom_work.sql`** once against production MySQL (same as local migration).

Optional **admin analytics** (`/admin`): set `BRIDGE_ADMIN_KEY` on the backend and the same value as `VITE_BRIDGE_ADMIN_KEY` on Vercel for the frontend.

## 3. Vercel (frontend)

In your Vercel project → **Settings** → **Environment Variables**:

| Key | Value |
|-----|--------|
| `VITE_API_URL` | Your Render backend URL, e.g. `https://bridge-backend.onrender.com` (no trailing slash) |
| `VITE_CLERK_PUBLISHABLE_KEY` | Your Clerk publishable key |

You do **not** need Supabase variables for this stack.

Redeploy the frontend.

## 4. Clerk (production)

If you use a production Clerk application:

- Add your Vercel URL to **Allowed redirect URLs** in Clerk.
- Point the backend JWT verifier at production JWKS (env override or `application.properties`).

## 5. Checklist

- [ ] MySQL instance created; `mysql_schema.sql` applied.
- [ ] Render: `MYSQL_*`, `MYSQL_USE_SSL` (if required), `ALLOWED_ORIGINS` set; `SUPABASE_*` removed.
- [ ] Vercel: `VITE_API_URL`, Clerk key set; no Supabase keys required.
- [ ] Backend redeployed; frontend redeployed.
- [ ] No `localhost` URLs in production env vars.

## 6. Migrating data from Supabase

If you had production data in Postgres, export those tables and import into MySQL with a one-off migration (types differ: UUID → `CHAR(36)`, timestamps → `DATETIME(6)`, table `user` → `bridge_user`). That is separate from this repo’s schema file.
