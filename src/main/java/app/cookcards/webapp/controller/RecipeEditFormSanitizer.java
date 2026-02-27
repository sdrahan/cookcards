package app.cookcards.webapp.controller;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

final class RecipeEditFormSanitizer {

    private RecipeEditFormSanitizer() {
    }

    static RecipeEditForm sanitizeForSave(RecipeEditForm raw) {
        RecipeEditForm sanitized = new RecipeEditForm();
        sanitized.setRecipeId(raw.getRecipeId());
        sanitized.setSourceContext(raw.getSourceContext());
        sanitized.setName(trimToEmpty(raw.getName()));
        sanitized.setDescription(trimToEmpty(raw.getDescription()));
        sanitized.setRecipeYield(trimToEmpty(raw.getRecipeYield()));
        sanitized.setPrepTime(trimToEmpty(raw.getPrepTime()));
        sanitized.setCookTime(trimToEmpty(raw.getCookTime()));
        sanitized.setTotalTime(trimToEmpty(raw.getTotalTime()));
        sanitized.setCalories(trimToEmpty(raw.getCalories()));

        List<String> ingredients = new ArrayList<>();
        if (raw.getIngredients() != null) {
            for (String ingredient : raw.getIngredients()) {
                String normalized = trimToNull(ingredient);
                if (normalized != null) {
                    ingredients.add(normalized);
                }
            }
        }
        sanitized.setIngredients(ingredients);

        List<RecipeEditForm.InstructionSectionForm> sections = new ArrayList<>();
        if (raw.getInstructions() != null) {
            for (RecipeEditForm.InstructionSectionForm section : raw.getInstructions()) {
                String sectionName = section == null ? null : trimToNull(section.getName());
                List<RecipeEditForm.InstructionStepForm> steps = new ArrayList<>();
                if (section != null && section.getSteps() != null) {
                    for (RecipeEditForm.InstructionStepForm step : section.getSteps()) {
                        String text = step == null ? null : trimToNull(step.getText());
                        if (text != null) {
                            RecipeEditForm.InstructionStepForm stepForm = new RecipeEditForm.InstructionStepForm();
                            stepForm.setText(text);
                            steps.add(stepForm);
                        }
                    }
                }
                if (!steps.isEmpty()) {
                    RecipeEditForm.InstructionSectionForm sectionForm = new RecipeEditForm.InstructionSectionForm();
                    sectionForm.setName(sectionName == null ? "" : sectionName);
                    sectionForm.setSteps(steps);
                    sections.add(sectionForm);
                }
            }
        }
        sanitized.setInstructions(sections);
        return sanitized;
    }

    static void validateForSave(RecipeEditForm form, BindingResult bindingResult) {
        if (!StringUtils.hasText(form.getName())) {
            bindingResult.rejectValue("name", "name.required", "Recipe name is required.");
        }
        if (form.getIngredients() == null || form.getIngredients().isEmpty()) {
            bindingResult.rejectValue("ingredients", "ingredients.required", "Add at least one ingredient.");
        }

        int totalSteps = 0;
        if (form.getInstructions() != null) {
            for (RecipeEditForm.InstructionSectionForm section : form.getInstructions()) {
                if (section == null || section.getSteps() == null) {
                    continue;
                }
                totalSteps += section.getSteps().size();
            }
        }
        if (totalSteps == 0) {
            bindingResult.rejectValue("instructions", "instructions.required", "Add at least one instruction step.");
        }
    }

    private static String trimToEmpty(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? "" : normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
