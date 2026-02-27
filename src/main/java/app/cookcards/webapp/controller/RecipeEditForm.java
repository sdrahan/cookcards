package app.cookcards.webapp.controller;

import app.cookcards.webapp.dto.RecipeDTO;

import java.util.ArrayList;
import java.util.List;

public class RecipeEditForm {

    private Long recipeId;
    private SourceContext sourceContext = SourceContext.PARSED;
    private String name = "";
    private String description = "";
    private String recipeYield = "";
    private String prepTime = "";
    private String cookTime = "";
    private String totalTime = "";
    private String calories = "";
    private List<String> ingredients = new ArrayList<>();
    private List<InstructionSectionForm> instructions = new ArrayList<>();

    public RecipeEditForm() {
        ensureDefaults();
    }

    public static RecipeEditForm fromRecipeDTO(RecipeDTO recipeDTO, SourceContext sourceContext, Long recipeId) {
        RecipeEditForm form = new RecipeEditForm();
        form.setSourceContext(sourceContext == null ? SourceContext.PARSED : sourceContext);
        form.setRecipeId(recipeId);
        form.setName(orEmpty(recipeDTO == null ? null : recipeDTO.name()));
        form.setDescription(orEmpty(recipeDTO == null ? null : recipeDTO.description()));
        form.setRecipeYield(orEmpty(recipeDTO == null ? null : recipeDTO.recipeYield()));
        form.setPrepTime(orEmpty(recipeDTO == null ? null : recipeDTO.prepTime()));
        form.setCookTime(orEmpty(recipeDTO == null ? null : recipeDTO.cookTime()));
        form.setTotalTime(orEmpty(recipeDTO == null ? null : recipeDTO.totalTime()));
        form.setCalories(orEmpty(readCalories(recipeDTO)));

        List<String> mappedIngredients = new ArrayList<>();
        if (recipeDTO != null && recipeDTO.ingredients() != null) {
            mappedIngredients.addAll(recipeDTO.ingredients());
        }
        form.setIngredients(mappedIngredients);

        List<InstructionSectionForm> mappedInstructions = new ArrayList<>();
        if (recipeDTO != null && recipeDTO.instructions() != null) {
            for (RecipeDTO.InstructionSectionDTO section : recipeDTO.instructions()) {
                InstructionSectionForm sectionForm = new InstructionSectionForm();
                sectionForm.setName(orEmpty(section == null ? null : section.name()));
                List<InstructionStepForm> stepForms = new ArrayList<>();
                if (section != null && section.steps() != null) {
                    for (RecipeDTO.InstructionStepDTO step : section.steps()) {
                        InstructionStepForm stepForm = new InstructionStepForm();
                        stepForm.setText(orEmpty(step == null ? null : step.text()));
                        stepForms.add(stepForm);
                    }
                }
                sectionForm.setSteps(stepForms);
                mappedInstructions.add(sectionForm);
            }
        }
        form.setInstructions(mappedInstructions);
        form.ensureDefaults();
        return form;
    }

    public RecipeDTO toRecipeDTO() {
        String normalizedCalories = toNullable(calories);
        RecipeDTO.NutritionDTO nutrition = normalizedCalories == null ? null : new RecipeDTO.NutritionDTO(normalizedCalories);

        List<String> mappedIngredients = new ArrayList<>(ingredients == null ? List.of() : ingredients);
        List<RecipeDTO.InstructionSectionDTO> mappedInstructions = new ArrayList<>();
        if (instructions != null) {
            for (InstructionSectionForm section : instructions) {
                List<RecipeDTO.InstructionStepDTO> mappedSteps = new ArrayList<>();
                if (section != null && section.getSteps() != null) {
                    for (InstructionStepForm step : section.getSteps()) {
                        mappedSteps.add(new RecipeDTO.InstructionStepDTO(step == null ? null : step.getText(), null));
                    }
                }
                mappedInstructions.add(new RecipeDTO.InstructionSectionDTO(
                        section == null ? null : section.getName(),
                        mappedSteps
                ));
            }
        }

        return new RecipeDTO(
                name,
                null,
                toNullable(description),
                toNullable(prepTime),
                toNullable(cookTime),
                toNullable(totalTime),
                toNullable(recipeYield),
                nutrition,
                mappedIngredients,
                mappedInstructions
        );
    }

    public void ensureDefaults() {
        if (ingredients == null) {
            ingredients = new ArrayList<>();
        }
        if (ingredients.isEmpty()) {
            ingredients.add("");
        }
        if (instructions == null) {
            instructions = new ArrayList<>();
        }
        if (instructions.isEmpty()) {
            instructions.add(new InstructionSectionForm());
        }
        for (InstructionSectionForm section : instructions) {
            if (section == null) {
                continue;
            }
            if (section.getSteps() == null) {
                section.setSteps(new ArrayList<>());
            }
            if (section.getSteps().isEmpty()) {
                section.getSteps().add(new InstructionStepForm());
            }
        }
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public SourceContext getSourceContext() {
        return sourceContext;
    }

    public void setSourceContext(SourceContext sourceContext) {
        this.sourceContext = sourceContext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecipeYield() {
        return recipeYield;
    }

    public void setRecipeYield(String recipeYield) {
        this.recipeYield = recipeYield;
    }

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getCookTime() {
        return cookTime;
    }

    public void setCookTime(String cookTime) {
        this.cookTime = cookTime;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<InstructionSectionForm> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<InstructionSectionForm> instructions) {
        this.instructions = instructions;
    }

    public enum SourceContext {
        PARSED,
        EXISTING
    }

    public static class InstructionSectionForm {
        private String name = "";
        private List<InstructionStepForm> steps = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<InstructionStepForm> getSteps() {
            return steps;
        }

        public void setSteps(List<InstructionStepForm> steps) {
            this.steps = steps;
        }
    }

    public static class InstructionStepForm {
        private String text = "";

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    private static String readCalories(RecipeDTO recipeDTO) {
        if (recipeDTO == null || recipeDTO.nutrition() == null) {
            return null;
        }
        return recipeDTO.nutrition().calories();
    }

    private static String toNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
