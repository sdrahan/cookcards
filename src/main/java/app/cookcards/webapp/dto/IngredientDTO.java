package app.cookcards.webapp.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Ingredient line items.
 * Keep text as-is in the recipe's language; parsing quantity/unit is "best effort".
 */
public record IngredientDTO(
        String section,     // e.g. "For the dough", nullable
        String quantity,    // e.g. "200", "1/2", nullable
        String unit,        // e.g. "g", "tbsp", "ст. л.", nullable
        @NotBlank String item,   // e.g. "flour", "мука"
        String preparation  // e.g. "chopped", "drained", nullable
) {}