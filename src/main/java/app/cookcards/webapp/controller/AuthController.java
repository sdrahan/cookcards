package app.cookcards.webapp.controller;

import app.cookcards.webapp.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/")
    public String landing(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/recipes";
        }
        return "landing";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/recipes";
        }
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Authentication authentication, Model model) {
        if (isAuthenticated(authentication)) {
            return "redirect:/recipes";
        }
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(@ModelAttribute("signupForm") @Valid SignupForm form,
                               BindingResult bindingResult,
                               Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/recipes";
        }

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        if (userService.emailExists(form.email())) {
            bindingResult.rejectValue("email", "email.exists", "An account with this email already exists.");
            return "signup";
        }

        String normalizedEmail = UserService.normalizeEmail(form.email());
        userService.createUser(normalizedEmail, form.password());

        Authentication authResult = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, form.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authResult);

        return "redirect:/recipes";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public record SignupForm(
            @NotBlank(message = "Email is required.")
            @Email(message = "Please enter a valid email address.")
            String email,
            @NotBlank(message = "Password is required.")
            @Size(min = 8, message = "Password must be at least 8 characters.")
            String password
    ) {
        public SignupForm() {
            this("", "");
        }
    }
}
