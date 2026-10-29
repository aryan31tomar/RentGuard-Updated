# RentGuard: start here

## Important security note

The values in `.env.example` and the Compose defaults are for local development only. Before deploying publicly, set strong unique `MYSQL_ROOT_PASSWORD` and `JWT_SECRET` values in your hosting provider's secret/environment settings. Never commit `.env`. If the old credentials from your previous project have already been pushed to a public repository, rotate them.

## Run locally with Docker Desktop

1. Install and open Docker Desktop. Wait until Docker says it is running.
2. In Terminal, change directory into this `RentGuard` folder.
3. Copy `.env.example` to `.env` (Finder can do this too, but ensure the copied file is named `.env`).
4. Start the app with `docker compose up --build`.
5. Open http://localhost:8080.
6. To stop it, press `Ctrl+C`, then run `docker compose down`. Database data persists in the Docker volume.

The default Spring profile uses an in-memory H2 database when run directly without the MySQL profile. Docker Compose explicitly activates the MySQL profile.

## Existing data

The MySQL schema file is mounted for initialization of a fresh MySQL data volume. MySQL only runs initialization scripts when its data directory is empty. If you already have a database volume with data, this script will not automatically rerun. Do not run `docker compose down -v` unless you are okay deleting the local database volume and its data.

## Production deployment

Do not deploy the development credentials. Configure a managed MySQL database and set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and a newly generated `JWT_SECRET` in the hosting provider. The `DB_URL` should be the JDBC URL supplied by your database provider. Configure the app to use the `mysql` Spring profile. Review file-upload storage before production because files stored inside a container may not persist across redeploys.
