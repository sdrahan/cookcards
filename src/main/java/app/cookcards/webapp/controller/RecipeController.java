package app.cookcards.webapp.controller;


import app.cookcards.webapp.dto.RecipeDTO;
import app.cookcards.webapp.recipe.Recipe;
import app.cookcards.webapp.recipe.RecipeService;
import app.cookcards.webapp.service.OpenAiRecipeClient;
import app.cookcards.webapp.service.RecipeParsingService;
import app.cookcards.webapp.service.ParsedRecipeResult;
import app.cookcards.webapp.user.UserService;
import app.cookcards.webapp.user.User;
import app.cookcards.webapp.user.UserSettings;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class RecipeController {

    public static final String SESSION_PARSED_RECIPE_KEY = "parsedRecipe";
    public static final String SESSION_PARSED_RECIPE_JSON_KEY = "parsedRecipeJson";

    private final RecipeParsingService recipeParsingService;
    private final UserService userService;
    private final RecipeService recipeService;

    public RecipeController(RecipeParsingService recipeParsingService,
                            UserService userService,
                            RecipeService recipeService) {
        this.recipeParsingService = recipeParsingService;
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @GetMapping("/recipes/new")
    public String addRecipePage(Model model) {
        if (!model.containsAttribute("addRecipeForm")) {
            model.addAttribute("addRecipeForm", new AddRecipeForm());
        }
        return "add-recipe";
    }

    @PostMapping("/recipes/new")
    public String parseRecipe(@ModelAttribute("addRecipeForm") @Valid AddRecipeForm addRecipeForm,
                        BindingResult bindingResult,
                        Authentication authentication,
                        HttpSession session,
                        Model model) {
        String freeText = addRecipeForm.freeText() == null ? "" : addRecipeForm.freeText().trim();

        if (!StringUtils.hasText(freeText) || freeText.length() < 20) {
            bindingResult.rejectValue("freeText", "freeText.minLength", "Please paste at least 20 characters of recipe text.");
        }

        if (bindingResult.hasErrors()) {
            return "add-recipe";
        }

        ParsedRecipeResult parseResult;
        try {
            boolean validRecipe = recipeParsingService.isValidFoodRecipe(freeText);
            if (!validRecipe) {
                bindingResult.rejectValue("freeText", "freeText.invalidRecipe", "Input does not look like a valid food recipe.");
                return "add-recipe";
            }

            UserSettings settings = userService.getOrCreateSettingsByEmail(authentication.getName());
            parseResult = recipeParsingService.parseFromFreeText(
                    freeText,
                    settings.getUnitsMode(),
                    settings.getTargetLanguage()
            );
        } catch (OpenAiRecipeClient.OpenAiException ex) {
            bindingResult.reject("parse.failed", "Could not parse recipe right now. Please try again.");
            return "add-recipe";
        }

        session.setAttribute(SESSION_PARSED_RECIPE_KEY, parseResult.recipe());
        session.setAttribute(SESSION_PARSED_RECIPE_JSON_KEY, parseResult.recipeJson());

        return "redirect:/recipes/preview";
    }

    @GetMapping("/recipes/preview")
    public String previewRecipe(HttpSession session, Model model) {
        RecipeDTO recipe = (RecipeDTO) session.getAttribute(SESSION_PARSED_RECIPE_KEY);
        if (recipe == null) {
            return "redirect:/recipes/new";
        }
        model.addAttribute("recipe", recipe);
        return "recipe-preview";
    }

    @PostMapping("/recipes/preview/save")
    public String saveParsedRecipe(Authentication authentication, HttpSession session) {
        RecipeDTO recipe = (RecipeDTO) session.getAttribute(SESSION_PARSED_RECIPE_KEY);
        String recipeJson = (String) session.getAttribute(SESSION_PARSED_RECIPE_JSON_KEY);
        if (recipe == null || !StringUtils.hasText(recipeJson)) {
            return "redirect:/recipes/new";
        }

        User user = userService.requireByEmail(authentication.getName());
        Recipe saved = recipeService.createRecipe(user, recipe.name(), recipeJson);

        session.removeAttribute(SESSION_PARSED_RECIPE_KEY);
        session.removeAttribute(SESSION_PARSED_RECIPE_JSON_KEY);
        return "redirect:/recipes/" + saved.getId();
    }

    public record AddRecipeForm(String freeText) {
        public AddRecipeForm() {
            this("");
        }
    }
}
