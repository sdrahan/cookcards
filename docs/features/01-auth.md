# Cookcards.app — Product/UX Stories
## Flow 1: First visit → Landing → Sign up / Log in (MVP)

**Decisions confirmed:**
- Auth screens are **full pages** (not modals).
- Authenticated area has a **top navigation bar**.

**Out of scope (explicit):**
- Email confirmation
- Forgot password / reset flow
- Magic link / OAuth

---

## 1) Information architecture

### Public pages
- `GET /` Landing
- `GET /signup` Sign up form
- `POST /signup` Sign up submit
- `GET /login` Log in form
- `POST /login` Log in submit

### Auth-required pages
- `GET /recipes` My recipes (empty state for new users)
- (placeholder) `GET /recipes/new` Add recipe (next flow)

### Access rules
- If user is **authenticated** and visits `/`, `/login`, or `/signup` → redirect to `/recipes`.
- If user is **not authenticated** and visits `/recipes` (or any future `/recipes/**`) → redirect to `/login`.

---

## 2) Shared layout: Top navigation (authenticated pages)

### NAV-001 — Authenticated top navigation
**User story:** As an authenticated user, I always see a top navigation bar so I can identify the app and log out.

**Applies to:**
- All auth-required pages (`/recipes`, future `/recipes/**`)

**UI requirements:**
- White background, minimal height
- Left: App name “Cookcards” linking to `/recipes`
- Right: “Log out” button/link (`POST /logout` preferred; `GET /logout` acceptable for MVP)

**Acceptance criteria:**
- Given I am authenticated, when I open any `/recipes` page, then I see a top nav.
- Given I click “Cookcards” in the nav, then I go to `/recipes`.
- Given I click “Log out”, then I am logged out and redirected to `/`.

**Notes (implementation-friendly):**
- Use a shared Thymeleaf fragment for nav and include it on authenticated templates.
- Add CSRF token handling if enabled; if CSRF complicates MVP, document it and choose the simplest safe option.

---

## 3) Screen specs (MVP)

### AUTH-001 — Landing page (unauthenticated)
**User story:** As a first-time visitor, I see a minimal landing page with options to sign up or log in.

**Route / template:**
- `GET /`
- `templates/landing.html`

**UI requirements:**
- White background
- Centered app name “Cookcards”
- Two buttons:
    - “Log in” → `/login`
    - “Sign up” → `/signup`
- No other content on the page

**Acceptance criteria:**
- Given I am not authenticated, when I open `/`, then I see only:
    - “Cookcards”
    - “Log in” button linking to `/login`
    - “Sign up” button linking to `/signup`
- Given I am authenticated, when I open `/`, then I am redirected to `/recipes`.

---

### AUTH-002 — Sign up page (GET)
**User story:** As a new user, I can open a sign-up form to create an account.

**Route / template:**
- `GET /signup`
- `templates/signup.html`

**Form fields:**
- Email
- Password
- Submit button: “Create account”
- Secondary link: “Already have an account? Log in” → `/login`

**Validation (MVP):**
- Email: required, basic email format check
- Password: required (optional min length: 8)

**Acceptance criteria:**
- Given I am not authenticated, when I open `/signup`, then I see email/password fields and “Create account”.
- Given I click the login link, then I navigate to `/login`.
- Given I am authenticated, when I open `/signup`, then I am redirected to `/recipes`.

---

### AUTH-003 — Sign up submission (POST)
**User story:** As a new user, I can submit the sign-up form to create an account and start using Cookcards.

**Route:**
- `POST /signup`

**Backend behavior:**
- Normalize email: trim + lower-case.
- Create user record:
    - email (unique)
    - password hash (never store plaintext)
    - createdAt
- Create authenticated session on success.
- Redirect to `/recipes`.

**Error states:**
- Email already exists → show: “An account with this email already exists.”
- Validation errors → show field-level or form-level errors.

**Acceptance criteria:**
- Given a new email and valid password, when I submit sign up, then:
    - user is created
    - session is established
    - I am redirected to `/recipes`
- Given an email that already exists, when I submit sign up, then:
    - no new user is created
    - I remain on `/signup`
    - I see “An account with this email already exists.”
