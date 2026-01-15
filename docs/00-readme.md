## Documentation Structure

This repository has two kinds of documentation:

### `.ai/`
- Operational rules for AI assistants
- Prescriptive and binding
- Defines how work must be done

### `docs/`
- Product and domain documentation
- Descriptive and explanatory
- Defines what the system is and why

## How to Add a New Feature
1. Create or update a spec in `docs/`
2. Define scope and non-goals explicitly
3. Reference existing architecture and domain rules
4. Only then implement code

Specs are contracts. Code must conform.