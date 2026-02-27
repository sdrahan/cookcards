package app.cookcards.webapp.controller;

import app.cookcards.webapp.dto.RecipeDTO;
import app.cookcards.webapp.recipe.Recipe;
import app.cookcards.webapp.recipe.RecipeService;
import app.cookcards.webapp.user.User;
import app.cookcards.webapp.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
        RecipeDTO recipeData;
        try {
            recipeData = recipeService.parseRecipeJson(recipe.getRecipeJson());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Recipe data is invalid.");
        }
        model.addAttribute("recipe", recipe);
        model.addAttribute("recipeData", recipeData);
        return "recipe";
    }

    @GetMapping("/recipes/{recipeId}/edit")
    public String recipeEdit(@PathVariable Long recipeId, Authentication authentication, Model model) {
        User user = userService.requireByEmail(authentication.getName());
        Recipe recipe = recipeService.findForUser(recipeId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        RecipeDTO recipeData;
        try {
            recipeData = recipeService.parseRecipeJson(recipe.getRecipeJson());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Recipe data is invalid.");
        }
        RecipeEditForm form = RecipeEditForm.fromRecipeDTO(recipeData, RecipeEditForm.SourceContext.EXISTING, recipe.getId());
        model.addAttribute("recipeEditForm", form);
        return renderEditPage(model, form);
    }

    @PostMapping("/recipes/{recipeId}/edit")
    public String updateRecipe(@PathVariable Long recipeId,
                               Authentication authentication,
                               @ModelAttribute("recipeEditForm") RecipeEditForm recipeEditForm,
                               BindingResult bindingResult,
                               Model model) {
        User user = userService.requireByEmail(authentication.getName());
        recipeService.findForUser(recipeId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        RecipeEditForm sanitizedForm = RecipeEditFormSanitizer.sanitizeForSave(recipeEditForm);
        sanitizedForm.setSourceContext(RecipeEditForm.SourceContext.EXISTING);
        sanitizedForm.setRecipeId(recipeId);
        RecipeEditFormSanitizer.validateForSave(sanitizedForm, bindingResult);

        if (bindingResult.hasErrors()) {
            applyFormValues(recipeEditForm, sanitizedForm);
            recipeEditForm.ensureDefaults();
            return renderEditPage(model, recipeEditForm);
        }

        recipeService.updateRecipe(user, recipeId, sanitizedForm.toRecipeDTO());
        return "redirect:/recipes/" + recipeId;
    }

    @GetMapping("/recipes/{recipeId}/print")
    public String recipePrint(@PathVariable Long recipeId, Authentication authentication, Model model) {
        User user = userService.requireByEmail(authentication.getName());
        Recipe recipe = recipeService.findForUser(recipeId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        RecipeDTO recipeData;
        try {
            recipeData = recipeService.parseRecipeJson(recipe.getRecipeJson());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Recipe data is invalid.");
        }
        model.addAttribute("recipe", recipe);
        model.addAttribute("recipeData", recipeData);
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

    private String renderEditPage(Model model, RecipeEditForm form) {
        RecipeEditForm safeForm = form == null ? new RecipeEditForm() : form;
        safeForm.ensureDefaults();

        boolean parsedFlow = safeForm.getSourceContext() == RecipeEditForm.SourceContext.PARSED;
        String saveAction = parsedFlow
                ? "/recipes/edit"
                : "/recipes/" + safeForm.getRecipeId() + "/edit";
        String cancelAction = parsedFlow
                ? "/recipes/edit/cancel"
                : "/recipes/" + safeForm.getRecipeId();

        if (!model.containsAttribute("recipeEditForm")) {
            model.addAttribute("recipeEditForm", safeForm);
        }
        model.addAttribute("isParsedFlow", parsedFlow);
        model.addAttribute("pageSubtitle", parsedFlow ? "Review & fix parsed recipe" : "Edit recipe");
        model.addAttribute("saveAction", saveAction);
        model.addAttribute("cancelAction", cancelAction);
        model.addAttribute("cancelLabel", parsedFlow ? "Back to all recipes" : "Cancel");
        return "recipe-edit";
    }

    private void applyFormValues(RecipeEditForm target, RecipeEditForm source) {
        target.setRecipeId(source.getRecipeId());
        target.setSourceContext(source.getSourceContext());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setRecipeYield(source.getRecipeYield());
        target.setPrepTime(source.getPrepTime());
        target.setCookTime(source.getCookTime());
        target.setTotalTime(source.getTotalTime());
        target.setCalories(source.getCalories());
        target.setIngredients(source.getIngredients());
        target.setInstructions(source.getInstructions());
    }
}
