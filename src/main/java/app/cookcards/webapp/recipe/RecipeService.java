package app.cookcards.webapp.recipe;

import app.cookcards.webapp.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> listForUser(User user) {
        return recipeRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    public Optional<Recipe> findForUser(Long recipeId, User user) {
        return recipeRepository.findByIdAndUser(recipeId, user);
    }

    @Transactional
    public Recipe createRecipe(User user, String title) {
        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setTitle(title);
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
}
