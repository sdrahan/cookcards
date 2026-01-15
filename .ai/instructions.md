## Purpose
This directory defines how AI assistants must operate in this repository.  
These rules are **binding**. Violations are bugs.

## How to Work in This Repository
- This repository is **spec-first**.
- You implement code **only** after a spec exists in `docs/`.
- If behavior is unclear, prefer **explicitness over cleverness**.
- Optimize for correctness, simplicity, and debuggability.
- Assume this is a long-lived codebase, not a demo.

## Mandatory Rules
- Never change behavior without updating specs.
- Never introduce new dependencies without architectural justification.
- Never persist data unless explicitly specified.
- Never add abstraction layers “just in case”.
- Never rely on undocumented framework magic.

## Precedence Rules (Highest → Lowest)
1. `.ai/*` files (this directory)
2. `docs/*` specifications
3. Existing code
4. Framework defaults
5. Personal preference

If two rules conflict, the **higher-precedence source wins**.

## When in Doubt
- Prefer fewer features over more.
- Prefer clarity over flexibility.
- Prefer deletion over addition.
- Prefer boring solutions.