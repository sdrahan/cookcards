## Core Concepts

### Recipe
A structured representation of a cooking instruction set.

### Ingredient
A single line item, optionally structured into quantity/unit/item.

### Step
An ordered instruction.

### Template
A visual representation of a recipe.

## Relationships
- A Recipe has many Ingredients
- A Recipe has many Steps
- A Template renders a Recipe

## Ownership
- Recipes belong to a session (MVP)
- No cross-user access

## Invariants
- A recipe must have a title
- A recipe must have at least one ingredient
- A recipe must have at least one step
- Original language must be preserved