package app.cookcards.webapp.controller;

import app.cookcards.webapp.dto.RecipeDTO;
import app.cookcards.webapp.recipe.Recipe;
import app.cookcards.webapp.recipe.RecipeService;
import app.cookcards.webapp.service.ParsedRecipeResult;
import app.cookcards.webapp.service.RecipeParsingService;
import app.cookcards.webapp.user.TargetLanguage;
import app.cookcards.webapp.user.UnitsMode;
import app.cookcards.webapp.user.User;
import app.cookcards.webapp.user.UserService;
import app.cookcards.webapp.user.UserSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeParsingService recipeParsingService;

    @MockBean
    private UserService userService;

    @MockBean
    private RecipeService recipeService;

    @Test
    @WithMockUser(username = "user@mail.com")
    void parseRecipeRedirectsToEditOnSuccess() throws Exception {
        UserSettings settings = new UserSettings();
        settings.setUnitsMode(UnitsMode.ORIGINAL);
        settings.setTargetLanguage(TargetLanguage.ORIGINAL);
        RecipeDTO parsedRecipe = sampleRecipe();

        when(recipeParsingService.isValidFoodRecipe("This is a sufficiently long recipe body with ingredients and steps."))
                .thenReturn(true);
        when(userService.getOrCreateSettingsByEmail("user@mail.com")).thenReturn(settings);
        when(recipeParsingService.parseFromFreeText(
                "This is a sufficiently long recipe body with ingredients and steps.",
                UnitsMode.ORIGINAL,
                TargetLanguage.ORIGINAL
        )).thenReturn(new ParsedRecipeResult(parsedRecipe, "{\"name\":\"Soup\"}"));

        MvcResult result = mockMvc.perform(post("/recipes/new")
                        .with(csrf())
                        .param("freeText", "This is a sufficiently long recipe body with ingredients and steps."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/edit"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(RecipeController.SESSION_PARSED_RECIPE_KEY)).isEqualTo(parsedRecipe);
        assertThat(session.getAttribute(RecipeController.SESSION_PARSED_RECIPE_JSON_KEY)).isEqualTo("{\"name\":\"Soup\"}");
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void getParsedRecipeEditWithoutSessionRecipeRedirectsToAddRecipe() throws Exception {
        mockMvc.perform(get("/recipes/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/new"));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void saveParsedRecipeCreatesRecipeClearsSessionAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(RecipeController.SESSION_PARSED_RECIPE_KEY, sampleRecipe());
        session.setAttribute(RecipeController.SESSION_PARSED_RECIPE_JSON_KEY, "{\"name\":\"Old\"}");

        User user = new User();
        Recipe saved = new Recipe();
        ReflectionTestUtils.setField(saved, "id", 42L);

        when(userService.requireByEmail("user@mail.com")).thenReturn(user);
        when(recipeService.serializeRecipeDto(any(RecipeDTO.class))).thenReturn("{\"name\":\"Tomato Soup\"}");
        when(recipeService.createRecipe(user, "Tomato Soup", "{\"name\":\"Tomato Soup\"}")).thenReturn(saved);

        MvcResult result = mockMvc.perform(post("/recipes/edit")
                        .with(csrf())
                        .session(session)
                        .param("name", "Tomato Soup")
                        .param("description", "Simple soup")
                        .param("ingredients[0]", "1 tomato")
                        .param("instructions[0].name", "")
                        .param("instructions[0].steps[0].text", "Mix and cook"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/42"))
                .andReturn();

        MockHttpSession resultingSession = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(resultingSession.getAttribute(RecipeController.SESSION_PARSED_RECIPE_KEY)).isNull();
        assertThat(resultingSession.getAttribute(RecipeController.SESSION_PARSED_RECIPE_JSON_KEY)).isNull();
        verify(recipeService).createRecipe(user, "Tomato Soup", "{\"name\":\"Tomato Soup\"}");
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void saveParsedRecipeRejectsBlankNameIngredientsAndSteps() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(RecipeController.SESSION_PARSED_RECIPE_KEY, sampleRecipe());

        mockMvc.perform(post("/recipes/edit")
                        .with(csrf())
                        .session(session)
                        .param("name", "   ")
                        .param("ingredients[0]", "  ")
                        .param("instructions[0].name", "")
                        .param("instructions[0].steps[0].text", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-edit"))
                .andExpect(model().attributeHasFieldErrors("recipeEditForm", "name", "ingredients", "instructions"));

        verify(recipeService, never()).createRecipe(any(), any(), any());
    }

    private RecipeDTO sampleRecipe() {
        return new RecipeDTO(
                "Soup",
                null,
                "Description",
                null,
                null,
                null,
                null,
                null,
                java.util.List.of("Water"),
                java.util.List.of(new RecipeDTO.InstructionSectionDTO(
                        "",
                        java.util.List.of(new RecipeDTO.InstructionStepDTO("Boil", null))
                ))
        );
    }
}
