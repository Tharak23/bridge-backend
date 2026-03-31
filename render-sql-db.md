# Bridge backend + MySQL on Render

This complements the official guide: **[Deploy MySQL on Render](https://docs.render.com/docs/deploy-mysql)**. Follow Render for creating the database; use this file for **Bridge schema** and **wiring the Spring Boot API**.

---

## 1. Create MySQL on Render (official steps)

1. Use Render’s **[MySQL template](https://github.com/render-examples/mysql)** (one-click or fork + **Private Service**, language **Docker**).
2. Set env vars (example names — use strong passwords):

   | Key | Example |
   |-----|--------|
   | `MYSQL_DATABASE` | `bridge` ← matches our app default |
   | `MYSQL_USER` | `bridge` or `mysql` |
   | `MYSQL_PASSWORD` | strong secret |
   | `MYSQL_ROOT_PASSWORD` | strong secret |

3. **Disk (required):** mount path must be **`/var/lib/mysql`** exactly. Wrong path → data loss. See [Render disks](https://docs.render.com/docs/disks).

4. After deploy, note the **internal URL** (e.g. `mysql-xxxxx:3306` or the service **name** Render shows). Only other services in the **same workspace** can reach it.

---

## 2. Apply Bridge SQL (schema)

**From inside the MySQL service** (Render dashboard → MySQL → **Shell**, or SSH if enabled), connect:

```bash
mysql -h localhost -D "$MYSQL_DATABASE" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD"
```

Then either:

- **New empty database:** paste/run the full repo file **`mysql_schema.sql`** (creates `bridge_user`, `service_provider`, `booking`, `custom_work_request`, `custom_work_application`), or  
- **Old DB that already has core tables but not custom work:** run **`mysql_migration_custom_work.sql`** once only.

From your laptop (if you expose nothing publicly, this usually **does not** work — prefer Render Shell):

```bash
mysql -h INTERNAL_HOST -P 3306 -u USER -p bridge < mysql_schema.sql
```

(`INTERNAL_HOST` is the private service hostname from the dashboard.)

---

## 3. Point the Bridge **Web Service** at MySQL

On your **Spring Boot** Web Service (not the MySQL service), set:

| Env var | Value |
|--------|--------|
| `MYSQL_HOST` | Internal hostname of the MySQL private service (e.g. `mysql-xxxxx` — **no** `https://`) |
| `MYSQL_PORT` | `3306` |
| `MYSQL_DATABASE` | Same as `MYSQL_DATABASE` on MySQL (e.g. `bridge`) |
| `MYSQL_USER` / `MYSQL_PASSWORD` | Same as on MySQL |
| `MYSQL_USE_SSL` | Often `false` for **internal** TCP between Render services; use `true` if your provider requires TLS |
| `ALLOWED_ORIGINS` | Your live frontend URL(s), comma-separated, **no trailing slash** |

Redeploy the web service after changing env.

---

## 4. Backups (Render’s warning)

Render states that **restoring disk snapshots is not recommended** for MySQL; prefer **[mysqldump](https://dev.mysql.com/doc/refman/8.0/en/mysqldump.html)** for backup/restore. See [Backups section](https://docs.render.com/docs/deploy-mysql#backups) in the official doc.

---

## 5. Optional: Adminer

To browse tables in a browser, Render documents deploying **[Adminer](https://docs.render.com/deploy-adminer)** on the same workspace.

---

## Quick checklist

- [ ] MySQL private service live, disk at `/var/lib/mysql`
- [ ] DB name/user/password match on **both** MySQL and Bridge web env
- [ ] `mysql_schema.sql` (or migration only) applied via **Shell** on MySQL
- [ ] `MYSQL_HOST` = internal MySQL service hostname
- [ ] `ALLOWED_ORIGINS` includes your Vercel URL
- [ ] `/actuator/health` on the API returns UP
