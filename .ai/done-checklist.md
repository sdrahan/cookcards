## Definition of Done

Every change must satisfy **all** applicable items below.

### Specification
- [ ] Feature is described or updated in `docs/`
- [ ] Scope and non-goals are explicit
- [ ] Edge cases are documented

### Code
- [ ] Code follows `.ai/style.md`
- [ ] No unused classes, methods, or configs
- [ ] No speculative abstractions

### Validation & Errors
- [ ] Inputs validated at boundaries
- [ ] Errors handled deterministically
- [ ] User-facing errors are readable

### Persistence
- [ ] Flyway migration added (if schema changed)
- [ ] Migration is idempotent and ordered

### Security
- [ ] No sensitive data logged
- [ ] CSRF considered for POST endpoints
- [ ] Session usage reviewed

### UI
- [ ] Degrades gracefully
- [ ] Printable output tested

### Edge Cases
- [ ] Empty input
- [ ] Large input
- [ ] Invalid input
- [ ] Session expiration

If any checkbox is unchecked, the work is **not done**.