# Deploying cognit-backend to Railway

The backend deploys to [Railway](https://railway.com) as a Docker service, backed
by Railway's managed **PostgreSQL** and **Redis**, with a **persistent volume**
for uploaded media. A GitHub Actions workflow redeploys on every push to `master`.

---

## 1. One-time: push this repo to GitHub

This repo currently lives on GitLab. Add a GitHub remote and push:

```bash
# create an empty repo at github.com/<you>/cognit-backend first (no README)
git remote add github git@github.com:<you>/cognit-backend.git
git push github master
```

GitHub Actions runs from whichever remote is `github`. Keep pushing to both, or
make `github` the primary remote.

---

## 2. One-time: create the Railway project

1. Create a new project at [railway.com/new](https://railway.com/new).
2. **Add PostgreSQL**: *New → Database → Add PostgreSQL*.
3. **Add Redis**: *New → Database → Add Redis*.
4. **Add the backend service**: *New → GitHub Repo → `cognit-backend`*
   (or *Empty Service* if you only deploy via the Actions workflow).
   - Railway auto-detects `railway.json` + `Dockerfile`.
   - Rename the service to **`cognit-backend`** (must match `RAILWAY_SERVICE_NAME`).
5. **Add the volume**: select the backend service → *Settings → Volumes → New Volume*,
   mount path **`/app/uploads`**.
6. **Generate a domain**: backend service → *Settings → Networking → Generate Domain*.

> If Railway's own GitHub auto-deploy is enabled on the service, you may want to
> disable it (*Settings → Deploy → disconnect repo*) so the Actions workflow is the
> single deploy path and you don't get double deploys.

---

## 3. One-time: environment variables (backend service → Variables)

Use Railway [reference variables](https://docs.railway.com/guides/variables#reference-variables)
so credentials stay wired to the plugins:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}
SPRING_DATASOURCE_PASSWORD=${{Postgres.PGPASSWORD}}

REDIS_HOST=${{Redis.REDISHOST}}
REDIS_PORT=${{Redis.REDISPORT}}
REDIS_PASSWORD=${{Redis.REDISPASSWORD}}

APP_SEED_ENABLED=false

JWT_SECRET=<generate a 256-bit random string>
MAILTRAP_USERNAME=<your mailtrap user>
MAILTRAP_PASSWORD=<your mailtrap pass>
GEMINI_API_KEY=<your gemini key>

CORS_ALLOWED_ORIGINS=https://<your-frontend-domain>,https://<your-admin-domain>
```

Notes:
- `PORT` is injected by Railway automatically — do **not** set it.
- Keep `APP_SEED_ENABLED=false`. Seeding bcrypt-hashes ~1,100 users and inserts
  thousands of posts on every boot, which will time out the healthcheck.
- The Postgres/Redis plugin variable names (`PGHOST`, `REDISHOST`, …) are shown on
  each plugin's *Variables* tab — copy them exactly if they differ.

---

## 4. One-time: wire up GitHub Actions

The workflow is at `.github/workflows/deploy.yml`. It builds on every push/PR and
deploys to Railway on pushes to `master`.

In **GitHub → repo → Settings → Secrets and variables → Actions**:

| Type     | Name                   | Value |
|----------|------------------------|-------|
| Secret   | `RAILWAY_TOKEN`        | A Railway **project token**: Railway → project → *Settings → Tokens → Create Token* (scope it to the environment you deploy, usually `production`). |
| Variable | `RAILWAY_SERVICE_NAME` | `cognit-backend` (only needed if the service has a different name; the workflow defaults to `cognit-backend`). |

---

## 5. Deploy

```bash
git push github master
```

Watch the run under the repo's **Actions** tab. The `deploy` job runs
`railway up --service cognit-backend --ci`, which uploads the source, Railway
builds the `Dockerfile`, and the new deployment goes live once
`/actuator/health` returns `200`.

### Manual deploy from your machine

```bash
npm i -g @railway/cli
railway login
railway link          # pick the project + environment
railway up --service cognit-backend
```

---

## Troubleshooting

- **Healthcheck fails / deploy stuck "waiting for healthcheck"** — check the
  service **Deploy logs**. Usually Flyway can't reach Postgres (wrong reference
  variable) or seeding is still enabled. `/actuator/health` reports `DOWN` while
  Postgres or Redis is unreachable.
- **`Permission denied` writing to `/app/uploads`** — the container runs as the
  non-root `spring` user. In the volume settings confirm the mount path is exactly
  `/app/uploads`; Railway chowns the volume to the image user. If it persists,
  temporarily remove the `USER spring` line in the `Dockerfile`, redeploy once,
  then add it back.
- **Out of memory on boot** — set `JAVA_OPTS=-XX:MaxRAMPercentage=75` in the
  service variables, or bump the plan.
- **CORS errors from the frontend** — add the deployed frontend/admin origins to
  `CORS_ALLOWED_ORIGINS` (comma-separated, no trailing slash).
