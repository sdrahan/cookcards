## Overview
CookCards is a monolithic Spring Boot application with server-rendered HTML.

## Rationale
- Minimal operational complexity
- Easy debugging
- Clear control flow
- Fast iteration

## Layering
- Controllers: HTTP + validation
- Services: recipe parsing and transformation
- Repositories: persistence (future)

## Error Handling
- Validate early
- Fail fast
- No silent recovery
- Explicit user feedback

## Deployment
- Single container
- Docker Compose
- One database
- Stateless except HTTP session