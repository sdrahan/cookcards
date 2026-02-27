package app.cookcards.webapp.recipe;

import app.cookcards.webapp.dto.RecipeDTO;
import app.cookcards.webapp.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        recipeService = new RecipeService(recipeRepository, new ObjectMapper());
    }

    @Test
    void updateRecipeUpdatesTitleAndRecipeJson() {
        User user = new User();
        Recipe recipe = new Recipe();
        recipe.setTitle("Old");
        recipe.setRecipeJson("{\"name\":\"Old\"}");
        RecipeDTO updatedDto = sampleRecipeDto("Updated");

        when(recipeRepository.findByIdAndUser(7L, user)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recipe saved = recipeService.updateRecipe(user, 7L, updatedDto);

        assertThat(saved.getTitle()).isEqualTo("Updated");
        RecipeDTO parsedBack = recipeService.parseRecipeJson(saved.getRecipeJson());
        assertThat(parsedBack).isEqualTo(updatedDto);
        verify(recipeRepository).save(recipe);
    }

    @Test
    void serializeAndParseRoundTripPreservesRecipeData() {
        RecipeDTO recipeDTO = sampleRecipeDto("Round Trip");

        String json = recipeService.serializeRecipeDto(recipeDTO);
        RecipeDTO parsed = recipeService.parseRecipeJson(json);

        assertThat(parsed).isEqualTo(recipeDTO);
    }

    @Test
    void parseRecipeJsonThrowsForInvalidJson() {
        assertThrows(IllegalStateException.class, () -> recipeService.parseRecipeJson("{not-json"));
    }

    private RecipeDTO sampleRecipeDto(String name) {
        return new RecipeDTO(
                name,
                null,
                "Description",
                "PT10M",
                "PT20M",
                "PT30M",
                "2 servings",
                new RecipeDTO.NutritionDTO("300 kcal"),
                List.of("1 item", "2 item"),
                List.of(new RecipeDTO.InstructionSectionDTO(
                        "Section",
                        List.of(new RecipeDTO.InstructionStepDTO("Step one", null))
                ))
        );
    }
}
