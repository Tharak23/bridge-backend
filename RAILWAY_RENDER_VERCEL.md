# Deploy: Railway MySQL + Render backend + Vercel frontend

Your Spring app expects **MySQL 8** and **does not** auto-create tables (`ddl-auto=none`). You must run **`mysql_schema.sql`** once on the database Railway gives you.

The Railway **Database → Data** tab (see [Using the Database View](https://docs.railway.com/databases/database-view)) is good for small queries and browsing tables. For the full Bridge schema, use the **SQL bar** to run the script **or** connect with the `mysql` CLI from your laptop using the **public TCP** host (below).

---

## 1. Railway: MySQL service

1. Add **MySQL** from Railway templates (you already have it **Online** with a volume — keep that).
2. Open the MySQL service → **Variables**. Note:
   - `MYSQL_HOST` / `MYSQLUSER` / `MYSQLPASSWORD` / `MYSQLDATABASE` / `MYSQLPORT`  
     (exact names appear in your project; Railway may use `MYSQL_URL` as well.)
3. **Render is outside Railway**, so the backend cannot use Railway’s **private** hostname. Enable **public TCP** access:
   - MySQL service → **Settings** → **Networking** → **TCP Proxy** (generate / copy the **public host and port**, e.g. `*.proxy.rlwy.net` and a port like `12345`).
   - Use this **public host + port** in Render as `MYSQL_HOST` and `MYSQL_PORT`.
4. Decide the **logical database name**:
   - Either use the database Railway already created (value of `MYSQLDATABASE`), **or** create one named `bridge` and use that everywhere.

---

## 2. What SQL to run (only these from this repo)

| Situation | File |
|-----------|------|
| **New empty Railway database** | Run the full **`mysql_schema.sql`** once. |
| **Database already had old tables but no custom-work tables** | Run only **`mysql_migration_custom_work.sql`** once. |

**Do not** run `supabase_schema.sql` or `supabase_migration_booking.sql` on MySQL (Postgres only).

### Database name

`mysql_schema.sql` does **not** contain `CREATE DATABASE`. Before running it:

1. Connect to MySQL (Data tab SQL bar, or CLI) **using the database you will put in `MYSQL_DATABASE` on Render** (Railway’s default or `bridge`).
2. If you want a dedicated DB named `bridge` and it does not exist yet:
   ```sql
   CREATE DATABASE IF NOT EXISTS bridge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   Then select it (`USE bridge;` in CLI, or set default DB when connecting) and run **`mysql_schema.sql`**.

### How to run the full file

- **Option A — Railway Data tab:** Paste **`mysql_schema.sql`** into the SQL query area and execute (if Railway splits on `;`, you may need to run in a few chunks).
- **Option B — Your machine:**  
  `mysql -h PUBLIC_TCP_HOST -P PUBLIC_TCP_PORT -u USER -p -D YOUR_DATABASE < mysql_schema.sql`  
  (use the TCP proxy host/port from step 1.3).

Afterward, refresh the Data tab — you should see tables: **`bridge_user`**, **`service_provider`**, **`booking`**, **`custom_work_request`**, **`custom_work_application`**.

---

## 3. Render: Web Service (backend)

Environment variables (match Railway’s user, password, and **database name** you used for the schema):

| Variable | Value |
|----------|--------|
| `MYSQL_HOST` | Railway **TCP proxy** hostname (not the private internal host). |
| `MYSQL_PORT` | Railway **TCP proxy** port (not always 3306 on the proxy). |
| `MYSQL_DATABASE` | Same database you ran `mysql_schema.sql` against. |
| `MYSQL_USER` | From Railway Variables. |
| `MYSQL_PASSWORD` | From Railway Variables. |
| `MYSQL_USE_SSL` | Start with **`true`** for public TCP to Railway; if the app fails to connect, try **`false`** per Railway/MySQL connector behavior. |
| `ALLOWED_ORIGINS` | Your Vercel origin(s), comma-separated, **no trailing slash**. |
| `BRIDGE_ADMIN_KEY` | Optional; same as `VITE_BRIDGE_ADMIN_KEY` on Vercel if you use admin stats. |

Redeploy after saving. Check **`/actuator/health`** → `UP`.

---

## 4. Vercel: frontend

| Variable | Value |
|----------|--------|
| `VITE_API_URL` | Your Render service URL, **no** trailing slash. |
| `VITE_CLERK_PUBLISHABLE_KEY` | Production Clerk key. |
| `VITE_BRIDGE_ADMIN_KEY` | Same as `BRIDGE_ADMIN_KEY` if used. |

Redeploy after env changes.

---

## 5. Quick checks

- Railway Data tab: tables exist and are empty until users sign up.
- Render logs: no `Access denied` / `Communications link failure`.
- Browser: app loads; sign-in and one API call succeed.

---

## 6. Costs / security note

Public TCP from Render → Railway may count as **egress** on Railway. Restrict MySQL user to the app password; rotate via Railway’s credentials flow if you use it ([Credentials](https://docs.railway.com/databases/database-view) patterns in their DB UI docs).
