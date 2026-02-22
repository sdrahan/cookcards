## Naming Conventions
- Java packages: `app.cookcards.webapp.<layer>`
- Classes: `PascalCase`
- Methods: `camelCase`, verb-first
- DTOs: `*DTO`
- Form objects: `*Form`
- Services: `*Service`
- Controllers: `*Controller`

No abbreviations unless industry-standard.

## Code Organization
- Controllers: HTTP + validation only
- Services: business logic only
- Repositories: persistence only
- DTOs: immutable (`record`)
- No logic in templates beyond iteration and conditionals

## Error Handling
- Fail fast
- Validate inputs at boundaries
- No silent fallbacks
- User-facing errors must be human-readable
- Internal errors must include context

## Logging Rules
- Use SLF4J
- Log at INFO for lifecycle events
- Log at WARN for recoverable issues
- Log at ERROR only when action is required
- Never log:
    - passwords
    - full recipe text
    - OpenAI prompts or responses verbatim
