package app.cookcards.webapp.service;

import app.cookcards.webapp.dto.RecipeDTO;

public record ParsedRecipeResult(
        RecipeDTO recipe,
        String recipeJson
) {
}
