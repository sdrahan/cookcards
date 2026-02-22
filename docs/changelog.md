# Changelog

Condensed log of spec-impacting changes and important decisions.

## Entry rules
- Add an entry when behavior in `docs/specs/` changes.
- Add an entry when a decision materially affects implementation direction.
- Keep entries short (3-6 bullets max).
- Link to the task and spec files.

## Template
### YYYY-MM-DD — <short title>
- Type: `spec-change` | `decision`
- Task: `docs/tasks/open/...` or `docs/tasks/done/...` (if applicable)
- Specs: `docs/specs/...`
- Summary:
  - <what changed or what was decided>
  - <why>
- Impact:
  - <implementation/testing/docs impact>

---

### 2026-02-22 — Documentation structure and workflow consolidation
- Type: `decision`
- Task: N/A
- Specs: `docs/specs/auth.md`, `docs/specs/postlogin_flow.md`
- Summary:
  - Standardized project docs around `docs/specs/` (contracts), `docs/tasks/open/` (active work), and `docs/tasks/done/` (history).
  - Added `AGENTS.md` as the top-level entry point and reduced duplicated rules across meta-docs.
  - Introduced `docs/changelog.md` as the condensed log for spec-impacting changes and important decisions.
- Impact:
  - Contributors and agents now follow one read path and one precedence model.
  - Future behavior changes should update both the relevant spec and this changelog.

### 2026-02-22 — Specs converted from ticket drafts to implementation snapshots
- Type: `spec-change`
- Task: N/A
- Specs: `docs/specs/auth.md`, `docs/specs/postlogin_flow.md`
- Summary:
  - Rewrote both spec files to reflect current implemented behavior (routes, redirects, validations, ownership rules, and placeholder areas).
  - Removed forward-looking story/ticket framing from spec files.
- Impact:
  - Specs now serve as source-of-truth snapshots for current code behavior.
  - Future tasks can reference these specs directly and document only deltas.
