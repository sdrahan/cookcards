# Recipe Edit (Review & Fix) — Snapshot Spec

Last verified against code: 2026-02-22.

## Purpose
Define the canonical recipe editing flow used both after parse and from existing recipe detail, optimized for reviewing and fixing structured recipe data.

## Entry points
- Parsed flow:
  - `POST /recipes/new` parses unstructured text and redirects to `GET /recipes/edit`.
  - `GET /recipes/edit` requires `parsedRecipe` in session and pre-fills the edit form.
- Existing recipe flow:
  - `GET /recipes/{recipeId}` includes an `Edit` action.
  - `GET /recipes/{recipeId}/edit` opens the same `recipe-edit.html` page pre-filled from persisted `recipe_json`.

## Editable fields and structure
- Header:
  - `name` (required)
  - `description` (optional)
- Details accordion (optional):
  - `recipeYield`
  - `prepTime`
  - `cookTime`
  - `totalTime`
  - `nutrition.calories`
- Ingredients editor:
  - Add / remove / reorder ingredient rows with explicit controls.
- Instructions editor:
  - Add / remove / reorder sections.
  - Add / remove / reorder steps inside sections.
  - Section name is optional.
- Page mode:
  - Edit mode and Preview mode are toggled client-side in the same template.

## Save and cancel behavior
- Save from parsed flow (`POST /recipes/edit`):
  - Creates a new persisted recipe.
  - Redirects to full recipe view (`/recipes/{id}`).
  - Clears parsed session state.
- Save from existing flow (`POST /recipes/{recipeId}/edit`):
  - Updates persisted recipe JSON and title.
  - Redirects to full recipe view (`/recipes/{recipeId}`).
- Cancel from parsed flow (`POST /recipes/edit/cancel`):
  - Label: `Back to all recipes`.
  - Clears parsed session state and returns to `/recipes`.
- Cancel from existing flow:
  - Label: `Cancel`.
  - Navigates back to `/recipes/{recipeId}` without persistence changes.

## Validation and normalization
- Server-side normalization trims all scalar fields.
- Blank ingredient rows are removed before validation.
- Blank step rows are removed before validation.
- Instruction sections with zero remaining steps are removed before validation.
- Save is rejected with inline errors when:
  - Name is blank.
  - No non-empty ingredients remain.
  - No non-empty instruction steps remain across all sections.
- Optional scalar fields (`description`, details, calories) are persisted as `null` when blank.

## Navigation-away warning
- Client-side unsaved-changes warning is shown on browser navigation (`beforeunload`) when the form has unsaved edits.
- Save and intentional cancel actions suppress the warning.

## Compatibility routes
- `GET /recipes/preview` redirects to `GET /recipes/edit`.
- `POST /recipes/preview/save` redirects to `GET /recipes/edit`.

## Explicit non-goals
- Image support in UI/form handling (`RecipeDTO.images`, `InstructionStepDTO.image`).
- Bulk paste import UX for ingredients or steps.
- Drag-and-drop reorder interactions.
- Recipe creation from an empty blank-state wizard.

## Source files
- `src/main/java/app/cookcards/webapp/controller/RecipeController.java`
- `src/main/java/app/cookcards/webapp/controller/RecipesController.java`
- `src/main/java/app/cookcards/webapp/controller/RecipeEditForm.java`
- `src/main/java/app/cookcards/webapp/controller/RecipeEditFormSanitizer.java`
- `src/main/java/app/cookcards/webapp/recipe/RecipeService.java`
- `src/main/resources/templates/recipe-edit.html`
- `src/main/resources/static/js/recipe-edit.js`
