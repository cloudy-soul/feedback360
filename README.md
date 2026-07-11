# FeedBack360

Full-stack training feedback platform integrated with TalentUp.

## Project structure

```
feedback360-backend/           ← Spring Boot backend (port 8081)
└── feedback360-frontend/      ← Angular frontend (port 4200)
```

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+ / npm 9+
- A running PostgreSQL 15+ instance (local install or remote server)

## Database setup

Create the database and role manually (adjust names/credentials as needed):

```sql
CREATE DATABASE feedback360;
CREATE USER feedback360 WITH PASSWORD 'feedback360';
GRANT ALL PRIVILEGES ON DATABASE feedback360 TO feedback360;
```

Connection settings are read from environment variables with sensible local defaults in `application.yaml`:

| Variable | Default |
|---|---|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5434` |
| `DB_NAME` | `feedback360` |
| `DB_USER` | `feedback360` |
| `DB_PASSWORD` | `feedback360` |

Override any of them to point at your actual Postgres server:

```bash
export DB_HOST=locqlhost
export DB_PORT=5434
export DB_NAME=postgres
export DB_USER=postgres
export DB_PASSWORD=
```

Flyway runs automatically on startup and creates the schema (`V1__init_schema.sql`, `V2__seed_data.sql`).

## Running locally

### 1. Start the backend
```bash
mvn clean spring-boot:run
```

### 2. Start the Angular frontend
```bash
cd feedback360-frontend
npm install
npm start
```

Open `http://localhost:4200`.

All Angular API calls (`/api/**`) are proxied to the Spring Boot backend via `proxy.conf.json` — no CORS issues in development.

## Email (optional, for testing real sends)

By default `app.mail.impl` is `smtp`. Set your credentials before running:

```bash
export SMTP_USERNAME="your.email@outlook.com"
export SMTP_PASSWORD="your-app-password"
```

To fall back to console-only logging instead of sending real emails, set `MAIL_IMPL=console`.

## Default credentials
- Email: `admin@feedback360.local`
- Password: `Admin123!`
