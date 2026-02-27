# Post-login Product Flow — Snapshot Spec

Last verified against code: 2026-02-22.

## Purpose
Define current authenticated user flow after login, including recipe library, recipe creation from free text, recipe review/edit/save, recipe detail rendering, deletion, and profile settings.

## In scope
- Authenticated navigation and top nav
- Recipe library (`/recipes`)
- Add recipe and parse flow (`/recipes/new` -> `/recipes/edit` -> save)
- Recipe detail/edit/print pages
- Recipe deletion
- Profile settings page

## Out of scope
- PDF generation
- Recipe sharing/collaboration

## Shared authenticated layout
- Fragment: `fragments/nav.html`
- Visible on authenticated templates:
  - App link `Cookcards` -> `/recipes`
  - `Profile` -> `/profile`
  - `Log out` (POST `/logout`)

## Routes and behavior

### Recipes dashboard (`GET /recipes`)
- Template: `recipes.html`
- Data:
  - Recipes for current user only
  - Sorted by `updated_at DESC`
- UI states:
  - List state (`hasRecipes = true`): each row has title link, `Open`, `Delete`
  - Empty state (`hasRecipes = false`): `No recipes yet.` and `Add a recipe`
- Optional error banner on `?deleteError`:
  - `Could not delete recipe. Please try again.`

### Recipe detail (`GET /recipes/{recipeId}`)
- Template: `recipe.html`
- Renders structured recipe content from persisted `recipe_json`.
- Shows recipe title, optional description/details, ingredients, and instruction sections/steps.
- Shows `Edit` / `Print` links in normal mode.
- Ownership enforced: non-owned or missing recipe -> HTTP 404.

### Recipe edit (`GET /recipes/{recipeId}/edit`, `POST /recipes/{recipeId}/edit`)
- Template: `recipe-edit.html`
- GET:
  - Loads persisted `recipe_json` and pre-fills the editor.
  - Ownership enforced: non-owned or missing recipe -> HTTP 404.
- POST:
  - Applies server-side sanitization and validation:
    - `name` required
    - At least one non-empty ingredient required
    - At least one non-empty instruction step required
  - On validation errors: re-renders `recipe-edit.html` with inline errors.
  - On success: updates recipe JSON and title, then redirects to `/recipes/{recipeId}`.

### Recipe print (`GET /recipes/{recipeId}/print`)
- Template: `recipe.html` with `mode=print`
- Renders structured recipe content from persisted `recipe_json`.
- Same ownership and 404 behavior as detail.

### Delete recipe (`POST /recipes/{recipeId}/delete`)
- Requires ownership.
- On success: redirect `/recipes`.
- If recipe not found for user: HTTP 404.
- On unexpected failure: redirect `/recipes?deleteError`.
- UI uses browser confirmation:
  - `Delete recipe? This cannot be undone.`

### Add recipe form (`GET /recipes/new`)
- Template: `add-recipe.html`
- Input: `freeText` textarea.

### Parse recipe (`POST /recipes/new`)
- Validation and errors:
  - Empty or <20 chars -> `Please paste at least 20 characters of recipe text.`
  - Fails food-recipe classifier -> `Input does not look like a valid food recipe.`
  - OpenAI/parse failure -> `Could not parse recipe right now. Please try again.`
- Uses current user settings (`unitsMode`, `targetLanguage`) during parsing.
- On success, stores parsed data in session:
  - `parsedRecipe`
  - `parsedRecipeJson`
- Redirects to `/recipes/edit`.

### Parsed recipe review & fix (`GET /recipes/edit`, `POST /recipes/edit`, `POST /recipes/edit/cancel`)
- Template: `recipe-edit.html`
- GET:
  - Requires `parsedRecipe` in session.
  - If session recipe is missing: redirects to `/recipes/new`.
- POST `/recipes/edit`:
  - Applies same server-side sanitization and validation rules as persisted edit.
  - On validation errors: re-renders `recipe-edit.html` with inline errors.
  - On success: persists a new recipe and redirects to `/recipes/{id}`.
  - Clears session keys (`parsedRecipe`, `parsedRecipeJson`) after successful save.
- POST `/recipes/edit/cancel`:
  - Clears session keys (`parsedRecipe`, `parsedRecipeJson`).
  - Redirects to `/recipes`.

### Preview route compatibility redirects
- `GET /recipes/preview` redirects to `/recipes/edit`.
- `POST /recipes/preview/save` redirects to `/recipes/edit`.

### Profile (`GET /profile`, `POST /profile`)
- Template: `profile.html`
- Shows current email and settings form.
- Editable fields:
  - `unitsMode`: `METRIC`, `IMPERIAL`, `ORIGINAL`
  - `targetLanguage`: `ENGLISH`, `GERMAN`, `RUSSIAN`, `ORIGINAL`
- On save success: redirect `/profile?saved` and show `Settings saved.`

## Data constraints relevant to this flow
- Recipe record fields: owner (`user_id`), `title`, `recipe_json`, timestamps.
- Recipe access is always scoped to authenticated user.
- `title` is synchronized with the edited recipe name on updates.

## Known current limitations
- See `docs/specs/recipe-edit.md` for explicit edit-flow non-goals (no images, no bulk paste, no drag-and-drop reorder).

## Source files
- `src/main/java/app/cookcards/webapp/controller/RecipesController.java`
- `src/main/java/app/cookcards/webapp/controller/RecipeController.java`
- `src/main/java/app/cookcards/webapp/controller/ProfileController.java`
- `src/main/java/app/cookcards/webapp/controller/RecipeEditForm.java`
- `src/main/java/app/cookcards/webapp/controller/RecipeEditFormSanitizer.java`
- `src/main/java/app/cookcards/webapp/recipe/RecipeService.java`
- `src/main/java/app/cookcards/webapp/config/SecurityConfig.java`
- `src/main/resources/templates/fragments/nav.html`
- `src/main/resources/templates/recipes.html`
- `src/main/resources/templates/recipe.html`
- `src/main/resources/templates/recipe-edit.html`
- `src/main/resources/templates/add-recipe.html`
- `src/main/resources/static/js/recipe-edit.js`
- `src/main/resources/templates/profile.html`
- `src/main/resources/db/migration/V01__initial_schema.sql`
