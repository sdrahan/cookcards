package app.cookcards.webapp.controller;

import app.cookcards.webapp.user.TargetLanguage;
import app.cookcards.webapp.user.UnitsMode;
import app.cookcards.webapp.user.UserService;
import app.cookcards.webapp.user.UserSettings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        UserSettings settings = userService.getOrCreateSettingsByEmail(authentication.getName());
        model.addAttribute("email", authentication.getName());
        model.addAttribute("unitsModes", UnitsMode.values());
        model.addAttribute("targetLanguages", TargetLanguage.values());
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", new ProfileForm(settings.getUnitsMode(), settings.getTargetLanguage()));
        }
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @ModelAttribute("profileForm") @Valid ProfileForm profileForm,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("email", authentication.getName());
            model.addAttribute("unitsModes", UnitsMode.values());
            model.addAttribute("targetLanguages", TargetLanguage.values());
            return "profile";
        }
        userService.updateSettings(authentication.getName(), profileForm.unitsMode(), profileForm.targetLanguage());
        return "redirect:/profile?saved";
    }

    public record ProfileForm(
            @NotNull UnitsMode unitsMode,
            @NotNull TargetLanguage targetLanguage
    ) {
        public ProfileForm() {
            this(UnitsMode.ORIGINAL, TargetLanguage.ORIGINAL);
        }
    }
}
