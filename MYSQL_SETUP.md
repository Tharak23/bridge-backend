# MySQL install and schema (no Docker for the database)

Use a normal **MySQL 8+** server on your machine or in the cloud. The Spring Boot app connects with JDBC; it does **not** auto-create tables (`ddl-auto=none`).

The **`Dockerfile`** in this repo only builds and runs the **Java backend** (e.g. on Render). It does **not** install MySQL.

---

## Which SQL files to use

| File | Use it? |
|------|---------|
| **`mysql_schema.sql`** | **Yes.** Run once on a new database (includes `custom_work_request` + `custom_work_application`). |
| **`mysql_migration_custom_work.sql`** | **Additive only.** If you already applied an older `mysql_schema.sql` without custom-work tables, run this file once — it does not change existing tables. |
| `supabase_schema.sql` | **No** (legacy Postgres / Supabase). Do not run on MySQL. |
| `supabase_migration_booking.sql` | **No** (legacy). Booking is already included in `mysql_schema.sql`. |

Keep `mysql_schema.sql` in the repo and re-run it only on a **new** empty database. To change schema later, use new migration SQL or ALTER statements (do not duplicate the full script on a DB that already has tables unless you intend to recreate).

---

## MySQL Workbench — step by step

Workbench is only the **GUI**; MySQL Server must already be running (installed with MySQL or via Homebrew). You will create **one** database for this app: **`bridge`**.

### 1. Open a connection

1. Open **MySQL Workbench**.
2. Under **MySQL Connections**, click your local instance (often **Local instance MySQL80** or **127.0.0.1:3306**).
3. Enter the password you set for **root** (or the user shown on the tile) → **OK**.

If you have no connection yet: **+** next to “MySQL Connections” → Connection Name: `Local` → Hostname `127.0.0.1`, Port `3306`, Username `root` → **Test Connection** → **OK**.

### 2. Create the database (schema)

1. After the SQL tab opens, click **Create a new schema** in the toolbar (cylinder + **+**), or run in the query editor:

   ```sql
   CREATE DATABASE bridge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Click the **lightning** icon to **execute** the statement (or `Cmd+Enter` / `Ctrl+Enter`).
3. In the left **Schemas** panel, click **Refresh** (circular arrows). You should see **`bridge`**.

**What to keep:** For this project you only need the **`bridge`** schema. Default schemas like `sys`, `mysql`, `performance_schema` are part of MySQL — **do not delete those**. You can ignore them.

### 3. (Recommended) Create an app user

Still in a query tab (while connected as `root`), run (change the password):

```sql
CREATE USER 'bridge'@'localhost' IDENTIFIED BY 'your_password_here';
GRANT ALL PRIVILEGES ON bridge.* TO 'bridge'@'localhost';
FLUSH PRIVILEGES;
```

Execute. Your Spring Boot **`.env`** will use `MYSQL_USER=bridge`, `MYSQL_PASSWORD=your_password_here`, `MYSQL_DATABASE=bridge`.

*Local shortcut:* you *can* use `root` in `.env` for development only; a dedicated `bridge` user is safer.

### 4. Load the Bridge tables from `mysql_schema.sql`

1. In Workbench: **File → Open SQL Script…**
2. Browse to your repo file: **`bridge-backend/mysql_schema.sql`**
3. A new tab opens with the script. In the toolbar, set the **default schema** to **`bridge`** (dropdown that says “No schema selected” → choose **`bridge`**), or add at the very top of the script (once) and execute first:

   ```sql
   USE bridge;
   ```

4. Click **Execute** (lightning icon) to run the **whole** script.

5. In the left panel: **Schemas** → **`bridge`** → **Tables** → you should see **`bridge_user`**, **`service_provider`**, **`booking`**.

If Workbench says **table already exists**, the script was already applied. Do not run it again on the same database unless you intentionally dropped the tables first.

### 5. Point the Java app at this database

In **`bridge-backend/.env`** (copy from `.env.example` if needed):

```env
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DATABASE=bridge
MYSQL_USER=bridge
MYSQL_PASSWORD=your_password_here
MYSQL_USE_SSL=false
```

Then run: `set -a && source .env && set +a && ./mvnw spring-boot:run` and open `http://localhost:8080/actuator/health`.

