package app.cookcards.webapp.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Cooking instructions.
 */
public record StepDTO(
        @Min(1) int number,
        @NotBlank String text
) {}