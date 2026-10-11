# machine-scheduler

A full stack app for managing users, permissions and simulated machines. Users create machines, start, stop and restart them, and schedule any of those operations for later. It began as a university assignment (RAF, homework 3).

## Features

- **Users and permissions**: create, list, edit and delete users, each with their own set of permissions that gates every screen and endpoint.
- **Machines**: create, search (by name, status and creation date) and delete your own machines. A machine is either `STOPPED` or `RUNNING`.
- **Operations**: start, stop and restart run in the background and take 10–15 seconds to simulate real work. A machine that is in the middle of an operation rejects further requests.
- **Scheduling**: schedule a start, stop or restart for a future date and time.
- **Error log**: scheduled operations that fail (for example, starting a machine that is already running) are recorded and shown on the Logs page.

## Tech stack

| Part | Stack |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA, jjwt |
| Frontend | Angular 21, Bootstrap 5 |
| Database | MySQL 8.4 |

Authentication is a JWT stored in an HttpOnly cookie. State changing requests, login included, must also carry a CSRF token: fetch it from `GET /auth/csrf` and send it back in the `X-XSRF-TOKEN` header. The frontend does this automatically.

## Running locally

You need Java 21, Node.js 22 and Docker.

1. Start the database:

   ```bash
   docker compose up -d
   ```

2. Start the backend (http://localhost:8081):

   ```bash
   cd backend && ./mvnw spring-boot:run
   ```

3. Start the frontend (http://localhost:4200):

   ```bash
   cd frontend && npm install && npm start
   ```

4. Open http://localhost:4200 and sign in as `admin@mail.com`. Its password is the `app.seed.admin-password` value in [application.properties](backend/src/main/resources/application.properties), which also holds the password prefix for the 20 sample users.

The schema is dropped and reseeded on every backend start, so data does not survive a restart. Sample users get random permissions and sample machines get random owners.

## Configuration

The backend reads these environment variables; the defaults suit local development.

| Variable | Default | Purpose |
|---|---|---|
| `SERVER_PORT` | `8081` | Backend port. The frontend expects 8081 (see `frontend/src/environments/`). |
| `DB_URL` | `jdbc:mysql://localhost:3306/machine_scheduler...` | JDBC connection string. |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `root` | Database login, matching `docker-compose.yml`. |
| `DDL_AUTO` | `create` | Hibernate schema mode. Changing it requires guarding the seeding first. |
| `SEED_ADMIN_PASSWORD` | development value | Password of the seeded admin account. |
| `SEED_USER_PASSWORD_PREFIX` | development value | Sample users get this prefix followed by their number as a password. |
| `APP_JWT_SECRET` | development key | Base64 HS512 signing key. Override it outside local development: `openssl rand -base64 64`. |

Allowed CORS origins default to `http://localhost:4200` and can be changed with `app.cors.allowed-origins`.

## Permissions

| Area | Permissions |
|---|---|
| Users | `CAN_CREATE_USER`, `CAN_READ_USER`, `CAN_UPDATE_USER`, `CAN_DELETE_USER` |
| Machines | `CAN_CREATE_MACHINE`, `CAN_SEARCH_MACHINE`, `CAN_START_MACHINE`, `CAN_STOP_MACHINE`, `CAN_RESTART_MACHINE`, `CAN_DESTROY_MACHINE` |

A missing permission returns 403. Machine endpoints only act on the caller's own machines and return 404 for anyone else's.

## API

All endpoints under `/api` require the auth cookie.

| Method | Path | Description |
|---|---|---|
| GET | `/auth/csrf` | CSRF token and the header to send it in |
| POST | `/auth/login` | Sign in and set the auth cookie |
| GET | `/auth/me` | Current user and permissions |
| POST | `/auth/logout` | Clear the auth cookie |
| GET, POST | `/api/users` | List or create users |
| GET, PUT, DELETE | `/api/users/{id}` | Read, update or delete a user |
| GET, POST | `/api/machines` | List your machines or create one |
| GET | `/api/machines/search` | Filter by `name`, `status`, `dateFrom`, `dateTo` (`dd-MM-yyyy`) |
| DELETE | `/api/machines/{id}` | Delete a stopped machine |
| PATCH | `/api/machines/{start\|stop\|restart}/{id}` | Run an operation now |
| PATCH | `/api/machines/schedule/{start\|stop\|restart}/{id}` | Schedule an operation; body `{"time": "yyyy-MM-dd HH:mm"}` |
| GET | `/api/logs` | Your failed scheduled operations |
