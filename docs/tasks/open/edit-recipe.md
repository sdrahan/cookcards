# Task NNN — Recipe Edit Page (Review & Fix)

## Goal
Implement an edit page for an existing recipe that is optimized for “review & fix” after import. The page is pre-filled from `RecipeDTO` (produced by `RecipeParsingService` after user pastes unstructured recipe text) or opened from the existing “view recipe” page via an Edit action. Users can adjust values and structure (add/remove/reorder ingredients, instruction sections, and steps) and then save changes back to persistence.

## Non-goals
- Recipe creation from scratch (no empty-state wizard, no “start blank” flow)
- Image support (recipe images and per-step images are out of scope)
- Bulk paste / bulk import UX for ingredients or steps
- Drag-and-drop reordering (use up/down controls)

## Spec delta
- In `docs/specs/recipe-edit.md` (create if doesn't exist yet), add/update sections to cover:
    - Entry points:
        - Redirect to Recipe Edit page after `RecipeParsingService` successfully parses input and produces a `RecipeDTO`
        - “Edit” action from “view recipe” page navigates to the same Recipe Edit page for that recipe id (add "edit" button if not present)
    - Editable fields and UI structure:
        - Header: name (required), description (optional)
        - Details accordion (optional): `recipeYield`, `prepTime`, `cookTime`, `totalTime`, `nutrition.calories`
        - Ingredients list editor: edit/add/remove/reorder lines
        - Instructions editor: edit/add/remove/reorder sections and steps (section name optional)
    - Save/Cancel behavior:
        - Save persists (modifies persisted recipe if came from "edit recipe" or creates if came from "create recipe" flow) and switches to full recipe view
        - If last page was recipe view, then "cancel" reverts all unsaved edits to last saved state and returns to full recipe view
        - If last page was recipe add, then "cancel" is called "back to all recipes", and nothing is persisted
        - Warn on navigation away if there are unsaved changes

## Changelog delta
- Add one entry to `docs/changelog.md` noting:
    - Added “Recipe Edit (Review & Fix)” page as the canonical post-parse destination and as the target of “Edit” from recipe view
    - Images/bulk paste/drag-drop explicitly out of scope for MVP

## Scope
Allowed:
- Spring MVC controller(s) and routes for recipe edit (GET + POST)
- Thymeleaf template for recipe edit page (supports Edit + Preview toggle)
- Minimal vanilla JS for:
    - show/hide “Details” accordion
    - Preview/Edit toggle
    - add/remove/reorder list items (ingredients, sections, steps)
    - unsaved-changes warning
- DTO/form binding for `RecipeDTO` (excluding `images` and `InstructionStepDTO.image`)
- Persistence update path for recipe edits (service + repository as applicable)
- Navigation updates on recipe view page to include “Edit” action (if not present)

Not allowed:
- Introducing a frontend framework (React/Vue/etc.) or jQuery
- Adding image upload/storage/rendering (any `images` fields)
- Drag-and-drop reorder
- Bulk paste features

## Acceptance criteria
Behavior:
- Given a user submits unstructured recipe text and `RecipeParsingService` returns a valid `RecipeDTO`, when parsing succeeds, then the app navigates to the Recipe Edit page with all parsed fields pre-filled for editing.
- Given an existing saved recipe, when the user clicks “Edit” from the recipe view page, then the same Recipe Edit page opens pre-filled from persistence.
- Given the Recipe Edit page is open, when the user edits the recipe name/description, then those changes are reflected in the editable inputs and included in the payload on Save.
- Given the Recipe Edit page is open, when the user adds/removes/reorders ingredient rows using explicit controls, then the rendered list updates immediately and Save persists the resulting order/content.
- Given the Recipe Edit page is open, when the user adds/removes/reorders instruction sections and steps using explicit controls, then the rendered structure updates immediately and Save persists the resulting structure and order.
- Given the user clicks Cancel with unsaved edits, then the page reverts to the last saved recipe state (no persistence changes).
- Given there are unsaved edits, when the user attempts to navigate away (back, close tab, link away), then the browser shows an unsaved-changes warning.
- Given required fields are invalid (blank name OR no ingredients OR no steps), when the user clicks Save, then the save is rejected and inline validation errors are shown near the relevant blocks.

## Notes
- `RecipeDTO` is the canonical transport/storage DTO for the form.
- Images are out of scope: ignore/hide `RecipeDTO.images` and `InstructionStepDTO.image` in UI and form handling for this task.
- Reordering approach: up/down buttons (swap DOM nodes) + renumber form field indices to match server binding expectations.
- Instructions model:
    - Section name is optional; if only one section exists and name is blank, preview may omit the section title, but edit mode must still allow setting it.
    - Enforce “at least 1 step total” (not just “at least 1 empty section”).
- Implementation detail left to dev: either server-side indexed binding (recommended) or a custom form model; keep it boring and debuggable.
- Decide what to do with `recipes/preview` path - whether to keep it and add separate `recipes/edit` (with common controller?) or merge into single `recipes/edit`. Make decision yourself.
- Make it visually look elegant, simple, clean, and beautiful.