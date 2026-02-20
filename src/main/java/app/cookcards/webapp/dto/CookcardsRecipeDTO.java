package app.cookcards.webapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CookcardsRecipeDTO(
        @NotBlank String name,
        List<String> images,
        String description,
        String prepTime,
        String cookTime,
        String totalTime,
        @JsonProperty("yield") String recipeYield,
        NutritionDTO nutrition,
        @NotEmpty List<String> ingredients,
        @NotEmpty List<InstructionSectionDTO> instructions
) {
    public record NutritionDTO(
            String calories
    ) {
    }

    public record InstructionSectionDTO(
            String name,
            @NotEmpty List<InstructionStepDTO> steps
    ) {
    }

    public record InstructionStepDTO(
            @NotBlank String text,
            String image
    ) {
    }
}
