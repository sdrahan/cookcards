## Authentication & Authorization
- Session-based
- No roles (MVP)
- No public access to session data

## Session Strategy
- Server-side HTTP session
- Short-lived
- No persistence

## Password Handling
- Standard Spring Security hashing
- Never logged
- Never reversible

## CSRF / XSS
- CSRF protection enabled for POST
- Output escaped by Thymeleaf
- No raw HTML injection

## Logging & PII
- Never log recipe content verbatim
- Never log images
- Never log credentials
- Logs are diagnostic only

Privacy by default.