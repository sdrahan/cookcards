package app.cookcards.webapp;

import app.cookcards.webapp.controller.RecipeController;
import app.cookcards.webapp.dto.IngredientDTO;
import app.cookcards.webapp.dto.RecipeDTO;
import app.cookcards.webapp.dto.StepDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(
        properties = {
                "openai.api.key=dummy-test-key",
                "spring.flyway.enabled=false"
        }
)
@AutoConfigureMockMvc
class ReviewPageIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reviewPageRendersWithSessionRecipe() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(RecipeController.SESSION_RECIPE_KEY, dummyRecipe());

        mockMvc.perform(get("/review")
                        .param("template", "classic")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("review"))
                .andExpect(model().attributeExists("recipe", "reviewForm", "template", "availableTemplates", "previewFragment"))
                .andExpect(model().attribute("template", equalTo("classic")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Review & Preview")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Best Pancakes")));
    }

    private static RecipeDTO dummyRecipe() {
        return new RecipeDTO(
                "Best Pancakes",
                "en",
                List.of("Serves 2"),
                List.of("Prep: 5 min", "Cook: 10 min"),
                List.of(
                        new IngredientDTO(null, "1", "cup", "flour", null),
                        new IngredientDTO(null, "1", "cup", "milk", null),
                        new IngredientDTO(null, "1", "pc", "egg", null)
                ),
                List.of(
                        new StepDTO(1, "Whisk the batter until smooth."),
                        new StepDTO(2, "Cook on a hot griddle until golden.")
                ),
                List.of("Serve warm."),
                List.of("breakfast"),
                "family recipe"
        );
    }
}
