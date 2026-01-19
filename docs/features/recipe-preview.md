## Feature: Recipe Preview

## Purpose
Allow the user to review, edit, and preview a parsed recipe using different visual templates before printing.

This is the **core interaction** of the application.

---

## User Story
As a user,  
I want to review my parsed recipe and select the visual template,  
so that it looks correct before I print it.

---

## Scope

### In Scope (MVP)
- Display parsed recipe
- Non-editable fields:
    - Title
    - Ingredients
    - Steps
    - Notes
- Template selection
- Live server-rendered preview
- Browser-based printing

### Out of Scope
- PDF generation
- Persistent saving
- Rich editors
- Drag-and-drop
- Sharing

---

## UI Specification

### Page
- Route: `/review`
- Template: `review.html`

### Layout
- Single pane. On top of page - full-width template selector panel
- Below: recipe preview

### Elements
- Template selector (`classic`, `minimal`, `card`)
- Preview area
- Print button
- Back link to input page

---

## Template Behavior
- Template selection via query parameter: `?template=...`
- Changing template triggers full page reload
- No JavaScript state management

---

## Backend Behavior

### On GET `/review`
1. Load `RecipeDTO` from session
2. If missing:
    - Redirect to `/`
3. Populate `ReviewForm`
4. Render preview using selected template

---

## Preview Rules
- Preview uses session-stored `RecipeDTO`
- Preview must match printed output
- No preview-only transformations

---

## Printing
- Printing is done via browser `window.print()`
- No server-side PDF generation
- User is responsible for print settings

---

## Error Handling
- Missing session recipe → redirect to input page
- Invalid edits → redisplay form with errors
- Template not found → fail loudly (developer error)

---

## Domain Rules
- Original recipe language must be preserved
- No translation
- No automatic corrections beyond parsing

---

## Non-Goals
- Undo/redo
- History
- Multiple recipes per session

---

## Definition of Done
- Parsed recipe is visible
- Template switching works
- Preview updates correctly
- Printing works
- No persistence beyond session