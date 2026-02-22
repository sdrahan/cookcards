# Post-login Product Flow — Snapshot Spec

Last verified against code: 2026-02-22.

## Purpose
Define current authenticated user flow after login, including recipe library, recipe creation from free text, recipe preview/save, recipe detail placeholders, deletion, and profile settings.

## In scope
- Authenticated navigation and top nav
- Recipe library (`/recipes`)
- Add recipe and parse flow (`/recipes/new` -> `/recipes/preview` -> save)
- Recipe detail/edit/print placeholder pages
- Recipe deletion
- Profile settings page

## Out of scope
- Rich recipe detail rendering from stored JSON
- Real edit flow
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

### Recipe detail placeholder (`GET /recipes/{recipeId}`)
- Template: `recipe.html`
- Shows recipe title and links to `Edit` / `Print`.
- Body content is placeholder text.
- Ownership enforced: non-owned or missing recipe -> HTTP 404.

### Recipe edit placeholder (`GET /recipes/{recipeId}/edit`)
- Template: `recipe.html` with `mode=edit`
- Same ownership and 404 behavior as detail.

### Recipe print placeholder (`GET /recipes/{recipeId}/print`)
- Template: `recipe.html` with `mode=print`
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
- Redirects to `/recipes/preview`.

### Recipe preview (`GET /recipes/preview`)
- Template: `recipe-preview.html`
- Requires `parsedRecipe` in session.
- If missing parsed recipe in session: redirect `/recipes/new`.

### Save parsed recipe (`POST /recipes/preview/save`)
- Requires both `parsedRecipe` and `parsedRecipeJson` in session.
- If missing, redirect `/recipes/new`.
- Creates recipe for current user with title from parsed recipe name.
- Clears session keys (`parsedRecipe`, `parsedRecipeJson`).
- Redirects to `/recipes/{id}` of saved recipe.

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

## Known current limitations
- `recipe.html` is still a placeholder and does not render structured recipe content.
- `/recipes/{id}/edit` and `/recipes/{id}/print` are route placeholders reusing the same template.

## Source files
- `src/main/java/app/cookcards/webapp/controller/RecipesController.java`
- `src/main/java/app/cookcards/webapp/controller/RecipeController.java`
- `src/main/java/app/cookcards/webapp/controller/ProfileController.java`
- `src/main/java/app/cookcards/webapp/recipe/RecipeService.java`
- `src/main/java/app/cookcards/webapp/config/SecurityConfig.java`
- `src/main/resources/templates/fragments/nav.html`
- `src/main/resources/templates/recipes.html`
- `src/main/resources/templates/recipe.html`
- `src/main/resources/templates/add-recipe.html`
- `src/main/resources/templates/recipe-preview.html`
- `src/main/resources/templates/profile.html`
- `src/main/resources/db/migration/V01__initial_schema.sql`
