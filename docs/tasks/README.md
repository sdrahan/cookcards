# Tasks

`docs/tasks/open/` contains actionable tasks.
`docs/tasks/done/` contains completed tasks for history.

## Naming
- Use `NNN_<slug>.md` (example: `003_signup_email_only.md`).

## Lifecycle
1. Create in `docs/tasks/open/` using `docs/tasks/TEMPLATE.md`.
2. Implement code and update referenced spec(s).
3. Add/update `docs/changelog.md` if behavior/specs changed or a key decision was made.
4. Run acceptance checks from the task.
5. After merge, move the file to `docs/tasks/done/`.

## Rules
- Task files must reference at least one spec in `docs/specs/`.
- Keep tasks implementation-ready and scoped.
- Do not treat files in `docs/tasks/done/` as active instructions.
- See `AGENTS.md` for conflict resolution and source-of-truth rules.
