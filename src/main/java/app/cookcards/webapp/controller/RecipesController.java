package app.cookcards.webapp.controller;

import app.cookcards.webapp.recipe.Recipe;
import app.cookcards.webapp.recipe.RecipeService;
import app.cookcards.webapp.user.User;
import app.cookcards.webapp.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class RecipesController {

    private final RecipeService recipeService;
    private final UserService userService;

    public RecipesController(RecipeService recipeService, UserService userService) {
        this.recipeService = recipeService;
        this.userService = userService;
    }

    @GetMapping("/recipes")
    public String recipes(Authentication authentication, Model model) {
        User user = userService.requireByEmail(authentication.getName());
        List<Recipe> recipes = recipeService.listForUser(user);
        model.addAttribute("recipes", recipes);
        model.addAttribute("hasRecipes", !recipes.isEmpty());
        return "recipes";
    }

    @GetMapping("/recipes/{recipeId}")
    public String recipeDetail(@PathVariable Long recipeId, Authentication authentication, Model model) {
        User user = userService.requireByEmail(authentication.getName());
        Recipe recipe = recipeService.findForUser(recipeId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("recipe", recipe);
        return "recipe";
    }

    @GetMapping("/recipes/{recipeId}/edit")
    public String recipeEdit(@PathVariable Long recipeId, Authentication authentication, Model model) {
        User user = userService.requireByEmail(authentication.getName());
        Recipe recipe = recipeService.findForUser(recipeId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("recipe", recipe);
        model.addAttribute("mode", "edit");
        return "recipe";
    }

    @GetMapping("/recipes/{recipeId}/print")
    public String recipePrint(@PathVariable Long recipeId, Authentication authentication, Model model) {
        User user = userService.requireByEmail(authentication.getName());
        Recipe recipe = recipeService.findForUser(recipeId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("recipe", recipe);
        model.addAttribute("mode", "print");
        return "recipe";
    }

    @PostMapping("/recipes/{recipeId}/delete")
    public String deleteRecipe(@PathVariable Long recipeId, Authentication authentication) {
        User user = userService.requireByEmail(authentication.getName());
        try {
            boolean deleted = recipeService.deleteForUser(recipeId, user);
            if (!deleted) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            return "redirect:/recipes";
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            return "redirect:/recipes?deleteError";
        }
    }
}
