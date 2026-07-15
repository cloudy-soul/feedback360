# FeedBack360 — Angular Frontend

## Prerequisites
- Node.js 18+
- npm 9+
- Spring Boot backend running on port 8081

## Setup

```bash
npm install
```

## Run

```bash
npm start
# or
ng serve
```

App runs on `http://localhost:4200`.
All `/api/**` and `/mock-talentup/**` requests are proxied to `http://localhost:8081` via `proxy.conf.json`.

## Build for production

```bash
ng build
```

## Project structure

```
src/app/
├── core/
│   ├── models/          ← TypeScript interfaces for all entities
│   ├── services/        ← AuthService, FeedbackService, AdminService, DashboardService
│   ├── guards/          ← authGuard, adminGuard, managerGuard (with returnUrl support)
│   └── interceptors/    ← authInterceptor (withCredentials on every request)
├── features/
│   ├── auth/login/      ← Login page with returnUrl handling (email link flow)
│   ├── admin/           ← Users, Logs (10/20/50 pagination), Settings
│   ├── employee/        ← My Modules, Feedback Form, Feedback Detail
│   └── manager/         ← Dashboard (4 charts), Feedback Browse/Export
└── shared/navbar/       ← Role-aware navbar
```

## Email link flow

1. TalentUp sync creates a completion
2. Email is sent with link: `http://localhost:4200/feedback/submit?moduleId=X`
3. Employee clicks → authGuard catches → redirects to `/login?returnUrl=/feedback/submit?moduleId=X`
4. Employee logs in → LoginComponent reads returnUrl → navigates directly to the form
5. Employee submits → redirected to `/my-modules`
