package app.cookcards.webapp.controller;

import app.cookcards.webapp.dto.RecipeDTO;
import app.cookcards.webapp.recipe.Recipe;
import app.cookcards.webapp.recipe.RecipeService;
import app.cookcards.webapp.user.User;
import app.cookcards.webapp.user.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = RecipesController.class)
class RecipesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "user@mail.com")
    void recipeDetailRendersWhenOptionalDetailFieldsAreNull() throws Exception {
        User user = new User();
        Recipe recipe = recipe(5L, "{\"name\":\"Simple\"}");
        RecipeDTO recipeDTO = new RecipeDTO(
                "Simple",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of("1 item"),
                List.of(new RecipeDTO.InstructionSectionDTO(
                        "",
                        List.of(new RecipeDTO.InstructionStepDTO("Step one", null))
                ))
        );

        when(userService.requireByEmail("user@mail.com")).thenReturn(user);
        when(recipeService.findForUser(5L, user)).thenReturn(Optional.of(recipe));
        when(recipeService.parseRecipeJson("{\"name\":\"Simple\"}")).thenReturn(recipeDTO);

        mockMvc.perform(get("/recipes/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe"));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void getRecipeEditPrefillsFormFromPersistedRecipe() throws Exception {
        User user = new User();
        Recipe recipe = recipe(5L, "{\"name\":\"Lemon Pie\"}");
        RecipeDTO recipeDTO = sampleRecipeDto("Lemon Pie");

        when(userService.requireByEmail("user@mail.com")).thenReturn(user);
        when(recipeService.findForUser(5L, user)).thenReturn(Optional.of(recipe));
        when(recipeService.parseRecipeJson("{\"name\":\"Lemon Pie\"}")).thenReturn(recipeDTO);

        mockMvc.perform(get("/recipes/5/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-edit"))
                .andExpect(model().attribute("saveAction", "/recipes/5/edit"))
                .andExpect(model().attribute("cancelAction", "/recipes/5"))
                .andExpect(model().attribute("recipeEditForm", hasProperty("name", is("Lemon Pie"))))
                .andExpect(model().attribute("recipeEditForm", hasProperty("recipeId", is(5L))));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void postRecipeEditUpdatesRecipeAndRedirects() throws Exception {
        User user = new User();
        Recipe recipe = recipe(5L, "{\"name\":\"Original\"}");

        when(userService.requireByEmail("user@mail.com")).thenReturn(user);
        when(recipeService.findForUser(5L, user)).thenReturn(Optional.of(recipe));
        when(recipeService.updateRecipe(eq(user), eq(5L), any(RecipeDTO.class))).thenReturn(recipe);

        mockMvc.perform(post("/recipes/5/edit")
                        .with(csrf())
                        .param("name", "Updated Pie")
                        .param("description", "Now with lemon zest")
                        .param("ingredients[0]", "2 lemons")
                        .param("ingredients[1]", "1 cup sugar")
                        .param("instructions[0].name", "Fill")
                        .param("instructions[0].steps[0].text", "Mix everything")
                        .param("instructions[0].steps[1].text", "Bake"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/5"));

        ArgumentCaptor<RecipeDTO> dtoCaptor = ArgumentCaptor.forClass(RecipeDTO.class);
        verify(recipeService).updateRecipe(eq(user), eq(5L), dtoCaptor.capture());
        RecipeDTO savedDto = dtoCaptor.getValue();
        assertThat(savedDto.name()).isEqualTo("Updated Pie");
        assertThat(savedDto.ingredients()).containsExactly("2 lemons", "1 cup sugar");
        assertThat(savedDto.instructions()).hasSize(1);
        assertThat(savedDto.instructions().get(0).name()).isEqualTo("Fill");
        assertThat(savedDto.instructions().get(0).steps()).extracting(RecipeDTO.InstructionStepDTO::text)
                .containsExactly("Mix everything", "Bake");
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    void missingRecipeReturns404ForViewEditAndSave() throws Exception {
        User user = new User();
        when(userService.requireByEmail("user@mail.com")).thenReturn(user);
        when(recipeService.findForUser(99L, user)).thenReturn(Optional.empty());

        mockMvc.perform(get("/recipes/99"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/recipes/99/edit"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/recipes/99/edit")
                        .with(csrf())
                        .param("name", "Any")
                        .param("ingredients[0]", "One")
                        .param("instructions[0].steps[0].text", "Step"))
                .andExpect(status().isNotFound());
    }

    private Recipe recipe(Long id, String recipeJson) {
        Recipe recipe = new Recipe();
        ReflectionTestUtils.setField(recipe, "id", id);
        recipe.setTitle("Any title");
        recipe.setRecipeJson(recipeJson);
        return recipe;
    }

    private RecipeDTO sampleRecipeDto(String name) {
        return new RecipeDTO(
                name,
                null,
                "A tart pie",
                null,
                null,
                null,
                null,
                null,
                List.of("1 crust", "2 lemons"),
                List.of(new RecipeDTO.InstructionSectionDTO(
                        "",
                        List.of(new RecipeDTO.InstructionStepDTO("Bake", null))
                ))
        );
    }
}
