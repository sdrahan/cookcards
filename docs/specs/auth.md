# Auth and Access Control — Snapshot Spec

Last verified against code: 2026-02-22.

## Purpose
Define current authentication behavior, access control, and auth-related UX for the server-rendered web app.

## In scope
- Landing, login, signup, logout
- Redirect behavior for authenticated vs unauthenticated users
- Signup validation and account creation behavior
- Login failure/success behavior

## Out of scope
- Password reset
- Email verification
- OAuth / social login
- Rate limiting / lockout

## Route access model

### Public routes
- `GET /`
- `GET /login`
- `GET /signup`
- `POST /login` (Spring Security login processing)
- `POST /signup`
- Static: `/css/**`, `/favicon.ico`, `/favicon.svg`

### Authenticated routes
All other routes require authentication.

## Redirect and access behavior
- Authenticated user visiting `/`, `/login`, or `/signup` is redirected to `/recipes`.
- Unauthenticated access to protected routes is redirected to `/login` by Spring Security.
- Successful login always redirects to `/recipes`.
- Failed login redirects to `/login?error`.
- Logout invalidates session and redirects to `/`.

## Screen behavior

### Landing (`GET /`)
- Template: `landing.html`
- Content: app name and links to login/signup.

### Login (`GET /login`, `POST /login`)
- Template: `login.html`
- Form fields:
  - `username` (email)
  - `password`
- CSRF token is required and included in the form.
- Error state shown when query param `error` is present:
  - `Invalid email or password.`

### Signup (`GET /signup`, `POST /signup`)
- Template: `signup.html`
- Form object: `SignupForm(email, password)`.
- Validation rules:
  - Email: required, valid email format
  - Password: required, minimum 8 characters
- Duplicate email error:
  - `An account with this email already exists.`
- On successful signup:
  1. Email is normalized (trim + lowercase).
  2. User is created with role `USER` and default settings record.
  3. Password is stored as BCrypt hash.
  4. User is authenticated immediately.
  5. Redirect to `/recipes`.

## Data and security constraints
- User email is unique at DB level.
- User UUID is generated on creation.
- Passwords are never stored in plaintext.
- Session auth uses Spring Security form login.
- Logout endpoint is `POST /logout` (CSRF-protected form).

## Source files
- `src/main/java/app/cookcards/webapp/config/SecurityConfig.java`
- `src/main/java/app/cookcards/webapp/controller/AuthController.java`
- `src/main/java/app/cookcards/webapp/user/UserService.java`
- `src/main/java/app/cookcards/webapp/user/AppUserDetailsService.java`
- `src/main/resources/templates/landing.html`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/signup.html`
- `src/main/resources/db/migration/V01__initial_schema.sql`
