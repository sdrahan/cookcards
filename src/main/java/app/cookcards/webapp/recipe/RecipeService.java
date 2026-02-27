package app.cookcards.webapp.recipe;

import app.cookcards.webapp.dto.RecipeDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.cookcards.webapp.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper;

    public RecipeService(RecipeRepository recipeRepository, ObjectMapper objectMapper) {
        this.recipeRepository = recipeRepository;
        this.objectMapper = objectMapper;
    }

    public List<Recipe> listForUser(User user) {
        return recipeRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    public Optional<Recipe> findForUser(Long recipeId, User user) {
        return recipeRepository.findByIdAndUser(recipeId, user);
    }

    @Transactional
    public Recipe createRecipe(User user, String title) {
        return createRecipe(user, title, minimalRecipeJson(title));
    }

    @Transactional
    public Recipe createRecipe(User user, String title, String recipeJson) {
        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setTitle(title);
        recipe.setRecipeJson(recipeJson);
        return recipeRepository.save(recipe);
    }

    @Transactional
    public boolean deleteForUser(Long recipeId, User user) {
        Optional<Recipe> recipe = recipeRepository.findByIdAndUser(recipeId, user);
        if (recipe.isEmpty()) {
            return false;
        }
        recipeRepository.delete(recipe.get());
        return true;
    }

    public RecipeDTO parseRecipeJson(String recipeJson) {
        try {
            return objectMapper.readValue(recipeJson, RecipeDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse persisted recipe JSON.", e);
        }
    }

    public String serializeRecipeDto(RecipeDTO recipeDTO) {
        try {
            return objectMapper.writeValueAsString(recipeDTO);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize recipe DTO.", e);
        }
    }

    @Transactional
    public Recipe updateRecipe(User user, Long recipeId, RecipeDTO recipeDTO) {
        Recipe recipe = recipeRepository.findByIdAndUser(recipeId, user)
                .orElseThrow(() -> new IllegalStateException("Recipe not found for user."));
        recipe.setTitle(recipeDTO.name());
        recipe.setRecipeJson(serializeRecipeDto(recipeDTO));
        return recipeRepository.save(recipe);
    }

    private String minimalRecipeJson(String title) {
        String safeTitle = title == null || title.isBlank() ? "Untitled recipe" : title.trim();
        String escapedTitle = safeTitle.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"name\":\"" + escapedTitle
                + "\",\"ingredients\":[\"1 item\"],\"instructions\":[{\"steps\":[{\"text\":\"Add instructions.\"}]}]}";
    }
}
