# Feedback360 — Project Context

Full-stack 360° training-feedback platform integrated with an external LMS ("TalentUp"). Employees complete training modules in TalentUp; this app is notified of completions, emails the employee a feedback link, collects a mandatory rating (0–10) + comment, and gives managers/HR analytics on response rates and ratings.

## Stack

- **Backend**: Spring Boot 3.5.15, Java 17, Maven. Port `8081`.
- **Frontend**: Angular 17 (standalone components, signals), Bootstrap 5, Chart.js via ng2-charts. Port `4200`.
- **Database**: PostgreSQL, schema owned by **Flyway** (`ddl-auto: validate`, never Hibernate auto-DDL).
- **Auth**: Stateless JWT in an HttpOnly cookie (`fb360_token`), no server sessions.
- **Mail**: plain SMTP via Spring's `JavaMailSender` (`spring-boot-starter-mail`), provider-agnostic — configured against SendGrid's SMTP relay for this POC (see "Mail delivery" below); a console-logging mock impl for local dev. Swappable via `app.mail.impl` (`smtp` | `console`).
- **External integration**: TalentUp (LMS) — polled on a cron; a mock TalentUp API is built into this same backend for local dev (`/mock-talentup/api/**`).

Dev proxy: Angular's `proxy.conf.json` forwards `/api/**` and `/mock-talentup/**` to `localhost:8081`, so no CORS handling is needed in dev.

## Repository layout

```
src/main/java/com/feedback/feedback360/
├── config/          CorsConfig, SecurityConfig
├── controllers/      REST endpoints (see below)
├── dto/              Request/response DTOs (records + a couple of classes)
├── entities/          JPA entities
├── enums/            FeedbackStatus, NotificationStatus, NotificationType, QuestionType, Role
├── repositories/     Spring Data JPA repos
├── repository/spec/  JPA Specifications (dynamic filtering for logs & feedback browse)
├── security/         JwtUtil, JwtCookieFilter, AppUserDetailsService
├── services/         Business logic (Feedback, Dashboard, Mail, SystemSetting, Reminder scheduler, User, QuestionFeedback)
│   ├── impl/          ConsoleMailServiceImpl, SmtpMailServiceImpl (JavaMailSender/SMTP)
│   └── talentup/      TalentUpClient, TalentUpSyncService, DTOs for the TalentUp contract
src/main/resources/
├── application.yaml
├── db/migration/     V1__init_schema.sql, V2__seed_data.sql
└── mock-data/        talentup-completions.json (fixtures for the mock TalentUp API)

feedback360-frontend/src/app/
├── core/
│   ├── guards/        authGuard, adminGuard, managerGuard
│   ├── interceptors/  authInterceptor (forces withCredentials on every request)
│   ├── models/        TS interfaces mirroring backend DTOs
│   └── services/      AuthService, AdminService, DashboardService, FeedbackService
├── features/
│   ├── auth/login
│   ├── employee/      my-modules, feedback-form, feedback-detail
│   ├── manager/       dashboard (charts), feedbacks (browse/filter/export)
│   └── admin/         users, logs, settings
└── shared/navbar
```

## Domain model (V1__init_schema.sql)

- **users** — id, name, email (unique), password_hash (BCrypt), role (`EMPLOYEE|MANAGER|ADMIN`), department, active flag, timestamps.
- **module_formation** — a TalentUp training module completion, one row per (user, talentup_module_id) — `uq_module_user_talentup` prevents double-import. Carries denormalized TalentUp parcours/population names.
- **question_feedback** — a dynamic question bank (rating/text/choice) with ordering + active flag. **Currently unused by the live flow** — the product decision was simplified to a fixed rating+comment pair, but the entity/service/table are still in the codebase (see "Known inconsistencies" below).
- **feedback** — one row per (user, module) — `uq_feedback_user_module`. Status `NOT_SUBMITTED → IN_PROGRESS → SUBMITTED`. Rating (0–10) + comment, both mandatory at submit time.
- **response_feedback** — would hold per-question answers if the dynamic question bank were wired up; currently dead weight.
- **notification** — tracks the invite/reminder email sent for a module, status `SENT|READ|RESPONDED|FAILED`, `reminder_count`.
- **integration_log** — audit trail for TalentUp sync runs and email sends (`POLL_RUN`, `POLL_ITEM_SAVED`, `POLL_ITEM_SKIPPED`, `POLL_ITEM_ERROR`, etc.), filterable/paginated in the admin UI.
- **system_setting** — key/value store; currently used for `reminder.delay.days` and `email.template.reminder`.

