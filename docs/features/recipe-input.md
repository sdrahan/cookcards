# docs/features/recipe-input.md

## Feature: Recipe Input

## Purpose
Allow the user to provide an unstructured recipe as input so it can be parsed into a structured recipe model.

This is the **entry point** of the application.

---

## User Story
As a user,  
I want to paste or upload a recipe in any language,  
so that the system can convert it into a structured, printable recipe.

---

## Scope

### In Scope (MVP)
- Free-text recipe input via a textarea
- Submission via HTML form
- Server-side validation
- Parsing via OpenAI
- Session-based storage of the parsed recipe
- Redirect to recipe preview on success

### Out of Scope
- Image upload (may be added later)
- Multiple input methods simultaneously
- Draft saving
- Autosave
- Client-side parsing or validation

---

## UI Specification

### Page
- Route: `/`
- Template: `index.html`

### Elements
- Application title
- Short explanatory text
- `<textarea>` for recipe input
- Submit button

### Behavior
- Form uses `POST /parse`
- Uses standard HTML form submission
- No JavaScript required

---

## Validation Rules
- `freeText` must not be empty
- Whitespace-only input is invalid
- Validation errors:
    - Redisplay the same page
    - Show a human-readable error message
    - Preserve user input

---

## Backend Behavior

### On GET `/`
- Render empty form
- Initialize `ParseRequest` model attribute

### On POST `/parse`
1. Validate input
2. Call recipe parsing service
3. Store resulting `RecipeDTO` in HTTP session
4. Redirect to `/review?template=classic`

### Error Handling
- Parsing failure:
    - Show user-facing error
    - Do not store partial data
- OpenAI errors:
    - Logged with context (no raw content)
    - Exposed as generic failure message

---

## Domain Rules
- Input language must be preserved
- No translation is performed
- No persistence beyond session

---

## Non-Goals
- Rich text editing
- Live preview
- File uploads (for now)

---

## Definition of Done
- User can paste a recipe and submit
- Empty input is rejected
- Valid input leads to preview page
- Recipe is stored in session
- No data is persisted