- Given invalid inputs, when I submit, then:
    - I remain on `/signup`
    - I see validation errors
    - no user is created

---

### AUTH-004 — Log in page (GET)
**User story:** As a returning user, I can open a login form.

**Route / template:**
- `GET /login`
- `templates/login.html`

**Form fields:**
- Email
- Password
- Submit button: “Log in”
- Secondary link: “New here? Create an account” → `/signup`

**Acceptance criteria:**
- Given I am not authenticated, when I open `/login`, then I see email/password fields and a “Log in” button.
- Given I am authenticated, when I open `/login`, then I am redirected to `/recipes`.

---

### AUTH-005 — Log in submission with unlimited retries (POST)
**User story:** As a returning user, I can log in with my email and password; if credentials are incorrect, I see an error and can retry indefinitely.

**Route:**
- `POST /login`

**Backend behavior:**
- If credentials are correct:
    - create session
    - redirect to `/recipes`
- If credentials are incorrect:
    - render `/login` with error
    - allow unlimited retries (no rate limiting in MVP)

**UI copy (security-friendly):**
- Error message: “Invalid email or password.”

**Acceptance criteria:**
- Given valid credentials, when I submit login, then I am redirected to `/recipes`.
- Given invalid credentials, when I submit login, then:
    - I remain on `/login`
    - I see “Invalid email or password.”
- Given I submit invalid credentials repeatedly, then the system continues to allow retries (no lockout in MVP).

---

### AUTH-006 — Log out (MVP)
**User story:** As an authenticated user, I can log out from the top nav.

**Route:**
- Preferred: `POST /logout`
- Redirect: `/`

**Acceptance criteria:**
- Given I am authenticated, when I trigger logout, then:
    - my session is cleared
    - I am redirected to `/`
- Given I log out and then open `/recipes`, then I am redirected to `/login`.

---

### AUTH-007 — My recipes page (empty state) (GET)
**User story:** As a newly signed-up user, I land somewhere meaningful after authentication even if I have no recipes yet.

**Route / template:**
- `GET /recipes`
- `templates/recipes.html`

**UI requirements:**
- Top nav visible (NAV-001)
- Page title: “My recipes”
- If user has zero recipes:
    - empty state text: “No recipes yet.”
    - primary CTA: “Add a recipe” → `/recipes/new` (placeholder for next flow)

**Acceptance criteria:**
- Given I am authenticated, when I open `/recipes`, then:
    - I see the top nav
    - I see “My recipes”
    - If I have no recipes, I see “No recipes yet.” and “Add a recipe”
- Given I am not authenticated, when I open `/recipes`, then I am redirected to `/login`.

---

## 4) Navigation map (links and redirects)

### Public area links
- Landing:
    - “Log in” → `/login`
    - “Sign up” → `/signup`
- Login:
    - “New here? Create an account” → `/signup`
- Signup:
    - “Already have an account? Log in” → `/login`

### Redirect behavior
- Authenticated user visiting `/`, `/login`, `/signup` → `/recipes`
- Unauthenticated user visiting `/recipes` or any future `/recipes/**` → `/login`
- Successful login/signup → `/recipes`
- Logout → `/`

---

## 5) Copy deck (exact strings)

### Landing
- “Cookcards”
- Buttons: “Log in”, “Sign up”

### Sign up
- Title: “Create your account”
- Button: “Create account”
- Link: “Already have an account? Log in”
- Error: “An account with this email already exists.”

### Log in
- Title: “Log in”
- Button: “Log in”
- Link: “New here? Create an account”
- Error: “Invalid email or password.”

### Recipes empty state
- “My recipes”
- “No recipes yet.”
- “Add a recipe”

### Top nav
- “Cookcards” (link)
- “Log out”

---

## 6) Implementation notes for Codex (non-functional constraints)
- Use server-side sessions (Spring Security default is fine).
- Store password hashes using BCrypt (or Spring Security password encoder).
- Email uniqueness should be enforced at DB level and in service layer.
- Keep templates minimal and consistent with “clean white” design.
- Feel free to rewrite the existing code as needed.
- Existing templates have style - try to keep consistent.