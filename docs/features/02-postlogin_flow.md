# Cookcards.app — Product/UX Stories
## Flow 2: Post-login dashboard (recipes library)

**Scope intent:** After successful authentication, the user lands on their recipes library.
- If they have ≥ 1 recipe: show dashboard list.
- If they have 0 recipes: show empty state (CTA to add recipe).
- Provide: open recipe (view/edit/print), delete recipe, link to profile.

**Out of scope (explicit):**
- “Archive” / trash bin / undo delete
- Recipe thumbnails / images

---

## 1) Screen: Dashboard (recipes library)

### IA / routes
- Dashboard (recipes list): `GET /recipes`
- Delete recipe: `POST /recipes/{recipeId}/delete` (or `DELETE /recipes/{recipeId}` if you prefer)
- Recipe view (placeholder for next flows): `GET /recipes/{recipeId}`
- Recipe edit (placeholder): `GET /recipes/{recipeId}/edit`
- Recipe print (placeholder): `GET /recipes/{recipeId}/print` (or `/recipes/{recipeId}/pdf`)
- Profile: `GET /profile`

### Shared layout updates (authenticated pages)
- Top nav (from Flow 1) must include:
    - Left: “Cookcards” → `/recipes`
    - Right: “Profile” → `/profile`
    - Right: “Log out” → logout endpoint

---

## 2) Dashboard UI spec (MVP)

### Content
- Page title: “My recipes”
- List of recipes (no images)
- Each recipe row shows:
    - Recipe name/title (required)
    - Actions:
        - “Open” (or row is clickable)
        - “Delete”
- A primary CTA button (optional but high ROI): “Add a recipe” → `/recipes/new` (placeholder)

### Sorting (assumption for MVP)
- Show newest first (by `updatedAt` if available, else `createdAt`).

---

## 3) Stories (Codex-ready)

### DASH-001 — Dashboard shows list when user has recipes
**User story:** As a logged-in user with saved recipes, I see a dashboard list of my recipes.

**Route / template:**
- `GET /recipes`
- `templates/recipes.html`

**Acceptance criteria:**
- Given I am authenticated and have at least 1 recipe, when I open `/recipes`, then I see:
    - top nav (Cookcards, Profile, Log out)
    - page title “My recipes”
    - a list of my recipes
- Given I am not authenticated, when I open `/recipes`, then I am redirected to `/login`.

---

### DASH-002 — Dashboard empty state when user has no recipes
**User story:** As a logged-in user with no recipes, I see an empty state that prompts me to add one.

**Route / template:**
- `GET /recipes`
- `templates/recipes.html` (same template, conditional rendering)

**UI copy:**
- Empty text: “No recipes yet.”
- CTA: “Add a recipe” → `/recipes/new` (placeholder)

**Acceptance criteria:**
- Given I am authenticated and have 0 recipes, when I open `/recipes`, then I see:
    - top nav
    - “My recipes”
    - “No recipes yet.”
    - “Add a recipe” button linking to `/recipes/new`

---

### DASH-003 — Open recipe from dashboard
**User story:** As a user, I can open a recipe from the dashboard to view/edit/print it.

**Interaction:**
- Option A: recipe title is a link to `GET /recipes/{recipeId}`
- Option B: explicit “Open” button linking to `GET /recipes/{recipeId}`

**Acceptance criteria:**
- Given a recipe appears in my list, when I click its title (or “Open”), then I navigate to `/recipes/{recipeId}`.
- Given the recipe does not exist or I don’t own it, when I try to open it, then I see a 404 or an access denied page (choose one behavior consistently).

**Notes:**
- The actual view/edit/print screens can be implemented in later flows; this story only requires correct navigation wiring.

---

### DASH-004 — Delete recipe from dashboard (with confirmation)
**User story:** As a user, I can delete a recipe from my dashboard.

**Interaction:**
- “Delete” action per recipe row.
- Confirmation required (MVP options):
    - Option A: browser confirm dialog (“Delete recipe? This cannot be undone.”)
    - Option B: dedicated confirm page `GET /recipes/{id}/delete-confirm` (more work)

**Route:**
- `POST /recipes/{recipeId}/delete` (recommended for simplicity with HTML forms)

**UI copy:**
- Confirm: “Delete recipe? This cannot be undone.”
- Error (if delete fails): “Could not delete recipe. Please try again.”

**Acceptance criteria:**
- Given a recipe is listed, when I click “Delete” and confirm, then:
    - the recipe is removed from my library
    - I return to `/recipes` and the list no longer shows it
- Given I cancel the confirmation, when I cancel, then the recipe is not deleted.
- Given I attempt to delete a recipe I do not own, when I submit delete, then deletion does not happen and I receive a 404 or access denied (consistent with open behavior).

---

### DASH-005 — Deleting the last recipe transitions to empty state
**User story:** As a user, if I delete my last recipe, I immediately see the empty state.

**Acceptance criteria:**
- Given I have exactly 1 recipe, when I delete it and confirm, then I am on `/recipes` and see:
    - “No recipes yet.”
    - “Add a recipe” CTA
- Given I have >1 recipe, when I delete one, then I remain on `/recipes` and still see the list with remaining recipes.

---

### DASH-006 — Profile link from dashboard
**User story:** As a user, I can navigate to my profile from the dashboard.

**Route / template:**
- `GET /profile`
- `templates/profile.html` (minimal placeholder is fine in MVP)

**UI requirements (MVP placeholder):**
- Top nav visible
- Page title: “Profile”
- Show at least the user’s email (read-only)
- (Optional) “Delete account” is out of scope unless you explicitly want it.

**Acceptance criteria:**
- Given I am authenticated, when I click “Profile” in the top nav, then I navigate to `/profile`.
- Given I am not authenticated, when I open `/profile`, then I am redirected to `/login`.

---

## 4) Copy deck (exact strings)

### Dashboard
- Title: “My recipes”
- Empty state: “No recipes yet.”
- CTA: “Add a recipe”
- Row actions: “Open”, “Delete”
- Delete confirm: “Delete recipe? This cannot be undone.”
- Delete error: “Could not delete recipe. Please try again.”

### Top nav (authenticated)
- “Cookcards” (link)
- “Profile” (link)
- “Log out”

---

## 5) Assumptions made (can be changed later without breaking flow)
- Dashboard lives at `/recipes` and is the post-login redirect.
- Recipe view/edit/print routes are placeholders for future flows:
    - `/recipes/{id}`, `/recipes/{id}/edit`, `/recipes/{id}/print`
- Delete is permanent (no archive / undo).