---

## Install MySQL (pick your OS)

### macOS (Homebrew)

```bash
brew install mysql
brew services start mysql
```

Default install: `root` with no password or a password you set during first login. Connect:

```bash
mysql -u root -p
```

### macOS / Windows / Linux — MySQL Installer

Download **MySQL Community Server 8** from [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/) and follow the wizard. Remember the **root** password you choose.

### Ubuntu / Debian

```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo mysql   # or mysql -u root -p
```

---

## Create database and user

Log in as a privileged user (`mysql -u root -p`), then run (adjust password and names if you like):

```sql
CREATE DATABASE bridge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'bridge'@'localhost' IDENTIFIED BY 'choose_a_strong_password';
GRANT ALL PRIVILEGES ON bridge.* TO 'bridge'@'localhost';
FLUSH PRIVILEGES;
```

If the app runs on another host (e.g. Render hitting a remote MySQL), create the user for remote access, for example:

```sql
CREATE USER 'bridge'@'%' IDENTIFIED BY 'choose_a_strong_password';
GRANT ALL PRIVILEGES ON bridge.* TO 'bridge'@'%';
FLUSH PRIVILEGES;
```

(Use `%` only when required and lock down by IP or VPN if your host allows it.)

---

## Apply the schema (run `mysql_schema.sql`)

From your machine, in the `bridge-backend` folder:

```bash
mysql -u bridge -p bridge < mysql_schema.sql
```

Or from inside the MySQL client after `USE bridge;`:

```bash
mysql -u bridge -p bridge
```

```sql
SOURCE /full/path/to/bridge-backend/mysql_schema.sql;
```

You should see no errors. Verify:

```sql
USE bridge;
SHOW TABLES;
-- Expect: booking, bridge_user, service_provider
```

---

## Point the Spring Boot app at MySQL

Copy `.env.example` to `.env` and set:

```env
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DATABASE=bridge
MYSQL_USER=bridge
MYSQL_PASSWORD=choose_a_strong_password
MYSQL_USE_SSL=false
```

For local MySQL on the same machine, `MYSQL_USE_SSL=false` is typical. For managed cloud MySQL, set `MYSQL_USE_SSL=true` if the provider requires TLS.

Start the API:

```bash
set -a && source .env && set +a && ./mvnw spring-boot:run
```

Check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## Production (Render, etc.)

Same schema: run **`mysql_schema.sql`** once against the production database, then set `MYSQL_*` (and `MYSQL_USE_SSL` as required) on your web service. See `PRODUCTION_SETUP.md`.

---

## Troubleshooting

- **`Could not open JPA EntityManager for transaction`** — MySQL not running, wrong `MYSQL_*` in `bridge-backend/.env`, tables missing (re-run `mysql_schema.sql`), or an old **`application-local.properties`** in `bridge-backend/src/main/resources/` still pointing at Postgres. Delete or fix that file; the app no longer enables the `local` profile by default, but a stray file can still confuse you if you add the profile back.
- **`Access denied`** — Wrong user/password or user not granted on `bridge.*`.
- **`Unknown database 'bridge'`** — Run `CREATE DATABASE bridge ...` first.
- **`Table already exists`** — Schema was already applied; do not re-run the full `mysql_schema.sql` unless you are resetting a dev database (drop tables or recreate the database first).
- **MySQL 8 auth errors from Java** — The JDBC URL in `application.properties` includes `allowPublicKeyRetrieval=true` for common local setups. For production, prefer proper SSL and users created with compatible auth plugins per your host’s docs.