Seed data (V2): one admin user (`admin@feedback360.local` / `Admin123!`) and the two default system settings.

## Auth & security

- `POST /api/auth/login` — verifies email+BCrypt password, issues a JWT (email, role, userId claims) in an HttpOnly cookie, 8h expiry. Returns `{role, userId, fullName}` for Angular routing (never leaks whether email or password was the wrong part on failure).
- `POST /api/auth/logout` — clears the cookie.
- `JwtCookieFilter` reads the cookie once per request, and — if valid — sets the `Authentication` principal to the **raw `userId` (Long)**, with authority `ROLE_<role>`. Controllers inject the current user via `@AuthenticationPrincipal Long userId`.
- `SecurityConfig`: CSRF disabled (stateless JWT API), stateless sessions, route-based authorization:
  - `/api/auth/**`, `/actuator/health,info`, `/swagger-ui/**`, `/v3/api-docs/**`, `/mock-talentup/**` → public
  - `/api/admin/**` → `ROLE_ADMIN`
  - `/api/manager/**` → `ROLE_MANAGER` or `ROLE_ADMIN`
  - `/api/feedback/**` → `ROLE_EMPLOYEE`, `ROLE_MANAGER`, or `ROLE_ADMIN`
  - everything else → authenticated
- `AppUserDetailsService` exists (implements `UserDetailsService`) but the actual login path in `AuthController` does its own manual lookup/password check rather than going through `AuthenticationManager` — this class looks currently unused by the live login flow.

## Key flows

**TalentUp sync** (`TalentUpSyncService`, cron `app.talentup.poll-cron`, default every 30 min):
1. Fetch completions since last poll from `TalentUpClient` (real or mock backend).
2. For each completion: match user by email (auto-creates a placeholder EMPLOYEE user with an unmatchable password hash if not found — admin must set a real password later); dedup by `(user, talentup_module_id)`; save `ModuleFormation` (immutable once synced); create a `NOT_SUBMITTED` `Feedback` row and a `Notification`; send the invite email.
3. Per-item errors are logged via a `REQUIRES_NEW` transaction so a failure on one item doesn't roll back the log entry or block the rest of the batch.
4. Admin can trigger an immediate sync via `POST /api/admin/integration/sync-now`.

**Reminders** (`ReminderSchedulerService`, cron `app.reminders.cron`, default daily 08:00): finds `Notification`s still `SENT` past `reminder.delay.days`, re-sends via `MailService`, bumps `reminder_count`.

**Feedback submission** (`FeedbackService.submit`): idempotent per (user, module); rejects if already `SUBMITTED`; validates rating 0–10 and non-blank comment (defense in depth even though the DTO is `@Valid`-checked); marks related notifications `RESPONDED`.

**Manager dashboard** (`DashboardService`): builds one `DashboardDTO` with total/submitted/response-rate, avg rating, top-5/bottom-5 modules by rating, submissions by department, and a monthly rating trend (native Postgres `to_char` query — explicitly not portable, but project is Postgres-only).

**Manager feedback browse/export**: `Specification`-based dynamic filtering (module, department, date range, min rating, status) with pagination, plus a CSV export endpoint (`ManagerFeedbackController.export`).

**Admin logs**: paginated, filterable integration log viewer; page size locked to 10/20/50 (anything else silently defaults to 20).

## Frontend notes

