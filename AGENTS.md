# AGENTS.md

Entry point for humans and coding agents working in this repository.

## Read order
1. `AGENTS.md` (this file)
2. `docs/README.md` (project documentation map)
3. Relevant file in `docs/specs/`
4. If implementing work: task file in `docs/tasks/open/`
5. `.ai/` rules for implementation constraints and quality checks

## Documentation layout
- `docs/specs/`: current expected behavior (source of truth for product behavior)
- `docs/tasks/open/`: actionable tasks ready to implement
- `docs/tasks/done/`: completed tasks (history only)
- `docs/changelog.md`: condensed log of spec-impacting changes and important decisions

## Conflict rules
1. For implementation requests from `docs/tasks/open/`, follow the task and update referenced spec(s).
2. Outside a specific task implementation, specs are authoritative for behavior.
3. `docs/tasks/done/` never overrides current specs.

## Working rules
- Spec-first: behavior changes must be reflected in `docs/specs/`.
- Update `docs/changelog.md` when a change affects spec behavior or records an important decision.
- Keep docs DRY: put each rule in one place; link instead of restating.
- Keep tasks narrow: no unrelated refactors.
- When a task is completed and merged, move it from `docs/tasks/open/` to `docs/tasks/done/`.
