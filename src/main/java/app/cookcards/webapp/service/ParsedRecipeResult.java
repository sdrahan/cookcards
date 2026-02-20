package app.cookcards.webapp.service;

import app.cookcards.webapp.dto.CookcardsRecipeDTO;

public record ParsedRecipeResult(
        CookcardsRecipeDTO recipe,
        String recipeJson
) {
}
