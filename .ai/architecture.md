## High-Level Constraints
- Single monolithic Spring Boot application
- Server-rendered HTML using Thymeleaf
- Session-based state (no SPA, no REST-first design)
- Docker Compose for all local and prod environments

## Layering Rules
- Controller → Service → (Repository)
- No controller-to-repository access
- No service-to-web dependencies
- No cross-layer shortcuts

## Technology Choices (Fixed)
- Java 21
- Spring Boot
- Thymeleaf
- Tailwind CSS
- MySQL + Flyway
- Docker / Docker Compose

## Explicitly Forbidden
- Microservices
- GraphQL
- Reactive stacks
- Frontend frameworks (React/Vue/etc.)
- Client-side state management
- Hidden persistence
- Implicit background jobs
