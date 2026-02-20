package app.cookcards.webapp.service;

import app.cookcards.webapp.user.TargetLanguage;
import app.cookcards.webapp.user.UnitsMode;
import org.springframework.stereotype.Service;

@Service
public class RecipeParsingService {

    private final OpenAiRecipeClient openAiRecipeClient;

    public RecipeParsingService(OpenAiRecipeClient openAiRecipeClient) {
        this.openAiRecipeClient = openAiRecipeClient;
    }

    public boolean isValidFoodRecipe(String freeText) {
        if (freeText == null || freeText.trim().isEmpty()) {
            return false;
        }
        return openAiRecipeClient.isValidFoodRecipe(freeText.trim());
    }

    public ParsedRecipeResult parseFromFreeText(String freeText, UnitsMode unitsMode, TargetLanguage targetLanguage) {
        if (freeText == null || freeText.trim().isEmpty()) {
            throw new IllegalArgumentException("freeText is empty");
        }
        return openAiRecipeClient.parseRecipeFromText(freeText.trim(), unitsMode, targetLanguage);
    }
}
