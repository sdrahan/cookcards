package app.cookcards.webapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Structured recipe extracted from user input (free text or OCR).
 * IMPORTANT: Preserve the original language. Do NOT translate.
 */
public record RecipeDTO(
        @NotBlank String title,                 // keep in original language
        String language,                        // BCP-47 if possible (e.g. "ru", "uk", "en"); can be null
        List<String> servingsAndYield,          // e.g. "Serves 4", "2 loaves" (optional)
        List<String> times,                     // e.g. "Prep: 15 min", "Cook: 45 min" (optional)
        @NotEmpty List<IngredientDTO> ingredients,
        @NotEmpty List<StepDTO> steps,
        List<String> notes,                     // optional
        List<String> tags,// optional (e.g. "dessert", "vegan") - keep original language if present
        String source                           // optional (e.g. URL or "handwritten note")
) {
    public static RecipeDTO empty() {
        return new RecipeDTO("", null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null);
    }
}