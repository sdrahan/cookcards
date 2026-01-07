package app.cookcards.webapp.service;

import app.cookcards.webapp.dto.RecipeDTO;
import org.springframework.stereotype.Service;

@Service
public class RecipeParsingService {

    private final OpenAiRecipeClient openAiRecipeClient;

    public RecipeParsingService(OpenAiRecipeClient openAiRecipeClient) {
        this.openAiRecipeClient = openAiRecipeClient;
    }

    public RecipeDTO parseFromFreeText(String freeText) {
        if (freeText == null || freeText.trim().isEmpty()) {
            throw new IllegalArgumentException("freeText is empty");
        }
        return openAiRecipeClient.parseRecipeFromText(freeText.trim());
    }
}
