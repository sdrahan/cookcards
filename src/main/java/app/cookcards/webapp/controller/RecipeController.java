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
                        HttpSession session) {
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

        return "redirect:/recipes/edit";
    }

    @GetMapping("/recipes/edit")
    public String parsedRecipeEdit(HttpSession session, Model model) {
        RecipeDTO recipe = (RecipeDTO) session.getAttribute(SESSION_PARSED_RECIPE_KEY);
        if (recipe == null) {
            return "redirect:/recipes/new";
        }

        if (!model.containsAttribute("recipeEditForm")) {
            model.addAttribute("recipeEditForm", RecipeEditForm.fromRecipeDTO(recipe, RecipeEditForm.SourceContext.PARSED, null));
        }
        RecipeEditForm form = (RecipeEditForm) model.asMap().get("recipeEditForm");
        return renderEditPage(model, form);
    }

    @PostMapping("/recipes/edit")
    public String saveParsedRecipe(Authentication authentication,
                                   HttpSession session,
                                   @ModelAttribute("recipeEditForm") RecipeEditForm recipeEditForm,
                                   BindingResult bindingResult,
                                   Model model) {
        RecipeDTO sessionRecipe = (RecipeDTO) session.getAttribute(SESSION_PARSED_RECIPE_KEY);
        if (sessionRecipe == null) {
            return "redirect:/recipes/new";
        }

        RecipeEditForm sanitizedForm = RecipeEditFormSanitizer.sanitizeForSave(recipeEditForm);
        sanitizedForm.setSourceContext(RecipeEditForm.SourceContext.PARSED);
        RecipeEditFormSanitizer.validateForSave(sanitizedForm, bindingResult);

        if (bindingResult.hasErrors()) {
            applyFormValues(recipeEditForm, sanitizedForm);
            recipeEditForm.ensureDefaults();
            return renderEditPage(model, recipeEditForm);
        }

        User user = userService.requireByEmail(authentication.getName());
        RecipeDTO recipeDTO = sanitizedForm.toRecipeDTO();
        String recipeJson = recipeService.serializeRecipeDto(recipeDTO);
        Recipe saved = recipeService.createRecipe(user, recipeDTO.name(), recipeJson);

        session.removeAttribute(SESSION_PARSED_RECIPE_KEY);
        session.removeAttribute(SESSION_PARSED_RECIPE_JSON_KEY);
        return "redirect:/recipes/" + saved.getId();
    }

    @PostMapping("/recipes/edit/cancel")
    public String cancelParsedRecipe(HttpSession session) {
        session.removeAttribute(SESSION_PARSED_RECIPE_KEY);
        session.removeAttribute(SESSION_PARSED_RECIPE_JSON_KEY);
        return "redirect:/recipes";
    }

    @GetMapping("/recipes/preview")
    public String previewRecipeCompatibilityRedirect() {
        return "redirect:/recipes/edit";
    }

    @PostMapping("/recipes/preview/save")
    public String savePreviewCompatibilityRedirect() {
        return "redirect:/recipes/edit";
    }

    public record AddRecipeForm(String freeText) {
        public AddRecipeForm() {
            this("");
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
