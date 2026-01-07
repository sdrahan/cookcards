package app.cookcards.webapp.controller;

import app.cookcards.webapp.dto.IngredientDTO;
import app.cookcards.webapp.dto.RecipeDTO;
import app.cookcards.webapp.dto.StepDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

/**
 * Editable representation for the Review page.
 * Keeps ingredients/steps editable as lists; you can render them as repeatable fields later.
 * For SuperMVP UI you can also collapse these into multi-line textareas — but keeping lists is future-proof.
 */
public record ReviewForm(
        @NotBlank String title,
        String language,
        @NotEmpty List<IngredientLine> ingredients,
        @NotEmpty List<StepLine> steps,
        String template
) {
    public static ReviewForm fromRecipe(RecipeDTO recipe, RecipeController.Template tpl) {
        List<IngredientLine> ing = new ArrayList<>();
        for (IngredientDTO i : recipe.ingredients()) {
            ing.add(IngredientLine.from(i));
        }

        List<StepLine> st = new ArrayList<>();
        for (StepDTO s : recipe.steps()) {
            st.add(StepLine.from(s));
        }

        return new ReviewForm(
                recipe.title(),
                recipe.language(),
                ing.isEmpty() ? List.of(new IngredientLine(null, null, null, "", null)) : ing,
                st.isEmpty() ? List.of(new StepLine(1, "")) : st,
                tpl.code()
        );
    }

    public RecipeDTO toRecipeDto(RecipeDTO existing) {
        List<IngredientDTO> ing = new ArrayList<>();
        for (IngredientLine l : ingredients) {
            if (l == null) continue;
            if (l.item() == null || l.item().trim().isBlank()) continue;
            ing.add(l.toDto());
        }

        List<StepDTO> st = new ArrayList<>();
        int n = 1;
        for (StepLine l : steps) {
            if (l == null) continue;
            if (l.text() == null || l.text().trim().isBlank()) continue;
            st.add(new StepDTO(n++, l.text().trim()));
        }

        return new RecipeDTO(
                title.trim(),
                language,
                existing.servingsAndYield(),
                existing.times(),
                ing.isEmpty() ? existing.ingredients() : ing,
                st.isEmpty() ? existing.steps() : st,
                existing.notes(),
                existing.tags(),
                existing.source()
        );
    }

    public record IngredientLine(
            String section,
            String quantity,
            String unit,
            String item,
            String preparation
    ) {
        static IngredientLine from(IngredientDTO dto) {
            return new IngredientLine(dto.section(), dto.quantity(), dto.unit(), dto.item(), dto.preparation());
        }

        IngredientDTO toDto() {
            return new IngredientDTO(
                    blankToNull(section),
                    blankToNull(quantity),
                    blankToNull(unit),
                    item == null ? "" : item.trim(),
                    blankToNull(preparation)
            );
        }
    }

    public record StepLine(int number, String text) {
        static StepLine from(StepDTO dto) {
            return new StepLine(dto.number(), dto.text());
        }
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}
