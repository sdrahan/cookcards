package app.cookcards.webapp.infrastructure;

import app.cookcards.webapp.recipe.RecipeService;
import app.cookcards.webapp.user.User;
import app.cookcards.webapp.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("init-db")
public class DatabaseInitializer implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final UserService userService;
    private final RecipeService recipeService;

    public DatabaseInitializer(UserService userService, RecipeService recipeService) {
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @Override
    public void run(String... args) {
        User demoUser = ensureUser("demo@cookcards.app", "123");
        ensureRecipes(demoUser, List.of(
                "Spicy Chickpea Stew",
                "Lemon Herb Salmon",
                "No-Knead Bread"
        ));

        User emptyUser = ensureUser("empty@cookcards.app", "123");
        ensureRecipes(emptyUser, List.of());

        User adminUser = ensureUser("admin@cookcards.app", "123");
        ensureRecipes(adminUser, List.of("Admin Test Recipe"));

        LOGGER.info("Done initializing database");
    }

    private User ensureUser(String email, String password) {
        User user;
        if (userService.emailExists(email)) {
            user = userService.requireByEmail(email);
        } else {
            user = userService.createUser(email, password);
        }
        userService.getOrCreateSettingsByEmail(email);
        return user;
    }

    private void ensureRecipes(User user, List<String> titles) {
        if (!recipeService.listForUser(user).isEmpty()) {
            return;
        }
        for (String title : titles) {
            recipeService.createRecipe(user, title, seedRecipeJson(title));
        }
    }

    private String seedRecipeJson(String title) {
        String escapedTitle = title.replace("\\", "\\\\").replace("\"", "\\\"");
        return """
                {
                  "name":"%s",
                  "description":"Simple seeded recipe.",
                  "ingredients":["1 cup water","1 pinch salt"],
                  "instructions":[
                    {
                      "steps":[
                        {"text":"Add water to a pot."},
                        {"text":"Heat and stir in salt."}
                      ]
                    }
                  ]
                }
                """.formatted(escapedTitle).replace("\n", "").replace("  ", "");
    }
}
