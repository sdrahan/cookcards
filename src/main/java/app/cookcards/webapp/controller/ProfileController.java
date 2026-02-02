package app.cookcards.webapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("email", authentication.getName());
        return "profile";
    }
}