- Standalone Angular components, lazy-loaded per route in `app.routes.ts`.
- `AuthService` holds role/userId/fullName as signals, persisted to `sessionStorage` (not cookies — the JWT itself is the HttpOnly cookie handling actual auth; this is just UI state).
- Route guards (`authGuard`, `adminGuard`, `managerGuard`) gate `/admin/**` and `/manager/**`.
- `authInterceptor` forces `withCredentials: true` globally so the JWT cookie rides along; some service calls also pass `withCredentials: true` explicitly (redundant but harmless).
- Charts: Chart.js/ng2-charts on the manager dashboard.

## Known inconsistencies / in-progress state (from code + commit history)

- **Dynamic question bank is vestigial**: `QuestionFeedback`, `ResponseFeedback`, `QuestionFeedbackService`, and their table/repo still exist, but the product pivoted to a fixed rating+comment model (per `FeedbackService`'s own comment: "No dynamic question bank; confirmed requirement, admin Questions tab removed"). Nothing in the current controllers/frontend writes to `response_feedback`.
- **Two mail backends checked in**: `ConsoleMailServiceImpl` (default/local) and `SmtpMailServiceImpl` (plain SMTP via `JavaMailSender`, `app.mail.impl=smtp`). Commit history mentions an earlier attempt with a real Gmail account, then a detour through the Mailtrap Java SDK — both abandoned; see "Mail delivery" below for the current setup and why.
- **`MailPropertyDebug.java`** at the top-level package — a debug helper that dumps the mail config actually in effect at startup (impl, SMTP host/port/username, from-address) — useful, kept intentionally rather than removed.
- Per the last commit message, the employee flow was "almost complete" with the remaining concern being verifying that JWT/security correctly redirects an employee to their own past feedback (worth double-checking `FeedbackController.detail`'s owner-vs-manager access check and the `/feedback/:id` route guard).
- `create-branches.sh` / `BRANCH_README.md` suggest a planned feature-branch-per-chapter workflow (`01-scaffold` … `08-admin-settings`) that doesn't seem to be in active use on `dev` currently.
- `users.json` at repo root looks like an ad-hoc DB export/dump, not application config — likely should not be committed long-term.
- Local Postgres defaults are inconsistent between `application.yaml` (`DB_USER=postgres`, `DB_PASSWORD=Rime2006`) and `README.md` (`DB_USER=feedback360`, `DB_PASSWORD=feedback360`) — whichever env vars are actually exported locally wins.

## Default local credentials
- `admin@feedback360.local` / `Admin123!`

## Mail delivery — investigation history and current state

The invite/reminder email pipeline went through several iterations before landing on a working setup:

1. **Real Gmail account via JavaMailSender** (earliest attempt, per commit history) — abandoned; personal Gmail SMTP is throttled/flagged for automated server-side sending.
2. **Mailtrap Java SDK** (`io.mailtrap:mailtrap-java`) — `SmtpMailServiceImpl` called Mailtrap's REST API directly. This silently failed for two stacked reasons: (a) `MAILTRAP_TOKEN` was never actually set, so the client never initialized and every send short-circuited to failure while logging as if nothing was wrong (the code didn't check `SendResponse.isSuccess()`); (b) the SMTP host in use (`sandbox.smtp.mailtrap.io`) was Mailtrap's **Email Testing/sandbox product**, which architecturally never delivers to real inboxes regardless of configuration — a product mismatch, not just a config bug.
3. **Current: plain SMTP via `JavaMailSender`** (`spring-boot-starter-mail`, already a dependency, previously unused) **against SendGrid's Sending API SMTP relay** (`smtp.sendgrid.net:587`, username literally `apikey`, password = a SendGrid API key). The `mailtrap-java` dependency was removed entirely. This is provider-agnostic — swapping SMTP host/credentials (Mailtrap Sending, Gmail, SES, Postmark, etc.) requires no code change.

**Why SendGrid + Single Sender Verification (not full domain auth)**: this is a POC with no owned/verifiable domain, so full domain authentication (SPF/DKIM/DMARC via DNS) isn't available. SendGrid's **Single Sender Verification** lets you verify one email address you own (via a confirmation-link click) and send to *any* real recipient without owning a domain — unlike Resend (unverified accounts can only send to the account owner's own address) or AWS SES sandbox (every recipient must also be individually verified), which don't fit this use case.

**Known, expected limitation**: mail sent this way reliably lands in **spam** for real recipients, and can be silently dropped entirely by hardened corporate mail security. Confirmed during testing:
- Sending `From: <verified-address>@capgemini.com` **to** a `@capgemini.com` recipient (i.e. using the recipient's own corporate domain as sender) got accepted by SendGrid (Activity Feed showed "Delivered") but never reached the inbox and produced no bounce — consistent with Microsoft 365/Exchange Online Protection's anti-impersonation handling silently dropping mail that claims to originate from the recipient's own domain without SPF/DKIM authorization. **Avoid using the recipient's own corporate domain as the verified sender.**
- Sending from a verified personal Gmail address to a `@capgemini.com` recipient triggered `550 The from address does not match a verified Sender Identity` until the sender was actually confirmed in SendGrid's dashboard (pending vs. verified state matters).
- Sending Gmail-to-Gmail (verified sender = recipient, both `@gmail.com`) succeeded but landed in **spam**, not inbox — expected without domain authentication; not further fixable without a real, DNS-verifiable domain.
- This spam-folder outcome is considered acceptable for POC purposes.

**Env vars for local runs**: `MAIL_IMPL=smtp`, `SMTP_HOST` (default `smtp.sendgrid.net`), `SMTP_PORT` (default `587`), `SMTP_USERNAME` (default `apikey`), `SMTP_PASSWORD` (the SendGrid API key — never commit this), `MAIL_FROM_ADDRESS` (must exactly match a **Verified** Single Sender in the SendGrid account being used).

**Observability added**: `SmtpMailServiceImpl` logs distinct causes for `MailAuthenticationException` (bad credentials) vs. `MailSendException` (relay-level rejection, e.g. sender-identity mismatch — the actual SMTP error text from the provider is included) vs. parse/prep errors. `TalentUpSyncService` and `ReminderSchedulerService` now also write `EMAIL_SENT`/`EMAIL_FAILED`/`REMINDER_SENT`/`REMINDER_FAILED` rows to `integration_log` (previously declared in the entity's own doc comment as expected types but never actually written), making send outcomes visible through the existing admin Logs screen, not just console output.

---

## Original project brief (client spec, translated summary)

**Goal**: build a web app to collect, analyze, and centralize structured, actionable feedback from employees at the end of each training module, integrated with the TalentUp LMS API. Today feedback is informal/unusable; users should be auto-notified by email to evaluate a module they completed.

**User roles & responsibilities**
- **Collaborateur (Employee)**: receives an email notification, accesses the completed module, gives structured feedback.
- **Manager / HR**: views feedbacks, analyzes results, sees KPIs/statistics.
- **Administrateur**: configures settings (questions, modules, API integration), manages users, supervises the app.

**Core entities (as specified)**: Utilisateur (id, nom, prénom, email, rôle, département), ModuleFormation (id, titre, description, catégorie, dateCompletion, source), Feedback (id, utilisateur, module, noteGlobale, commentaire, statut, createdAt), QuestionFeedback (id, libellé, type [notation/texte/choix], obligatoire), RéponseFeedback (id, feedback, question, valeur), Notification (id, utilisateur, module, dateEnvoi, statut [envoyé/lu/répondu]), IntegrationLog (id, type, statut, message, date).

**Specified features**: real-time TalentUp sync; automatic invite email on module completion; **dynamic** feedback form with configurable questions (ratings 0–10 + mandatory comments); manager browsing by module/user/period; dashboard KPIs; automatic reminders after X days (manager+admin configurable); search/filter (module, date, department, rating, status); user/module administration; full action audit trail (feedback submission, edits, notification sends).

**Business rules (RG-FB-01 → 09)**
| ID | Rule |
|---|---|
| RG-FB-01 | Every module completed in TalentUp must be imported |
| RG-FB-02 | A user gets a notification for every completed module |
| RG-FB-03 | Only one feedback allowed per (user, module) |
| RG-FB-04 | Certain fields are mandatory before submission |
| RG-FB-05 | Feedback status: Not submitted / In progress / Submitted |
| RG-FB-06 | A reminder is sent after a configurable delay if not submitted |
| RG-FB-07 | Access to data is restricted by role |
| RG-FB-08 | Data coming from TalentUp is never editable |
| RG-FB-09 | Every action must be logged (date, user) |

**Target workflow**: module completed in TalentUp → API polled/event received → module completion recorded → invite email sent → user opens Feedback360 → fills & submits feedback → feedback recorded and becomes exploitable → manager reviews in dashboard.

**Expected screens**: login, global dashboard, list of completed modules, feedback form, feedback detail, analytics dashboard, question administration, user management, integration log (API) viewer.

**Dashboard KPIs (specified)**: response rate, average rating per module, top-rated modules, lowest-rated modules, feedback breakdown by department, rating trend over time, count of pending feedbacks.

**TalentUp integration expectations**: consumes an external REST API; authentication; periodic or real-time sync; error handling (timeouts, invalid data); integration logs for audit/monitoring.

**Non-functional requirements**: responsive web app; response time < 2s; security (auth, access control, HTTPS); scalability for concurrent users; high availability; GDPR compliance for personal data.

## Spec vs. current implementation — gap analysis

| Spec item | Status |
|---|---|
| RG-FB-01/02/03/05/08 (sync, notify, unique, status, TalentUp immutability) | ✅ Implemented — see `TalentUpSyncService`, `Feedback` status enum, `uq_feedback_user_module` / `uq_module_user_talentup` constraints |
| RG-FB-04 (mandatory fields) | ✅ Implemented, but simplified — rating + comment are the *only* fields, both hard-required (`FeedbackService.submit`) |
| RG-FB-06 (configurable reminders) | ✅ Implemented — `ReminderSchedulerService` + `system_setting.reminder.delay.days`, cron via `app.reminders.cron` |
| RG-FB-07 (role-based access) | ✅ Implemented — `SecurityConfig` route rules + per-endpoint owner/role checks (e.g. `FeedbackController.detail`) |
| RG-FB-09 (full audit trail) | ⚠️ Partial — `integration_log` covers TalentUp sync + email events, but there's no generic audit log for feedback edits/user-management actions beyond `created_at`/`updated_at` timestamps |
| **Dynamic, configurable question form** | ❌ Not implemented as spec'd — the brief calls for a configurable question bank (notation/texte/choix) rendered as a dynamic form; the app pivoted to a fixed rating(0–10)+comment pair. `QuestionFeedback`/`ResponseFeedback`/`QuestionFeedbackService` still exist in the codebase but are wired to nothing (no controller, no frontend "Administration des questions" screen) |
| **"Administration des questions" screen** | ❌ Missing — explicitly dropped per commit history ("removed the admin Questions tab") |
| Real-time TalentUp sync | ⚠️ Polling only (cron every 30 min by default), not event-driven/real-time |
| Manager browse by module/user/period + filters | ✅ Implemented — `ManagerFeedbackController` + `FeedbackSpecifications` (module, department, date range, rating, status) |
| Dashboard KPIs | ✅ All 7 specified KPIs present in `DashboardDTO`/`DashboardService` (response rate, avg rating, top/bottom modules, by-department, trend, pending count) |
| CSV export | ➕ Extra, not in original spec — `ManagerFeedbackController.export` |
| Non-functional: HTTPS | ⚠️ Not configured — cookie's `secure` flag is commented out (`AuthController`, "enable once served over HTTPS") |
| Non-functional: GDPR | ⚠️ Not explicitly addressed anywhere in the code (no data export/erasure endpoints, no consent handling) |
| Non-functional: response time / scalability / availability | Not verifiable from code alone — no load testing, caching, or HA config present |

This gap analysis will need revisiting once more requirements/context are provided.
