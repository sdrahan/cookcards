package app.cookcards.webapp.controller;


import app.cookcards.webapp.dto.RecipeDTO;
import app.cookcards.webapp.service.RecipeParsingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class RecipeController {

    public static final String SESSION_RECIPE_KEY = "recipe";

    private final RecipeParsingService recipeParsingService;

    public RecipeController(RecipeParsingService recipeParsingService) {
        this.recipeParsingService = recipeParsingService;
    }

    // For now: ONLY freetext is implemented.
    @PostMapping("/parse")
    public String parse(@ModelAttribute("parseRequest") @Valid ParseRequest parseRequest,
                        BindingResult bindingResult,
                        HttpSession session,
                        Model model) {

        // Basic validation: require at least one input later; for now only freetext
        if (!StringUtils.hasText(parseRequest.freeText())) {
            bindingResult.rejectValue("freeText", "freeText.empty", "Please paste a recipe text.");
        }

        if (bindingResult.hasErrors()) {
            return "index";
        }

        RecipeDTO recipe = recipeParsingService.parseFromFreeText(parseRequest.freeText().trim());

        session.setAttribute(SESSION_RECIPE_KEY, recipe);

        // Default template is classic
        return "redirect:/review?template=classic";
    }

    // shows editable fields + template selector + preview.
    @GetMapping("/review")
    public String review(@RequestParam(name = "template", required = false, defaultValue = "classic") String template,
                         HttpSession session,
                         Model model) {

        RecipeDTO recipe = (RecipeDTO) session.getAttribute(SESSION_RECIPE_KEY);
        if (recipe == null) {
            return "redirect:/";
        }

        Template tpl = Template.from(template);

        // Form backing object (editable)
        ReviewForm form = ReviewForm.fromRecipe(recipe, tpl);

        model.addAttribute("recipe", recipe);
        model.addAttribute("reviewForm", form);
        model.addAttribute("template", tpl.code());
        model.addAttribute("availableTemplates", Template.values());

        // You can use this in Thymeleaf to include the correct preview partial:
        // th:replace="~{templates/${template} :: preview(...) }"
        model.addAttribute("previewFragment", "templates/" + tpl.code());

        return "review";
    }

    // updates session with user edits, stays on review.
    // Supports template switching via ?template=classic|minimal|modern|super_premium_luxury
    @PostMapping("/review")
    public String updateReview(@RequestParam(name = "template", required = false, defaultValue = "classic") String template,
                               @ModelAttribute("reviewForm") @Valid ReviewForm reviewForm,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model) {

        RecipeDTO existing = (RecipeDTO) session.getAttribute(SESSION_RECIPE_KEY);
        if (existing == null) {
            return "redirect:/";
        }

        Template tpl = Template.from(template);

        if (bindingResult.hasErrors()) {
            model.addAttribute("template", tpl.code());
            model.addAttribute("availableTemplates", Template.values());
            model.addAttribute("previewFragment", "templates/" + tpl.code());
            return "review";
        }

        // Convert editable form back to DTO and store in session
        RecipeDTO updated = reviewForm.toRecipeDto(existing);
        session.setAttribute(SESSION_RECIPE_KEY, updated);

        // Stay on review and preserve template
        return "redirect:/review?template=" + tpl.code();
    }

    // INPUT FORM for / (index)
    public record ParseRequest(
            String freeText,
            String url
            // TODO later: MultipartFile[] images
    ) {
        public ParseRequest() {
            this("", "");
        }
    }

    public enum Template {
        CLASSIC("classic"),
        MINIMAL("minimal"),
        CARD("card");

        private final String code;

        Template(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static Template from(String raw) {
            if (!StringUtils.hasText(raw)) return CLASSIC;
            String v = raw.trim().toLowerCase();
            for (Template t : values()) {
                if (t.code.equals(v)) return t;
            }
            return CLASSIC;
        }
    }
}
